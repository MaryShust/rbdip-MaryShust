-- ЛР3 (backfill): заполняем first_name/last_name для уже существующих
-- клиентов, продолжая писать и в старую колонку (full_name не меняется).
-- Конвенция разбора: первый токен full_name - имя, остаток - фамилия.
UPDATE customers
SET first_name = CASE
        WHEN position(' ' in full_name) > 0
            THEN trim(substring(full_name from 1 for position(' ' in full_name) - 1))
        ELSE trim(full_name)
    END,
    last_name = CASE
        WHEN position(' ' in full_name) > 0
            THEN trim(substring(full_name from position(' ' in full_name) + 1))
        ELSE ''
    END
WHERE full_name IS NOT NULL
  AND (first_name IS NULL OR last_name IS NULL);
