CREATE DATABASE IF NOT EXISTS warehouse;
USE warehouse;

CREATE TABLE IF NOT EXISTS zone
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    name           VARCHAR(255)                      NOT NULL,
    is_picker_zone BOOLEAN DEFAULT FALSE,
    capacity       INT                               NOT NULL
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
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id         BIGINT NOT NULL,
    date            DATE   NOT NULL,
    due_date        TIMESTAMP,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    mc_start_time   TIMESTAMP,
    mc_end_time     TIMESTAMP,
    recurrence_type INT         DEFAULT 0,
    eta             DATETIME(6) DEFAULT NULL,
    FOREIGN KEY (task_id) REFERENCES task (id)
);

CREATE TABLE IF NOT EXISTS notifications
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    message LONGTEXT NOT NULL,
    zone_id BIGINT,
    time    TIMESTAMP,
    FOREIGN KEY (zone_id) REFERENCES zone (id)
);

CREATE TABLE IF NOT EXISTS worker
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    zone_id      BIGINT,
    work_title   VARCHAR(255) NOT NULL,
    efficiency   DOUBLE       NOT NULL,
    availability BOOLEAN DEFAULT TRUE,
    dead         BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS worker_schedule
(
    worker_id   BIGINT      NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    start_time  TIME        NOT NULL,
    end_time    TIME        NOT NULL,
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

CREATE TABLE IF NOT EXISTS timetable
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id       BIGINT NOT NULL,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    real_start_time TIMESTAMP,
    real_end_time   TIMESTAMP,
    FOREIGN KEY (worker_id) REFERENCES worker (id)
);

CREATE TABLE IF NOT EXISTS picker_task
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    distance      DOUBLE,
    pack_amount   INT,
    lines_amount  INT,
    weight_g      INT,
    volume_ml     INT,
    avg_height_m  INT,
    time_s        DOUBLE DEFAULT NULL,
    start_time    TIMESTAMP,
    end_time      TIMESTAMP,
    mc_start_time TIMESTAMP,
    mc_end_time   TIMESTAMP,
    date          DATE,
    zone_id       BIGINT NOT NULL,
    worker_id     BIGINT,
    due_date      TIMESTAMP,
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
    zone_id         BIGINT,
    FOREIGN KEY (zone_id) REFERENCES zone (id)
);

CREATE TABLE IF NOT EXISTS monte_carlo_data
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    sim_no          INT,
    time            TIMESTAMP,
    completed_tasks INT,
    items_picked    INT,
    zone_id         BIGINT
);

CREATE TRIGGER update_worker_zone
    BEFORE INSERT
    ON active_task_worker
    FOR EACH ROW
BEGIN
    DECLARE task_zone INT;

    SELECT zone_id INTO task_zone FROM task WHERE id = (SELECT task_id FROM active_task WHERE id = NEW.active_task_id);

    UPDATE worker SET zone_id = task_zone WHERE id = NEW.worker_id;
END;


