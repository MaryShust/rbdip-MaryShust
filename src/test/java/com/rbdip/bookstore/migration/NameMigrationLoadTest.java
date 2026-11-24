package com.rbdip.bookstore.migration;

import static org.assertj.core.api.Assertions.assertThat;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class NameMigrationLoadTest {

    private static final int READERS = 8;
    private static final int SEEDED_CUSTOMERS = 200;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bookstore_load")
            .withUsername("bookstore")
            .withPassword("bookstore");

    @Test
    void readersDoNotFailWhileNamesAreSplit() throws Exception {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUser(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        flyway(dataSource, "2").migrate();
        seedCustomers(jdbc);

        List<Exception> errors = new CopyOnWriteArrayList<>();
        AtomicBoolean keepReading = new AtomicBoolean(true);
        AtomicInteger reads = new AtomicInteger();
        CountDownLatch started = new CountDownLatch(READERS);
        ExecutorService pool = Executors.newFixedThreadPool(READERS);

        Runnable reader = () -> {
            started.countDown();
            while (keepReading.get()) {
                try (Connection c = dataSource.getConnection();
                        Statement st = c.createStatement()) {
                    st.executeQuery("SELECT full_name FROM customers LIMIT 1");
                    reads.incrementAndGet();
                } catch (SQLException e) {
                    errors.add(e);
                }
            }
        };
        for (int i = 0; i < READERS; i++) {
            pool.submit(reader);
        }
        started.await(5, TimeUnit.SECONDS);

        flyway(dataSource, "4").migrate();

        keepReading.set(false);
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(errors).as("Во время expand/backfill не должно быть ошибок чтения full_name").isEmpty();
        assertThat(reads.get()).as("Читатели должны были реально выполнить запросы").isPositive();

        Integer notBackfilled = jdbc.queryForObject(
                "SELECT count(*) FROM customers WHERE full_name IS NOT NULL"
                        + " AND (first_name IS NULL OR last_name IS NULL)",
                Integer.class);
        assertThat(notBackfilled).as("Все существующие клиенты должны быть backfill-нуты").isZero();

        flyway(dataSource, null).migrate();
        List<String> columns = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns"
                        + " WHERE table_schema = 'public' AND table_name = 'customers'",
                String.class);
        assertThat(columns).doesNotContain("full_name").contains("first_name", "last_name");
    }

    private static void seedCustomers(JdbcTemplate jdbc) {
        for (int i = 0; i < SEEDED_CUSTOMERS; i++) {
            jdbc.update("INSERT INTO customers (full_name, address, phone) VALUES (?, ?, ?)",
                    "Customer" + i + " Surname" + i, "Address " + i, "+7900" + i);
        }
    }

    private static Flyway flyway(PGSimpleDataSource dataSource, String target) {
        var config = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration");
        if (target != null) {
            config.target(target);
        }
        return config.load();
    }
}
