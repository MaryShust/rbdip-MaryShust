-- ЛР3 (expand): добавляем новые колонки first_name/last_name.
-- Старую колонку full_name пока НЕ трогаем, чтобы параллельные читатели
-- продолжали работать без ошибок. Колонки nullable и без DEFAULT,
-- поэтому ALTER не переписывает таблицу.
ALTER TABLE customers ADD COLUMN first_name VARCHAR(255);
ALTER TABLE customers ADD COLUMN last_name VARCHAR(255);
