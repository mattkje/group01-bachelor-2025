CREATE DATABASE IF NOT EXISTS warehouse;
USE warehouse;

CREATE TABLE IF NOT EXISTS zone
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL ,
    name        VARCHAR(255) NOT NULL,
    is_picker_zone BOOLEAN DEFAULT FALSE,
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
    strict_start TIMESTAMP DEFAULT NULL,
    date       DATE NOT NULL,
    due_date   TIMESTAMP,
    start_time TIMESTAMP,
    end_time   TIMESTAMP,
    recurrence_type INT DEFAULT 0,
    eta        DATETIME(6) DEFAULT NULL,
    FOREIGN KEY (task_id) REFERENCES task (id)
);

CREATE TABLE IF NOT EXISTS worker
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    zone_id       BIGINT,
    work_title    VARCHAR(255) NOT NULL,
    efficiency    DOUBLE       NOT NULL,
    availability  BOOLEAN DEFAULT TRUE,
    break_start_time TIMESTAMP,
    dead          BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS worker_schedule (
    worker_id BIGINT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    PRIMARY KEY (worker_id, day_of_week),
    FOREIGN KEY (worker_id) REFERENCES worker (id)
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

CREATE TABLE IF NOT EXISTS timetable
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id  BIGINT NOT NULL,
    start_time TIMESTAMP,
    end_time   TIMESTAMP,
    real_start_time TIMESTAMP,
    real_end_time TIMESTAMP,
    FOREIGN KEY (worker_id) REFERENCES worker (id)
);

CREATE TABLE IF NOT EXISTS picker_task
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    distance       DOUBLE,
    pack_amount    INT,
    lines_amount   INT,
    weight_g       INT,
    volume_ml      INT,
    avg_height_m   INT,
    time_s         DOUBLE DEFAULT NULL,
    start_time    TIMESTAMP,
    end_time      TIMESTAMP,
    date           DATE,
    zone_id        BIGINT NOT NULL,
    worker_id      BIGINT,
    FOREIGN KEY (worker_id) REFERENCES worker (id),
    FOREIGN KEY (zone_id) REFERENCES zone (id)
);

CREATE TABLE IF NOT EXISTS world_sim_data
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    time            TIMESTAMP,
    completed_tasks INT,
    items_picked    INT,
    real_data       BOOLEAN,
    zone_id         BIGINT NOT NULL,
    FOREIGN KEY (zone_id) REFERENCES zone (id)
);

CREATE TABLE IF NOT EXISTS monte_carlo_data
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    sim_no          INT,
    time            TIMESTAMP,
    completed_tasks INT,
    items_picked    INT,
    zone_id         BIGINT NOT NULL,
    FOREIGN KEY (zone_id) REFERENCES zone (id)
);

CREATE TRIGGER update_worker_zone
    BEFORE INSERT ON active_task_worker
    FOR EACH ROW
BEGIN
    DECLARE task_zone INT;

    SELECT zone_id INTO task_zone FROM task WHERE id = (SELECT task_id FROM active_task WHERE id = NEW.active_task_id);

    UPDATE worker SET zone_id = task_zone WHERE id = NEW.worker_id;
END;