INSERT INTO worker (id, name, zone_id, work_title, efficiency, availability)
VALUES
       (1, 'Jane Smith', 1, 'Warehouse Supervisor', 0.9, true),
       (3, 'Alice Johnson', 2, 'Warehouse Technician', 1.2, true),
       (2, 'Bob Brown', 2, 'Forklift Operator', 0.8, false),
       (4, 'Charlie Davis', 3, 'Warehouse Engineer', 1.3, true),
       (5, 'Diana Evans', 3, 'Inventory Clerk', 1.0, true),
       (7, 'Eve Foster', 4, 'Logistics Analyst', 0.95, true),
       (6, 'Frank Green', 4, 'Shipping Coordinator', 1.25, true),
       (9, 'Grace Harris', 5, 'Quality Inspector', 0.85, false),
       (8, 'Hank Irving', 5, 'Warehouse Specialist', 1.15, true),
       (11, 'Ivy Johnson', 6, 'Warehouse Consultant', 1.05, true),
       (10, 'Jack King', 6, 'Warehouse Planner', 1.2, true),
       (13, 'Karen Lee', 6, 'Warehouse Designer', 0.75, true),
       (12, 'Leo Martin', 6, 'Warehouse Developer', 1.3, false),
       (15, 'Mona Nelson', 7, 'Warehouse Architect', 1.0, true),
       (16, 'Vincent Holiday', 0, 'Powerhouse Specialist', 0, false),
       (14, 'Gerrard Paul', 0, 'Truck Mechanic', 1.05, true),
       (17, 'Nancy White', 1, 'Warehouse Clerk', 1.1, true),
       (18, 'Oscar Black', 2, 'Warehouse Operator', 0.95, true),
       (20, 'Paul Brown', 3, 'Warehouse Assistant', 1.25, true),
       (19, 'Quincy Green', 4, 'Warehouse Coordinator', 0.9, true),
       (21, 'Rachel Blue', 5, 'Warehouse Manager', 1.2, true),
       (22, 'Steve Red', 6, 'Warehouse Supervisor', 1.15, true),
       (23, 'Tina Yellow', 7, 'Warehouse Technician', 1.0, true),
       (25, 'Uma Orange', 1, 'Warehouse Engineer', 0.85, true),
       (24, 'Victor Purple', 2, 'Warehouse Specialist', 1.3, true),
       (27, 'Wendy Pink', 3, 'Warehouse Analyst', 1.05, true),
       (26, 'Xander Gray', 4, 'Warehouse Planner', 0.75, true),
       (29, 'Yara Cyan', 5, 'Warehouse Developer', 1.1, true),
       (28, 'Zane Magenta', 6, 'Warehouse Architect', 0.9, true),
       (30, 'Adam Silver', 7, 'Warehouse Designer', 1.2, true),
       (31, 'Bella Gold', 1, 'Warehouse Clerk', 1.15, true),
       (32, 'Carl Bronze', 2, 'Warehouse Operator', 1.05, true),
       (33, 'Diana Copper', 3, 'Warehouse Assistant', 0.7, true),
       (34, 'Ethan Iron', 4, 'Warehouse Coordinator', 1.0, true),
       (35, 'Fiona Steel', 5, 'Warehouse Manager', 1.3, true),
       (36, 'George Nickel', 6, 'Warehouse Supervisor', 0.95, true),
       (37, 'Holly Zinc', 7, 'Warehouse Technician', 0.85, true),
       (38, 'Ian Lead', 1, 'Warehouse Engineer', 1.2, true),
       (39, 'Judy Tin', 2, 'Warehouse Specialist', 1.1, true),
       (40, 'John Doe', 1, 'Warehouse Manager', 1.1, true),
       (41, 'Kevin Aluminum', 3, 'Warehouse Analyst', 1.0, true),
       (42, 'Oliver Stone', 9, 'Warehouse Operator', 1.1, true),
       (43, 'Emma Watson', 9, 'Inventory Specialist', 1.2, true),
       (44, 'Liam Brown', 7, 'Logistics Coordinator', 0.95, true),
       (45, 'Sophia Green', 8, 'Shipping Manager', 1.05, true),
       (46, 'Noah White', 9, 'Quality Control', 1.0, true);

