CREATE DATABASE IF NOT EXISTS warehouse;
USE warehouse;

CREATE TABLE IF NOT EXISTS worker (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    workerType VARCHAR(255) NOT NULL,
    effectiveness DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS license (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS worker_license (
    worker_id INT NOT NULL,
    license_id INT NOT NULL,
    FOREIGN KEY (worker_id) REFERENCES worker(id),
    FOREIGN KEY (license_id) REFERENCES license(id)
);

CREATE TABLE IF NOT EXISTS zone (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    capacity INT NOT NULL
);

CREATE TABLE IF NOT EXISTS task (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    min_duration INT NOT NULL,
    max_duration INT NOT NULL,
    min_workers INT NOT NULL,
    max_workers INT NOT NULL
);

CREATE TABLE IF NOT EXISTS active_task (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    due_date DATE NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    FOREIGN KEY (task_id) REFERENCES task(id)
);

CREATE TABLE IF NOT EXISTS active_task_worker (
    active_task_id INT NOT NULL,
    worker_id INT NOT NULL,
    FOREIGN KEY (active_task_id) REFERENCES active_task(id),
    FOREIGN KEY (worker_id) REFERENCES worker(id)
);

CREATE TABLE IF NOT EXISTS task_license (
    task_id INT NOT NULL,
    license_id INT NOT NULL,
    FOREIGN KEY (task_id) REFERENCES task(id),
    FOREIGN KEY (license_id) REFERENCES license(id)
);

CREATE TABLE IF NOT EXISTS zone_tasks (
    zone_id INT NOT NULL,
    task_id INT NOT NULL,
    FOREIGN KEY (zone_id) REFERENCES zone(id),
    FOREIGN KEY (task_id) REFERENCES task(id)
);