INSERT INTO worker (id, name, zone_id, work_title, efficiency, availability)
VALUES
    (40,'John Doe', 1, 'Warehouse Manager', 1.1, true),
    (1, 'Jane Smith', 1, 'Warehouse Supervisor', 0.9, true),
    (3,'Alice Johnson', 2, 'Warehouse Technician', 1.2, true),
    (2,'Bob Brown', 2, 'Forklift Operator', 0.8, false),
    (4,'Charlie Davis', 3, 'Warehouse Engineer', 1.3, true),
    (5,'Diana Evans', 3, 'Inventory Clerk', 1.0, true),
    (7,'Eve Foster', 4, 'Logistics Analyst', 0.95, true),
    (6,'Frank Green', 4, 'Shipping Coordinator', 1.25, true),
    (9,'Grace Harris', 5, 'Quality Inspector', 0.85, false),
    (8,'Hank Irving', 5, 'Warehouse Specialist', 1.15, true),
    (11,'Ivy Johnson', 6, 'Warehouse Consultant', 1.05, true),
    (10,'Jack King', 6, 'Warehouse Planner', 1.2, true),
    (13,'Karen Lee', 7, 'Warehouse Designer', 0.75, true),
    (12,'Leo Martin', 7, 'Warehouse Developer', 1.3, false),
    (15,'Mona Nelson', 7, 'Warehouse Architect', 1.0, true),
    (16,'Vincent Holiday', 0, 'Powerhouse Specialist', 0, false),
    (14,'Gerrard Paul', 0, 'Truck Mechanic', 1.05, true),
    (17,'Nancy White', 1, 'Warehouse Clerk', 1.1, true),
    (18,'Oscar Black', 2, 'Warehouse Operator', 0.95, true),
    (20,'Paul Brown', 3, 'Warehouse Assistant', 1.25, true),
    (19,'Quincy Green', 4, 'Warehouse Coordinator', 0.9, true),
    (21,'Rachel Blue', 5, 'Warehouse Manager', 1.2, true),
    (22,'Steve Red', 6, 'Warehouse Supervisor', 1.15, true),
    (23,'Tina Yellow', 7, 'Warehouse Technician', 1.0, true),
    (25,'Uma Orange', 1, 'Warehouse Engineer', 0.85, true),
    (24,'Victor Purple', 2, 'Warehouse Specialist', 1.3, true),
    (27,'Wendy Pink', 3, 'Warehouse Analyst', 1.05, true),
    (26,'Xander Gray', 4, 'Warehouse Planner', 0.75, true),
    (29,'Yara Cyan', 5, 'Warehouse Developer', 1.1, true),
    (28,'Zane Magenta', 6, 'Warehouse Architect', 0.9, true),
    (30,'Adam Silver', 7, 'Warehouse Designer', 1.2, true),
    (31,'Bella Gold', 1, 'Warehouse Clerk', 1.15, true),
    (32,'Carl Bronze', 2, 'Warehouse Operator', 1.05, true),
    (33,'Diana Copper', 3, 'Warehouse Assistant', 0.7, true),
    (34,'Ethan Iron', 4, 'Warehouse Coordinator', 1.0, true),
    (35,'Fiona Steel', 5, 'Warehouse Manager', 1.3, true),
    (36,'George Nickel', 6, 'Warehouse Supervisor', 0.95, true),
    (37,'Holly Zinc', 7, 'Warehouse Technician', 0.85, true),
    (38,'Ian Lead', 1, 'Warehouse Engineer', 1.2, true),
    (39,'Judy Tin', 2, 'Warehouse Specialist', 1.1, true),
    (42, 'Oliver Stone', 10, 'Warehouse Operator', 1.1, true),
    (43, 'Emma Watson', 10, 'Inventory Specialist', 1.2, true),
    (44, 'Liam Brown', 11, 'Logistics Coordinator', 0.95, true),
    (45, 'Sophia Green', 11, 'Shipping Manager', 1.05, true),
    (46, 'Noah White', 12, 'Quality Control', 1.0, true);

