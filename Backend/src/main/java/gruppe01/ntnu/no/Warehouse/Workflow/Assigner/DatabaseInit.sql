CREATE DATABASE IF NOT EXISTS warehouse;
USE warehouse;

CREATE TABLE IF NOT EXISTS worker
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    zone_id       BIGINT,
    work_title    VARCHAR(255) NOT NULL,
    effectiveness DOUBLE       NOT NULL,
    availability  BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS license
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS worker_license
(
    worker_id  BIGINT NOT NULL,
    license_id BIGINT NOT NULL,
    FOREIGN KEY (worker_id) REFERENCES worker (id),
    FOREIGN KEY (license_id) REFERENCES license (id)
);

CREATE TABLE IF NOT EXISTS zone
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL ,
    name        VARCHAR(255) NOT NULL,
    capacity    INT          NOT NULL
);

CREATE TABLE IF NOT EXISTS task
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    min_duration INT          NOT NULL,
    max_duration INT          NOT NULL,
    min_workers  INT          NOT NULL,
    max_workers  INT          NOT NULL,
    zone_id      BIGINT,
    FOREIGN KEY (zone_id) REFERENCES zone (id)
);

CREATE TABLE IF NOT EXISTS active_task
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id    BIGINT NOT NULL,
    strict_start BOOLEAN DEFAULT FALSE,
    date       DATE NOT NULL,
    due_date   TIMESTAMP,
    start_time TIMESTAMP,
    end_time   TIMESTAMP,
    eta        DATETIME(6) DEFAULT NULL,
    FOREIGN KEY (task_id) REFERENCES task (id)
);

CREATE TABLE IF NOT EXISTS active_task_worker
(
    active_task_id BIGINT NOT NULL,
    worker_id      BIGINT NOT NULL,
    FOREIGN KEY (active_task_id) REFERENCES active_task (id),
    FOREIGN KEY (worker_id) REFERENCES worker (id)
);

CREATE TABLE IF NOT EXISTS task_license
(
    task_id    BIGINT NOT NULL,
    license_id BIGINT NOT NULL,
    FOREIGN KEY (task_id) REFERENCES task (id),
    FOREIGN KEY (license_id) REFERENCES license (id)
);

CREATE TABLE IF NOT EXISTS zone_worker
(
    zone_id   BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    FOREIGN KEY (zone_id) REFERENCES zone (id),
    FOREIGN KEY (worker_id) REFERENCES worker (id)
);

INSERT INTO worker (name, zone_id, work_title, effectiveness, availability)
VALUES ('John Doe', 1, 'Warehouse Manager', 1, true),
       ('Jane Smith', 1, 'Warehouse Supervisor', 1, true),
       ('Alice Johnson', 2, 'Warehouse Technician', 1, true),
       ('Bob Brown', 2, 'Forklift Operator', 1, false),
       ('Charlie Davis', 3, 'Warehouse Engineer', 1, true),
       ('Diana Evans', 3, 'Inventory Clerk', 1, true),
       ('Eve Foster', 4, 'Logistics Analyst', 1, true),
       ('Frank Green', 4, 'Shipping Coordinator', 1, true),
       ('Grace Harris', 5, 'Quality Inspector', 1, false),
       ('Hank Irving', 5, 'Warehouse Specialist', 1, true),
       ('Ivy Johnson', 6, 'Warehouse Consultant', 1, true),
       ('Jack King', 6, 'Warehouse Planner', 1, true),
       ('Karen Lee', 7, 'Warehouse Designer', 1, true),
       ('Leo Martin', 7, 'Warehouse Developer', 1, false),
       ('Mona Nelson', 7, 'Warehouse Architect', 1, true),
       ('Vincent Holiday', 0, 'Powerhouse Specialist', 1, false),
       ('Gerrard Paul', 0, 'Truck Mechanic', 1, true),
       ('Nancy White', 1, 'Warehouse Clerk', 1, true),
       ('Oscar Black', 2, 'Warehouse Operator', 1, true),
       ('Paul Brown', 3, 'Warehouse Assistant', 1, true),
       ('Quincy Green', 4, 'Warehouse Coordinator', 1, true),
       ('Rachel Blue', 5, 'Warehouse Manager', 1, true),
       ('Steve Red', 6, 'Warehouse Supervisor', 1, true),
       ('Tina Yellow', 7, 'Warehouse Technician', 1, true),
       ('Uma Orange', 1, 'Warehouse Engineer', 1, true),
       ('Victor Purple', 2, 'Warehouse Specialist', 1, true),
       ('Wendy Pink', 3, 'Warehouse Analyst', 1, true),
       ('Xander Gray', 4, 'Warehouse Planner', 1, true),
       ('Yara Cyan', 5, 'Warehouse Developer', 1, true),
       ('Zane Magenta', 6, 'Warehouse Architect', 1, true),
       ('Adam Silver', 7, 'Warehouse Designer', 1, true),
       ('Bella Gold', 1, 'Warehouse Clerk', 1, true),
       ('Carl Bronze', 2, 'Warehouse Operator', 1, true),
       ('Diana Copper', 3, 'Warehouse Assistant', 1, true),
       ('Ethan Iron', 4, 'Warehouse Coordinator', 1, true),
       ('Fiona Steel', 5, 'Warehouse Manager', 1, true),
       ('George Nickel', 6, 'Warehouse Supervisor', 1, true),
       ('Holly Zinc', 7, 'Warehouse Technician', 1, true),
       ('Ian Lead', 1, 'Warehouse Engineer', 1, true),
       ('Judy Tin', 2, 'Warehouse Specialist', 1, true);

