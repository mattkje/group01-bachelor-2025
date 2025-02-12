CREATE DATABASE IF NOT EXISTS warehouse;
USE warehouse;

CREATE TABLE IF NOT EXISTS worker
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    work_title    VARCHAR(255) NOT NULL,
    effectiveness DOUBLE       NOT NULL
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
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    due_date   TIMESTAMP NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time   TIMESTAMP NOT NULL,
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

INSERT INTO worker (name, work_title, effectiveness)
VALUES ('John Doe', 'Warehouse Manager', 1),
       ('Jane Smith', 'Warehouse Supervisor', 1),
       ('Alice Johnson', 'Warehouse Technician', 1),
       ('Bob Brown', 'Forklift Operator', 1),
       ('Charlie Davis', 'Warehouse Engineer', 1),
       ('Diana Evans', 'Inventory Clerk', 1),
       ('Eve Foster', 'Logistics Analyst', 1),
       ('Frank Green', 'Shipping Coordinator', 1),
       ('Grace Harris', 'Quality Inspector', 1),
       ('Hank Irving', 'Warehouse Specialist', 1),
       ('Ivy Johnson', 'Warehouse Consultant', 1),
       ('Jack King', 'Warehouse Planner', 1),
       ('Karen Lee', 'Warehouse Designer', 1),
       ('Leo Martin', 'Warehouse Developer', 1),
       ('Mona Nelson', 'Warehouse Architect', 1);

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
VALUES (1, 1),
       (1, 2),
       (2, 3),
       (3, 2),
       (3, 4),
       (4, 1),
       (5, 2),
       (5, 3),
       (6, 4),
       (7, 1),
       (7, 3),
       (8, 2),
       (9, 3),
       (9, 4),
       (10, 1),
       (11, 2),
       (11, 4),
       (12, 3),
       (13, 1),
       (13, 2),
       (13, 3),
       (14, 4),
       (15, 1),
       (15, 3);

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

INSERT INTO task (name, description, min_duration, max_duration, min_workers, max_workers)
VALUES ('Inventory Check', 'Check the inventory levels in the warehouse', 2, 4, 1, 2),
       ('Restock Shelves', 'Restock the shelves with new inventory', 1, 3, 1, 3),
       ('Order Processing', 'Process customer orders for shipment', 3, 5, 2, 4),
       ('Quality Inspection', 'Inspect the quality of incoming goods', 2, 4, 1, 2),
       ('Package Orders', 'Package customer orders for delivery', 1, 2, 1, 2),
       ('Load Trucks', 'Load trucks with outgoing shipments', 2, 3, 2, 3),
       ('Unload Trucks', 'Unload trucks with incoming shipments', 2, 3, 2, 3),
       ('Cycle Counting', 'Perform cycle counting of inventory', 1, 2, 1, 1),
       ('Label Products', 'Label products with barcodes', 1, 2, 1, 2),
       ('Warehouse Cleaning', 'Clean and organize the warehouse', 1, 2, 1, 2);