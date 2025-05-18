USE warehouse;

SELECT * FROM active_task at;

EXPLAIN ANALYZE
SELECT *
FROM active_task a
         JOIN task t ON a.task_id = t.id
         JOIN zone z ON t.zone_id = z.id
WHERE a.date = 20250521
  AND a.end_time IS NULL
  AND z.id = 1;

SELECT *
FROM active_task
WHERE date = 20250521;

SELECT *
FROM active_task
WHERE end_time IS NOT NULL;

SELECT *
FROM active_task a
         JOIN task t ON a.task_id = t.id
WHERE a.end_time IS NULL
  AND a.date = 20250521
  AND (1 = 0 OR t.zone_id = 1);

SELECT a.*
FROM active_task a
         JOIN task t ON a.task_id = t.id
WHERE a.end_time IS NULL
  AND a.date = 20250521
  AND (1 = 0 OR t.zone_id = 1);

SELECT *
FROM active_task
WHERE start_time IS NULL
  AND end_time IS NULL;

SELECT *
FROM active_task
WHERE start_time IS NOT NULL
  AND end_time IS NULL;

SELECT COUNT(*)
FROM active_task a
         JOIN task t ON a.task_id = t.id
WHERE a.date = 20250521
  AND a.end_time IS NOT NULL
  AND t.zone_id = 1;

SELECT at.*
FROM active_task at
         JOIN task t ON at.task_id = t.id
WHERE t.zone_id = 1;

SELECT at.*
FROM active_task at
         JOIN task t ON at.task_id = t.id
WHERE t.zone_id = 1
  AND at.date = 20250521
  AND at.end_time IS NULL;

SELECT COUNT(*)
FROM active_task at
         JOIN task t ON at.task_id = t.id
WHERE at.date = 20250521
  AND (1 = 0 OR t.zone_id = 1);

SELECT a.*
FROM active_task a
         JOIN task t ON a.task_id = t.id
WHERE a.date = 20250521
  AND t.zone_id = 1
  AND a.end_time IS NULL;

SELECT a.*
FROM active_task a
         JOIN task t ON a.task_id = t.id
WHERE a.date = 20250521
  AND (1 = 0 OR t.zone_id = 1);

SELECT p.*
FROM picker_task p
         JOIN zone z ON p.zone_id = z.id
WHERE z.id = 7;

SELECT COUNT(*)
FROM picker_task p
         JOIN zone z ON p.zone_id = z.id
WHERE p.date = 20250521
  AND p.end_time IS NOT NULL
  AND z.id = 7;

SELECT p.*
FROM picker_task p
         JOIN zone z ON p.zone_id = z.id
WHERE p.date = 20250521
  AND p.end_time IS NULL
  AND (7 = 0 OR z.id = 7);

SELECT COALESCE(SUM(p.pack_amount), 0)
FROM picker_task p
         JOIN zone z ON p.zone_id = z.id
WHERE p.date = 20250521
  AND p.end_time IS NOT NULL
  AND z.id = 7;

SELECT *
FROM picker_task
WHERE date = 20250521;

SELECT *
FROM picker_task
WHERE date = 20250521
  AND end_time IS NULL;

SELECT pt.*
FROM picker_task pt
         JOIN zone z ON pt.zone_id = z.id
WHERE z.id = 7
  AND pt.date = 20250521
  AND pt.end_time IS NULL;

SELECT COUNT(*)
FROM picker_task pt
         JOIN zone z ON pt.zone_id = z.id
WHERE pt.date = 20250521
  AND (7 = 0 OR z.id = 7);

SELECT pt.*
FROM picker_task pt
         JOIN zone z ON pt.zone_id = z.id
WHERE z.id = 7
  AND pt.time_s > 0
  AND pt.date > 20250518;

SELECT p.*
FROM picker_task p
         JOIN zone z ON p.zone_id = z.id
WHERE p.date = 20250521
  AND z.id = 7
  AND p.end_time IS NULL;

SELECT p.*
FROM picker_task p
         JOIN zone z ON p.zone_id = z.id
WHERE p.date = 20250521
  AND (7 = 0 OR z.id = 7);