INSERT INTO zone (name, capacity)
VALUES ('Receiving', 10),
       ('Storage', 15),
       ('Picking', 5),
       ('Packing', 7),
       ('Shipping', 8),
       ('Quality Control', 4),
       ('Planning', 6),
       ('Execution', 14),
       ('Monitoring', 2);

INSERT INTO license (name)
VALUES ('Truck License'),
       ('Forklift License'),
       ('Safety Training'),
       ('First Aid Certification');

-- Assuming worker IDs are from 1 to 15 and license IDs are from 1 to 4
INSERT INTO worker_license (worker_id, license_id)
VALUES (1, 1), (1, 2), (1, 3), (1, 4),
       (2, 1), (2, 2), (2, 3), (2, 4),
       (3, 1), (3, 2), (3, 3), (3, 4),
       (4, 1), (4, 2), (4, 3), (4, 4),
       (5, 1), (5, 2), (5, 3), (5, 4),
       (6, 1), (6, 2), (6, 3), (6, 4),
       (7, 1), (7, 2), (7, 3), (7, 4),
       (8, 1), (8, 2), (8, 3), (8, 4),
       (9, 1), (9, 2), (9, 3), (9, 4),
       (10, 1), (10, 2), (10, 3), (10, 4),
       (11, 1), (11, 2), (11, 3), (11, 4),
       (12, 1), (12, 2), (12, 3), (12, 4),
       (13, 1), (13, 2), (13, 3), (13, 4),
       (14, 1), (14, 2), (14, 3), (14, 4),
       (15, 1), (15, 2), (15, 3), (15, 4),
       (16, 1), (16, 2), (16, 3), (16, 4),
         (17, 1), (17, 2), (17, 3), (17, 4),
         (18, 1), (18, 2), (18, 3), (18, 4),
         (19, 1), (19, 2), (19, 3), (19, 4),
         (20, 1), (20, 2), (20, 3), (20, 4),
         (21, 1), (21, 2), (21, 3), (21, 4),
         (22, 1), (22, 2), (22, 3), (22, 4),
         (23, 1), (23, 2), (23, 3), (23, 4),
         (24, 1), (24, 2), (24, 3), (24, 4),
         (25, 1), (25, 2), (25, 3), (25, 4),
         (26, 1), (26, 2), (26, 3), (26, 4),
         (27, 1), (27, 2), (27, 3), (27, 4),
         (28, 1), (28, 2), (28, 3), (28, 4),
         (29, 1), (29, 2), (29, 3), (29, 4),
         (30, 1), (30, 2), (30, 3), (30, 4),
         (31, 1), (31, 2), (31, 3), (31, 4),
         (32, 1), (32, 2), (32, 3), (32, 4),
         (33, 1), (33, 2), (33, 3), (33, 4),
         (34, 1), (34, 2), (34, 3), (34, 4);

-- Assuming zone IDs are from 1 to 6
INSERT INTO zone_worker (zone_id, worker_id)
VALUES (1, 1),
       (1, 2),
       (1, 13),
       (2, 3),
       (2, 4),
       (2, 14),
       (3, 5),
       (3, 6),
       (3, 15),
       (4, 7),
       (4, 8),
       (5, 9),
       (5, 10),
       (5, 11),
       (6, 12);