INSERT INTO worker_schedule (worker_id, day_of_week, start_time, end_time)
VALUES
    -- Worker 1 works every day
    (1, 'MONDAY', '08:00:00', '16:00:00'),
    (1, 'TUESDAY', '08:00:00', '16:00:00'),
    (1, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (1, 'THURSDAY', '08:00:00', '16:00:00'),
    (1, 'FRIDAY', '08:00:00', '16:00:00'),
    (1, 'SATURDAY', '08:00:00', '12:00:00'),
    (1, 'SUNDAY', '08:00:00', '12:00:00'),

    -- Worker 2 works only on weekdays
    (2, 'MONDAY', '09:00:00', '17:00:00'),
    (2, 'TUESDAY', '09:00:00', '17:00:00'),
    (2, 'WEDNESDAY', '09:00:00', '17:00:00'),
    (2, 'THURSDAY', '09:00:00', '17:00:00'),
    (2, 'FRIDAY', '09:00:00', '17:00:00'),

    -- Worker 3 works only on weekends
    (3, 'SATURDAY', '10:00:00', '14:00:00'),
    (3, 'SUNDAY', '10:00:00', '14:00:00'),

    -- Worker 4 works on Monday, Wednesday, and Friday
    (4, 'MONDAY', '07:00:00', '15:00:00'),
    (4, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (4, 'FRIDAY', '07:00:00', '15:00:00'),

    -- Worker 5 works on Tuesday and Thursday
    (5, 'TUESDAY', '06:00:00', '14:00:00'),
    (5, 'THURSDAY', '06:00:00', '14:00:00'),

    -- Worker 6 works every day except Sunday
    (6, 'MONDAY', '08:00:00', '16:00:00'),
    (6, 'TUESDAY', '08:00:00', '16:00:00'),
    (6, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (6, 'THURSDAY', '08:00:00', '16:00:00'),
    (6, 'FRIDAY', '08:00:00', '16:00:00'),
    (6, 'SATURDAY', '08:00:00', '12:00:00'),

    (7, 'MONDAY', '08:00:00', '16:00:00'),
    (7, 'TUESDAY', '08:00:00', '16:00:00'),
    (7, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (7, 'THURSDAY', '08:00:00', '16:00:00'),
    (7, 'FRIDAY', '08:00:00', '16:00:00'),
    (7, 'SATURDAY', '08:00:00', '12:00:00'),
    (7, 'SUNDAY', '08:00:00', '12:00:00'),

    -- Worker 8 works only on weekends
    (8, 'SATURDAY', '10:00:00', '14:00:00'),
    (8, 'SUNDAY', '10:00:00', '14:00:00'),

    -- Worker 9 works on Monday, Wednesday, and Friday
    (9, 'MONDAY', '07:00:00', '15:00:00'),
    (9, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (9, 'FRIDAY', '07:00:00', '15:00:00'),

    -- Worker 10 works on Tuesday and Thursday
    (10, 'TUESDAY', '06:00:00', '14:00:00'),
    (10, 'THURSDAY', '06:00:00', '14:00:00'),

    -- Worker 11 works every day except Sunday
    (11, 'MONDAY', '08:00:00', '16:00:00'),
    (11, 'TUESDAY', '08:00:00', '16:00:00'),
    (11, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (11, 'THURSDAY', '08:00:00', '16:00:00'),
    (11, 'FRIDAY', '08:00:00', '16:00:00'),
    (11, 'SATURDAY', '08:00:00', '12:00:00'),

    -- Worker 12 works only on Monday and Friday
    (12, 'MONDAY', '09:00:00', '17:00:00'),
    (12, 'FRIDAY', '09:00:00', '17:00:00'),

    -- Worker 13 works every day
    (13, 'MONDAY', '08:00:00', '16:00:00'),
    (13, 'TUESDAY', '08:00:00', '16:00:00'),
    (13, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (13, 'THURSDAY', '08:00:00', '16:00:00'),
    (13, 'FRIDAY', '08:00:00', '16:00:00'),
    (13, 'SATURDAY', '08:00:00', '12:00:00'),
    (13, 'SUNDAY', '08:00:00', '12:00:00'),

    -- Worker 14 works only on weekends
    (14, 'SATURDAY', '10:00:00', '14:00:00'),
    (14, 'SUNDAY', '10:00:00', '14:00:00'),

    -- Worker 15 works on Monday, Wednesday, and Friday
    (15, 'MONDAY', '07:00:00', '15:00:00'),
    (15, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (15, 'FRIDAY', '07:00:00', '15:00:00'),

    (17, 'MONDAY', '09:00:00', '15:00:00'),
    (17, 'WEDNESDAY', '09:00:00', '15:00:00'),

    (18, 'SATURDAY', '10:00:00', '14:00:00'),
    (18, 'SUNDAY', '10:00:00', '14:00:00'),

    (20, 'MONDAY', '08:00:00', '16:00:00'),
    (20, 'TUESDAY', '08:00:00', '16:00:00'),
    (20, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (20, 'THURSDAY', '08:00:00', '16:00:00'),
    (20, 'FRIDAY', '08:00:00', '16:00:00'),
    (20, 'SATURDAY', '08:00:00', '12:00:00'),

    (21, 'TUESDAY', '07:00:00', '13:00:00'),
    (21, 'THURSDAY', '07:00:00', '13:00:00'),

    (22, 'MONDAY', '08:00:00', '16:00:00'),
    (22, 'TUESDAY', '08:00:00', '16:00:00'),
    (22, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (22, 'THURSDAY', '08:00:00', '16:00:00'),
    (22, 'FRIDAY', '08:00:00', '16:00:00'),
    (22, 'SATURDAY', '08:00:00', '12:00:00'),
    (22, 'SUNDAY', '08:00:00', '12:00:00'),

    (23, 'FRIDAY', '09:00:00', '17:00:00'),

    (25, 'MONDAY', '07:00:00', '15:00:00'),
    (25, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (25, 'FRIDAY', '07:00:00', '15:00:00'),

    (27, 'MONDAY', '08:00:00', '14:00:00'),
    (27, 'FRIDAY', '08:00:00', '14:00:00'),

    (28, 'MONDAY', '09:00:00', '17:00:00'),
    (28, 'TUESDAY', '09:00:00', '17:00:00'),
    (28, 'WEDNESDAY', '09:00:00', '17:00:00'),
    (28, 'THURSDAY', '09:00:00', '17:00:00'),
    (28, 'FRIDAY', '09:00:00', '17:00:00'),
    (28, 'SUNDAY', '10:00:00', '14:00:00'),

    (29, 'SATURDAY', '10:00:00', '16:00:00'),
    (29, 'SUNDAY', '10:00:00', '16:00:00'),

    (30, 'TUESDAY', '07:00:00', '15:00:00'),
    (30, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (30, 'THURSDAY', '07:00:00', '15:00:00'),

    (31, 'MONDAY', '08:00:00', '16:00:00'),
    (31, 'TUESDAY', '08:00:00', '16:00:00'),
    (31, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (31, 'THURSDAY', '08:00:00', '16:00:00'),
    (31, 'FRIDAY', '08:00:00', '16:00:00'),
    (31, 'SATURDAY', '08:00:00', '12:00:00'),
    (31, 'SUNDAY', '08:00:00', '12:00:00'),

    (33, 'MONDAY', '06:00:00', '14:00:00'),
    (33, 'WEDNESDAY', '06:00:00', '14:00:00'),
    (33, 'FRIDAY', '06:00:00', '14:00:00'),

    (34, 'THURSDAY', '09:00:00', '17:00:00'),

    (35, 'MONDAY', '08:00:00', '16:00:00'),
    (35, 'TUESDAY', '08:00:00', '16:00:00'),
    (35, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (35, 'THURSDAY', '08:00:00', '16:00:00'),
    (35, 'FRIDAY', '08:00:00', '16:00:00'),
    (35, 'SATURDAY', '08:00:00', '12:00:00'),

    (36, 'MONDAY', '09:00:00', '15:00:00'),
    (36, 'THURSDAY', '09:00:00', '15:00:00'),

    (37, 'SATURDAY', '10:00:00', '14:00:00'),
    (37, 'SUNDAY', '10:00:00', '14:00:00'),

    (38, 'MONDAY', '08:00:00', '16:00:00'),
    (38, 'TUESDAY', '08:00:00', '16:00:00'),
    (38, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (38, 'THURSDAY', '08:00:00', '16:00:00'),
    (38, 'SATURDAY', '08:00:00', '12:00:00'),
    (38, 'SUNDAY', '08:00:00', '12:00:00'),

    (39, 'TUESDAY', '07:00:00', '13:00:00'),
    (39, 'FRIDAY', '07:00:00', '13:00:00'),

    (40, 'MONDAY', '08:00:00', '16:00:00'),
    (40, 'TUESDAY', '08:00:00', '16:00:00'),
    (40, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (40, 'THURSDAY', '08:00:00', '16:00:00'),
    (40, 'FRIDAY', '08:00:00', '16:00:00'),
    (40, 'SATURDAY', '08:00:00', '12:00:00'),
    (40, 'SUNDAY', '08:00:00', '12:00:00'),

    (46, 'MONDAY', '06:00:00', '14:00:00'),
    (46, 'WEDNESDAY', '06:00:00', '14:00:00'),
    (46, 'SATURDAY', '06:00:00', '14:00:00'),

    (42, 'THURSDAY', '09:00:00', '17:00:00'),

    (43, 'MONDAY', '08:00:00', '16:00:00'),
    (43, 'TUESDAY', '08:00:00', '16:00:00'),
    (43, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (43, 'THURSDAY', '08:00:00', '16:00:00'),
    (43, 'FRIDAY', '08:00:00', '16:00:00'),
    (43, 'SATURDAY', '08:00:00', '12:00:00'),

    (44, 'TUESDAY', '07:00:00', '13:00:00'),
    (44, 'SATURDAY', '07:00:00', '13:00:00'),

    (45, 'MONDAY', '08:00:00', '16:00:00'),
    (45, 'TUESDAY', '08:00:00', '16:00:00'),
    (45, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (45, 'THURSDAY', '08:00:00', '16:00:00'),
    (45, 'FRIDAY', '08:00:00', '16:00:00'),
    (45, 'SATURDAY', '08:00:00', '12:00:00'),
    (45, 'SUNDAY', '08:00:00', '12:00:00');


INSERT INTO zone (name, capacity, is_picker_zone)
VALUES ('Receiving', 10, false),
       ('Storage', 15, false),
       ('Picking', 5, false),
       ('Packing', 7, false),
       ('Shipping', 8, false),
       ('Quality Control', 4, false),
       ('Planning', 6, false),
       ('Execution', 14, false),
       ('Monitoring', 2, false),
       ('Dry', 10, true),
       ('Fruit', 15, true),
       ('Freeze', 8, true);

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
VALUES ('Inventory Check', 'Check the inventory levels in the warehouse', 30, 90, 1, 2, 1),
       ('Restock Shelves', 'Restock the shelves with new inventory', 20, 75, 1, 3, 1),
       ('Order Processing', 'Process customer orders for shipment', 20, 60, 2, 4, 1),
       ('Quality Inspection', 'Inspect the quality of incoming goods', 45, 120, 1, 2, 2),
       ('Package Orders', 'Package customer orders for delivery', 30, 90, 1, 2, 2),
       ('Load Trucks', 'Load trucks with outgoing shipments', 60, 180, 2, 3, 3),
       ('Unload Trucks', 'Unload trucks with incoming shipments', 60, 180, 2, 3, 3),
       ('Cycle Counting', 'Perform cycle counting of inventory', 15, 45, 1, 1, 4),
       ('Label Products', 'Label products with barcodes', 30, 90, 1, 2, 4),
       ('Warehouse Cleaning', 'Clean and organize the warehouse', 45, 120, 1, 2, 5),
       ('Safety Inspection', 'Inspect the warehouse for safety compliance', 40, 90, 1, 2, 5),
       ('Plan Shipments', 'Plan the shipments for the day', 30, 60, 1, 1, 6),
       ('Monitor Inventory', 'Monitor inventory levels and report discrepancies', 30, 90, 1, 2, 6),
       ('Schedule Maintenance', 'Schedule maintenance for warehouse equipment', 45, 120, 1, 2, 7),
       ('Update Records', 'Update warehouse records and logs', 20, 60, 1, 1, 7),
       ('Coordinate Teams', 'Coordinate teams for various tasks', 40, 90, 1, 2, 8),
       ('Review Performance', 'Review the performance of warehouse operations', 60, 150, 1, 2, 8),
       ('System Monitoring', 'Monitor the warehouse management system for any issues', 60, 180, 1, 2, 9),
       ('Security Check', 'Perform security checks and monitor surveillance systems', 90, 240, 1, 2, 9);



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
       (17, 3),
         (18, 3),
         (19, 3);