INSERT INTO worker_schedule (worker_id, day_of_week, start_time, end_time)
VALUES
    -- Worker 1 works 7 days a week
    (1, 'MONDAY', '08:00:00', '16:00:00'),
    (1, 'TUESDAY', '08:00:00', '16:00:00'),
    (1, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (1, 'THURSDAY', '08:00:00', '16:00:00'),
    (1, 'FRIDAY', '08:00:00', '16:00:00'),
    (1, 'SATURDAY', '08:00:00', '14:00:00'),
    (1, 'SUNDAY', '08:00:00', '14:00:00'),

    -- Worker 2 works 5 days a week
    (2, 'MONDAY', '09:00:00', '17:00:00'),
    (2, 'TUESDAY', '09:00:00', '17:00:00'),
    (2, 'WEDNESDAY', '09:00:00', '17:00:00'),
    (2, 'THURSDAY', '09:00:00', '17:00:00'),
    (2, 'FRIDAY', '09:00:00', '17:00:00'),

    -- Worker 3 works 5 days a week
    (3, 'MONDAY', '10:00:00', '16:00:00'),
    (3, 'TUESDAY', '10:00:00', '16:00:00'),
    (3, 'WEDNESDAY', '10:00:00', '16:00:00'),
    (3, 'SATURDAY', '10:00:00', '16:00:00'),
    (3, 'SUNDAY', '10:00:00', '16:00:00'),

    -- Worker 4 works 5 days a week
    (4, 'MONDAY', '07:00:00', '15:00:00'),
    (4, 'TUESDAY', '07:00:00', '15:00:00'),
    (4, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (4, 'THURSDAY', '07:00:00', '15:00:00'),
    (4, 'FRIDAY', '07:00:00', '15:00:00'),

    -- Worker 5 works 5 days a week
    (5, 'MONDAY', '06:00:00', '14:00:00'),
    (5, 'TUESDAY', '06:00:00', '14:00:00'),
    (5, 'WEDNESDAY', '06:00:00', '14:00:00'),
    (5, 'THURSDAY', '06:00:00', '14:00:00'),
    (5, 'FRIDAY', '06:00:00', '14:00:00'),

    -- Worker 6 works 6 days a week
    (6, 'MONDAY', '03:00:00', '11:00:00'),
    (6, 'TUESDAY', '03:00:00', '11:00:00'),
    (6, 'WEDNESDAY', '03:00:00', '11:00:00'),
    (6, 'THURSDAY', '03:00:00', '11:00:00'),
    (6, 'FRIDAY', '03:00:00', '11:00:00'),
    (6, 'SATURDAY', '03:00:00', '09:00:00'),

    -- Worker 7 works 6 days a week
    (7, 'MONDAY', '08:00:00', '16:00:00'),
    (7, 'TUESDAY', '08:00:00', '16:00:00'),
    (7, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (7, 'THURSDAY', '08:00:00', '16:00:00'),
    (7, 'FRIDAY', '08:00:00', '16:00:00'),
    (7, 'SATURDAY', '08:00:00', '14:00:00'),

    -- Worker 8 works 5 days a week
    (8, 'MONDAY', '10:00:00', '16:00:00'),
    (8, 'TUESDAY', '10:00:00', '16:00:00'),
    (8, 'WEDNESDAY', '10:00:00', '16:00:00'),
    (8, 'SATURDAY', '10:00:00', '16:00:00'),
    (8, 'SUNDAY', '10:00:00', '16:00:00'),

    -- Worker 9 works 5 days a week
    (9, 'MONDAY', '07:00:00', '15:00:00'),
    (9, 'TUESDAY', '07:00:00', '15:00:00'),
    (9, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (9, 'THURSDAY', '07:00:00', '15:00:00'),
    (9, 'FRIDAY', '07:00:00', '15:00:00'),

    -- Worker 10 works 5 days a week
    (10, 'MONDAY', '06:00:00', '14:00:00'),
    (10, 'TUESDAY', '06:00:00', '14:00:00'),
    (10, 'WEDNESDAY', '06:00:00', '14:00:00'),
    (10, 'THURSDAY', '06:00:00', '14:00:00'),
    (10, 'FRIDAY', '06:00:00', '14:00:00'),

    -- Worker 11 works 6 days a week
    (11, 'MONDAY', '08:00:00', '16:00:00'),
    (11, 'TUESDAY', '08:00:00', '16:00:00'),
    (11, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (11, 'THURSDAY', '08:00:00', '16:00:00'),
    (11, 'FRIDAY', '08:00:00', '16:00:00'),
    (11, 'SATURDAY', '08:00:00', '14:00:00'),

    -- Worker 12 works 5 days a week
    (12, 'MONDAY', '09:00:00', '17:00:00'),
    (12, 'TUESDAY', '09:00:00', '17:00:00'),
    (12, 'WEDNESDAY', '09:00:00', '17:00:00'),
    (12, 'THURSDAY', '09:00:00', '17:00:00'),
    (12, 'FRIDAY', '09:00:00', '17:00:00'),

    -- Worker 13 works 5 days a week
    (13, 'MONDAY', '10:00:00', '16:00:00'),
    (13, 'TUESDAY', '10:00:00', '16:00:00'),
    (13, 'WEDNESDAY', '10:00:00', '16:00:00'),
    (13, 'SATURDAY', '10:00:00', '16:00:00'),
    (13, 'SUNDAY', '10:00:00', '16:00:00'),

    -- Worker 14 works 5 days a week
    (14, 'MONDAY', '07:00:00', '15:00:00'),
    (14, 'TUESDAY', '07:00:00', '15:00:00'),
    (14, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (14, 'THURSDAY', '07:00:00', '15:00:00'),
    (14, 'FRIDAY', '07:00:00', '15:00:00'),

    -- Worker 15 works 5 days a week
    (15, 'MONDAY', '06:00:00', '14:00:00'),
    (15, 'TUESDAY', '06:00:00', '14:00:00'),
    (15, 'WEDNESDAY', '06:00:00', '14:00:00'),
    (15, 'THURSDAY', '06:00:00', '14:00:00'),
    (15, 'FRIDAY', '06:00:00', '14:00:00'),

    -- Worker 16 works 6 days a week
    (16, 'MONDAY', '08:00:00', '16:00:00'),
    (16, 'TUESDAY', '08:00:00', '16:00:00'),
    (16, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (16, 'THURSDAY', '08:00:00', '16:00:00'),
    (16, 'FRIDAY', '08:00:00', '16:00:00'),
    (16, 'SATURDAY', '08:00:00', '14:00:00'),

    -- Worker 17 works 5 days a week
    (17, 'MONDAY', '09:00:00', '17:00:00'),
    (17, 'TUESDAY', '09:00:00', '17:00:00'),
    (17, 'WEDNESDAY', '09:00:00', '17:00:00'),
    (17, 'THURSDAY', '09:00:00', '17:00:00'),
    (17, 'FRIDAY', '09:00:00', '17:00:00'),

    -- Worker 18 works 5 days a week
    (18, 'MONDAY', '10:00:00', '16:00:00'),
    (18, 'TUESDAY', '10:00:00', '16:00:00'),
    (18, 'WEDNESDAY', '10:00:00', '16:00:00'),
    (18, 'SATURDAY', '10:00:00', '16:00:00'),
    (18, 'SUNDAY', '10:00:00', '16:00:00'),

    -- Worker 19 works 5 days a week
    (19, 'MONDAY', '07:00:00', '15:00:00'),
    (19, 'TUESDAY', '07:00:00', '15:00:00'),
    (19, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (19, 'THURSDAY', '07:00:00', '15:00:00'),
    (19, 'FRIDAY', '07:00:00', '15:00:00'),

    -- Worker 20 works 5 days a week
    (20, 'MONDAY', '06:00:00', '14:00:00'),
    (20, 'TUESDAY', '06:00:00', '14:00:00'),
    (20, 'WEDNESDAY', '06:00:00', '14:00:00'),
    (20, 'THURSDAY', '06:00:00', '14:00:00'),
    (20, 'FRIDAY', '06:00:00', '14:00:00'),

    -- Worker 21 works 6 days a week
    (21, 'MONDAY', '08:00:00', '16:00:00'),
    (21, 'TUESDAY', '08:00:00', '16:00:00'),
    (21, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (21, 'THURSDAY', '08:00:00', '16:00:00'),
    (21, 'FRIDAY', '08:00:00', '16:00:00'),
    (21, 'SATURDAY', '08:00:00', '14:00:00'),

    -- Worker 22 works 5 days a week
    (22, 'MONDAY', '09:00:00', '17:00:00'),
    (22, 'TUESDAY', '09:00:00', '17:00:00'),
    (22, 'WEDNESDAY', '09:00:00', '17:00:00'),
    (22, 'THURSDAY', '09:00:00', '17:00:00'),
    (22, 'FRIDAY', '09:00:00', '17:00:00'),

    -- Worker 23 works 5 days a week
    (23, 'MONDAY', '10:00:00', '16:00:00'),
    (23, 'TUESDAY', '10:00:00', '16:00:00'),
    (23, 'WEDNESDAY', '10:00:00', '16:00:00'),
    (23, 'SATURDAY', '10:00:00', '16:00:00'),
    (23, 'SUNDAY', '10:00:00', '16:00:00'),

    -- Worker 24 works 5 days a week
    (24, 'MONDAY', '07:00:00', '15:00:00'),
    (24, 'TUESDAY', '07:00:00', '15:00:00'),
    (24, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (24, 'THURSDAY', '07:00:00', '15:00:00'),
    (24, 'FRIDAY', '07:00:00', '15:00:00'),

    -- Worker 25 works 5 days a week
    (25, 'MONDAY', '06:00:00', '14:00:00'),
    (25, 'TUESDAY', '06:00:00', '14:00:00'),
    (25, 'WEDNESDAY', '06:00:00', '14:00:00'),
    (25, 'THURSDAY', '06:00:00', '14:00:00'),
    (25, 'FRIDAY', '06:00:00', '14:00:00'),

    -- Worker 26 works 6 days a week
    (26, 'MONDAY', '08:00:00', '16:00:00'),
    (26, 'TUESDAY', '08:00:00', '16:00:00'),
    (26, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (26, 'THURSDAY', '08:00:00', '16:00:00'),
    (26, 'FRIDAY', '08:00:00', '16:00:00'),
    (26, 'SATURDAY', '08:00:00', '14:00:00'),

    -- Worker 27 works 5 days a week
    (27, 'MONDAY', '09:00:00', '17:00:00'),
    (27, 'TUESDAY', '09:00:00', '17:00:00'),
    (27, 'WEDNESDAY', '09:00:00', '17:00:00'),
    (27, 'THURSDAY', '09:00:00', '17:00:00'),
    (27, 'FRIDAY', '09:00:00', '17:00:00'),

    -- Worker 28 works 5 days a week
    (28, 'MONDAY', '10:00:00', '16:00:00'),
    (28, 'TUESDAY', '10:00:00', '16:00:00'),
    (28, 'WEDNESDAY', '10:00:00', '16:00:00'),
    (28, 'SATURDAY', '10:00:00', '16:00:00'),
    (28, 'SUNDAY', '10:00:00', '16:00:00'),

    -- Worker 29 works 5 days a week
    (29, 'MONDAY', '07:00:00', '15:00:00'),
    (29, 'TUESDAY', '07:00:00', '15:00:00'),
    (29, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (29, 'THURSDAY', '07:00:00', '15:00:00'),
    (29, 'FRIDAY', '07:00:00', '15:00:00'),

    -- Worker 30 works 5 days a week
    (30, 'MONDAY', '06:00:00', '14:00:00'),
    (30, 'TUESDAY', '06:00:00', '14:00:00'),
    (30, 'WEDNESDAY', '06:00:00', '14:00:00'),
    (30, 'THURSDAY', '06:00:00', '14:00:00'),
    (30, 'FRIDAY', '06:00:00', '14:00:00'),

    -- Worker 31 works 6 days a week
    (31, 'MONDAY', '08:00:00', '16:00:00'),
    (31, 'TUESDAY', '08:00:00', '16:00:00'),
    (31, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (31, 'THURSDAY', '08:00:00', '16:00:00'),
    (31, 'FRIDAY', '08:00:00', '16:00:00'),
    (31, 'SATURDAY', '08:00:00', '14:00:00'),

    -- Worker 32 works 5 days a week
    (32, 'MONDAY', '09:00:00', '17:00:00'),
    (32, 'TUESDAY', '09:00:00', '17:00:00'),
    (32, 'WEDNESDAY', '09:00:00', '17:00:00'),
    (32, 'THURSDAY', '09:00:00', '17:00:00'),
    (32, 'FRIDAY', '09:00:00', '17:00:00'),

    -- Worker 33 works 5 days a week
    (33, 'MONDAY', '10:00:00', '16:00:00'),
    (33, 'TUESDAY', '10:00:00', '16:00:00'),
    (33, 'WEDNESDAY', '10:00:00', '16:00:00'),
    (33, 'SATURDAY', '10:00:00', '16:00:00'),
    (33, 'SUNDAY', '10:00:00', '16:00:00'),

    -- Worker 34 works 5 days a week
    (34, 'MONDAY', '07:00:00', '15:00:00'),
    (34, 'TUESDAY', '07:00:00', '15:00:00'),
    (34, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (34, 'THURSDAY', '07:00:00', '15:00:00'),
    (34, 'FRIDAY', '07:00:00', '15:00:00'),

    -- Worker 35 works 5 days a week
    (35, 'MONDAY', '06:00:00', '14:00:00'),
    (35, 'TUESDAY', '06:00:00', '14:00:00'),
    (35, 'WEDNESDAY', '06:00:00', '14:00:00'),
    (35, 'THURSDAY', '06:00:00', '14:00:00'),
    (35, 'FRIDAY', '06:00:00', '14:00:00'),

    -- Worker 36 works 6 days a week
    (36, 'MONDAY', '08:00:00', '16:00:00'),
    (36, 'TUESDAY', '08:00:00', '16:00:00'),
    (36, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (36, 'THURSDAY', '08:00:00', '16:00:00'),
    (36, 'FRIDAY', '08:00:00', '16:00:00'),
    (36, 'SATURDAY', '08:00:00', '14:00:00'),

    -- Worker 37 works 5 days a week
    (37, 'MONDAY', '09:00:00', '17:00:00'),
    (37, 'TUESDAY', '09:00:00', '17:00:00'),
    (37, 'WEDNESDAY', '09:00:00', '17:00:00'),
    (37, 'THURSDAY', '09:00:00', '17:00:00'),
    (37, 'FRIDAY', '09:00:00', '17:00:00'),

    -- Worker 38 works 5 days a week
    (38, 'MONDAY', '10:00:00', '16:00:00'),
    (38, 'TUESDAY', '10:00:00', '16:00:00'),
    (38, 'WEDNESDAY', '10:00:00', '16:00:00'),
    (38, 'SATURDAY', '10:00:00', '16:00:00'),
    (38, 'SUNDAY', '10:00:00', '16:00:00'),

    -- Worker 39 works 5 days a week
    (39, 'MONDAY', '07:00:00', '15:00:00'),
    (39, 'TUESDAY', '07:00:00', '15:00:00'),
    (39, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (39, 'THURSDAY', '07:00:00', '15:00:00'),
    (39, 'FRIDAY', '07:00:00', '15:00:00'),

    -- Worker 40 works 5 days a week
    (40, 'MONDAY', '06:00:00', '14:00:00'),
    (40, 'TUESDAY', '06:00:00', '14:00:00'),
    (40, 'WEDNESDAY', '06:00:00', '14:00:00'),
    (40, 'THURSDAY', '06:00:00', '14:00:00'),
    (40, 'FRIDAY', '06:00:00', '14:00:00'),

    -- Worker 41 works 6 days a week
    (41, 'MONDAY', '08:00:00', '16:00:00'),
    (41, 'TUESDAY', '08:00:00', '16:00:00'),
    (41, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (41, 'THURSDAY', '08:00:00', '16:00:00'),
    (41, 'FRIDAY', '08:00:00', '16:00:00'),
    (41, 'SATURDAY', '08:00:00', '14:00:00'),

    -- Worker 42 works 5 days a week
    (42, 'MONDAY', '09:00:00', '17:00:00'),
    (42, 'TUESDAY', '09:00:00', '17:00:00'),
    (42, 'WEDNESDAY', '09:00:00', '17:00:00'),
    (42, 'THURSDAY', '09:00:00', '17:00:00'),
    (42, 'FRIDAY', '09:00:00', '17:00:00'),

    -- Worker 43 works 5 days a week
    (43, 'MONDAY', '10:00:00', '16:00:00'),
    (43, 'TUESDAY', '10:00:00', '16:00:00'),
    (43, 'WEDNESDAY', '10:00:00', '16:00:00'),
    (43, 'SATURDAY', '10:00:00', '16:00:00'),
    (43, 'SUNDAY', '10:00:00', '16:00:00'),

    -- Worker 44 works 5 days a week
    (44, 'MONDAY', '07:00:00', '15:00:00'),
    (44, 'TUESDAY', '07:00:00', '15:00:00'),
    (44, 'WEDNESDAY', '07:00:00', '15:00:00'),
    (44, 'THURSDAY', '07:00:00', '15:00:00'),
    (44, 'FRIDAY', '07:00:00', '15:00:00'),

    -- Worker 45 works 5 days a week
    (45, 'MONDAY', '06:00:00', '14:00:00'),
    (45, 'TUESDAY', '06:00:00', '14:00:00'),
    (45, 'WEDNESDAY', '06:00:00', '14:00:00'),
    (45, 'THURSDAY', '06:00:00', '14:00:00'),
    (45, 'FRIDAY', '06:00:00', '14:00:00'),

    -- Worker 46 works 6 days a week
    (46, 'MONDAY', '08:00:00', '16:00:00'),
    (46, 'TUESDAY', '08:00:00', '16:00:00'),
    (46, 'WEDNESDAY', '08:00:00', '16:00:00'),
    (46, 'THURSDAY', '08:00:00', '16:00:00'),
    (46, 'FRIDAY', '08:00:00', '16:00:00'),
    (46, 'SATURDAY', '08:00:00', '14:00:00');


INSERT INTO zone (name, capacity, is_picker_zone)
VALUES ('Receiving', 10, false),
       ('Storage', 15, false),
       ('Picking', 5, false),
       ('Packing', 7, false),
       ('Shipping', 8, false),
       ('Quality Control', 4, false),
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
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (2, 1),
       (2, 2),
       (2, 3),
       (2, 4),
       (3, 1),
       (3, 2),
       (3, 3),
       (3, 4),
       (4, 1),
       (4, 2),
       (4, 3),
       (4, 4),
       (5, 1),
       (5, 2),
       (5, 3),
       (5, 4),
       (6, 1),
       (6, 2),
       (6, 3),
       (6, 4),
       (7, 1),
       (7, 2),
       (7, 3),
       (7, 4),
       (8, 1),
       (8, 2),
       (8, 3),
       (8, 4),
       (9, 1),
       (9, 2),
       (9, 3),
       (9, 4),
       (10, 1),
       (10, 2),
       (10, 3),
       (10, 4),
       (11, 1),
       (11, 2),
       (11, 3),
       (11, 4),
       (12, 1),
       (12, 2),
       (12, 3),
       (12, 4),
       (13, 1),
       (13, 2),
       (13, 3),
       (13, 4),
       (14, 1),
       (14, 2),
       (14, 3),
       (14, 4),
       (15, 1),
       (15, 2),
       (15, 3),
       (15, 4),
       (16, 1),
       (16, 2),
       (16, 3),
       (16, 4),
       (17, 1),
       (17, 2),
       (17, 3),
       (17, 4),
       (18, 1),
       (18, 2),
       (18, 3),
       (18, 4),
       (19, 1),
       (19, 2),
       (19, 3),
       (19, 4),
       (20, 1),
       (20, 2),
       (20, 3),
       (20, 4),
       (21, 1),
       (21, 2),
       (21, 3),
       (21, 4),
       (22, 1),
       (22, 2),
       (22, 3),
       (22, 4),
       (23, 1),
       (23, 2),
       (23, 3),
       (23, 4),
       (24, 1),
       (24, 2),
       (24, 3),
       (24, 4),
       (25, 1),
       (25, 2),
       (25, 3),
       (25, 4),
       (26, 1),
       (26, 2),
       (26, 3),
       (26, 4),
       (27, 1),
       (27, 2),
       (27, 3),
       (27, 4),
       (28, 1),
       (28, 2),
       (28, 3),
       (28, 4),
       (29, 1),
       (29, 2),
       (29, 3),
       (29, 4),
       (30, 1),
       (30, 2),
       (30, 3),
       (30, 4),
       (31, 1),
       (31, 2),
       (31, 3),
       (31, 4),
       (32, 1),
       (32, 2),
       (32, 3),
       (32, 4),
       (33, 1),
       (33, 2),
       (33, 3),
       (33, 4),
       (34, 1),
       (34, 2),
       (34, 3),
       (34, 4);


INSERT INTO task (name, description, min_duration, max_duration, min_workers, max_workers, zone_id)
VALUES ('Inventory Check', 'Check the inventory levels in the warehouse', 10, 30, 1, 2, 1),
       ('Restock Shelves', 'Restock the shelves with new inventory', 10, 20, 1, 3, 1),
       ('Order Processing', 'Process customer orders for shipment', 10, 30, 2, 4, 1),
       ('Quality Inspection', 'Inspect the quality of incoming goods', 20, 45, 1, 2, 2),
       ('Package Orders', 'Package customer orders for delivery', 10, 30, 1, 2, 2),
       ('Load Trucks', 'Load trucks with outgoing shipments', 15, 40, 2, 3, 3),
       ('Unload Trucks', 'Unload trucks with incoming shipments', 20, 45, 2, 3, 3),
       ('Sort Inventory', 'Sort inventory into appropriate zones', 15, 30, 1, 2, 3),
       ('Pick Orders', 'Pick items from shelves for customer orders', 10, 20, 1, 2, 4),
       ('Pack Orders', 'Pack items for shipment to customers', 10, 25, 1, 2, 4),
       ('Inventory Audit', 'Conduct an audit of the inventory', 20, 50, 1, 2, 5),
       ('Equipment Maintenance', 'Perform maintenance on warehouse equipment', 15, 30, 1, 2, 5),
       ('Safety Training', 'Conduct safety training for workers', 30, 60, 1, 2, 6),
       ('Data Entry', 'Enter data into the warehouse management system', 10, 20, 1, 1, 6),
       ('Report Generation', 'Generate reports on warehouse operations', 10, 25, 1, 1, 4),
       ('Cycle Counting', 'Perform cycle counting of inventory', 15, 25, 1, 1, 4),
       ('Label Products', 'Label products with barcodes', 15, 30, 1, 2, 4),
       ('Warehouse Cleaning', 'Clean and organize the warehouse', 15, 35, 1, 2, 5),
       ('Safety Inspection', 'Inspect the warehouse for safety compliance', 30, 35, 1, 2, 5),
       ('Plan Shipments', 'Plan the shipments for the day', 10, 20, 1, 1, 6),
       ('Monitor Inventory', 'Monitor inventory levels and report discrepancies', 10, 37, 1, 2, 6),
       ('Schedule Maintenance', 'Schedule maintenance for warehouse equipment', 10, 40, 1, 2, 4),
       ('Update Records', 'Update warehouse records and logs', 10, 30, 1, 1, 4),
       ('Coordinate Teams', 'Coordinate teams for various tasks', 10, 25, 1, 2, 5),
       ('Review Performance', 'Review the performance of warehouse operations', 20, 40, 1, 2, 5),
       ('System Monitoring', 'Monitor the warehouse management system for any issues', 15, 50, 1, 2, 6),
       ('Security Check', 'Perform security checks and monitor surveillance systems', 25, 60, 1, 2, 6);


INSERT INTO task_license (task_id, license_id)
VALUES (1, 2),
       (1, 4),
       (2, 1),
       (3, 2),
       (3, 3),
       (4, 1),
       (5, 3),
       (5, 4),
       (6, 2),
       (7, 1),
       (7, 3),
       (8, 2),
       (9, 1),
       (9, 4),
       (10, 3),
       (11, 2),
       (12, 1),
       (12, 3),
       (13, 4),
       (14, 2),
       (15, 1),
       (15, 2),
       (16, 3),
       (17, 2),
       (17, 4),
       (18, 1),
       (18, 2),
       (19, 3),
       (20, 4),
       (21, 1),
       (21, 3),
       (22, 2),
       (22, 4),
       (23, 1),
       (24, 3),
       (25, 2),
       (25, 4),
       (26, 1),
       (27, 2),
       (27, 3),
       (27, 4);