INSERT INTO task (name, description, min_duration, max_duration, min_workers, max_workers, zone_id)
VALUES ('Inventory Check', 'Check the inventory levels in the warehouse', 10, 40, 1, 2, 1),
       ('Restock Shelves', 'Restock the shelves with new inventory', 5, 30, 1, 3, 1),
       ('Order Processing', 'Process customer orders for shipment', 5, 25, 2, 4, 1),
       ('Quality Inspection', 'Inspect the quality of incoming goods', 20, 40, 1, 2, 2),
       ('Package Orders', 'Package customer orders for delivery', 10, 29, 1, 2, 2),
       ('Load Trucks', 'Load trucks with outgoing shipments', 20, 60, 2, 3, 3),
       ('Unload Trucks', 'Unload trucks with incoming shipments', 20, 60, 2, 3, 3),
       ('Cycle Counting', 'Perform cycle counting of inventory', 1, 10, 1, 1, 4),
       ('Label Products', 'Label products with barcodes', 10, 30, 1, 2, 4),
       ('Warehouse Cleaning', 'Clean and organize the warehouse', 20, 40, 1, 2, 5),
       ('Safety Inspection', 'Inspect the warehouse for safety compliance', 15, 30, 1, 2, 5),
       ('Plan Shipments', 'Plan the shipments for the day', 10, 20, 1, 1, 6),
       ('Monitor Inventory', 'Monitor inventory levels and report discrepancies', 10, 30, 1, 2, 6),
       ('Schedule Maintenance', 'Schedule maintenance for warehouse equipment', 15, 45, 1, 2, 7),
       ('Update Records', 'Update warehouse records and logs', 5, 20, 1, 1, 7),
       ('Coordinate Teams', 'Coordinate teams for various tasks', 10, 25, 1, 2, 8),
       ('Review Performance', 'Review the performance of warehouse operations', 20, 40, 1, 2, 8);

INSERT INTO task_license (task_id, license_id)
VALUES (1, 3),
       (2, 3),
       (3, 1),
       (3, 2),
       (4, 3),
       (5, 3),
       (6, 1),
       (7, 1),
       (8, 3),
       (9, 3),
       (10, 3),
       (11, 3),
       (12, 3),
       (13, 3),
       (14, 3),
       (15, 3),
       (17, 3);

INSERT INTO active_task (task_id, date, due_date, start_time, end_time)
VALUES
    (1, '2023-12-02', '2023-12-01 17:00:00', '2023-12-01 09:00:00', '2023-12-01 17:00:00'),
    (2, '2023-12-02', '2023-12-02 18:00:00', '2023-12-02 10:00:00', '2023-12-02 18:00:00'),
    (3, '2023-12-03', '2023-12-03 19:00:00', '2023-12-03 11:00:00', '2023-12-03 19:00:00'),
    (4, '2023-12-04', '2023-12-04 20:00:00', '2023-12-04 12:00:00', '2023-12-04 20:00:00'),
    (5, '2023-12-05', '2023-12-05 21:00:00', '2023-12-05 13:00:00', '2023-12-05 21:00:00'),
    (6, '2023-12-06', '2023-12-06 22:00:00', '2023-12-06 14:00:00', '2023-12-06 22:00:00'),
    (7, '2023-12-07', '2023-12-07 23:00:00', '2023-12-07 15:00:00', '2023-12-07 23:00:00'),
    (8, '2023-12-08', '2023-12-08 00:00:00', '2023-12-08 16:00:00', '2023-12-08 00:00:00'),
    (9, '2023-12-09', '2023-12-09 01:00:00', '2023-12-09 17:00:00', '2023-12-09 01:00:00'),
    (10, '2023-12-10', '2023-12-10 02:00:00', '2023-12-10 18:00:00', '2023-12-10 02:00:00'),
    (11, '2023-12-11', '2023-12-11 03:00:00', '2023-12-11 19:00:00', '2023-12-11 03:00:00'),
    (12, '2023-12-12', '2023-12-12 04:00:00', '2023-12-12 20:00:00', '2023-12-12 04:00:00'),
    (13, '2023-12-13', '2023-12-13 05:00:00', '2023-12-13 21:00:00', '2023-12-13 05:00:00'),
    (14, '2023-12-14', '2023-12-14 06:00:00', '2023-12-14 22:00:00', '2023-12-14 06:00:00'),
    (15, '2023-12-15', '2023-12-15 07:00:00', '2023-12-15 23:00:00', '2023-12-15 07:00:00'),
    (16, '2023-12-16', '2023-12-16 08:00:00', '2023-12-16 00:00:00', '2023-12-16 08:00:00'),
    (17, '2023-12-17', '2023-12-17 09:00:00', '2023-12-17 01:00:00', '2023-12-17 09:00:00');

INSERT INTO active_task_worker (active_task_id, worker_id)
VALUES (1, 1),
       (1, 2),
       (2, 3),
       (2, 4),
       (3, 5),
       (3, 6),
       (4, 7),
       (4, 8),
       (5, 9),
       (5, 10),
       (6, 11),
       (6, 12),
       (7, 13),
       (7, 14),
       (8, 15),
       (8, 16),
       (9, 17),
       (9, 18),
       (10, 19),
       (11, 21),
       (11, 22),
       (12, 23),
       (12, 24),
       (13, 25),
       (13, 26),
       (14, 27),
       (14, 28),
       (15, 29),
       (15, 30),
       (16, 31),
       (16, 32),
       (17, 33),
       (17, 34);
