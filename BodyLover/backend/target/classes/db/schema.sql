-- Create Database
CREATE DATABASE IF NOT EXISTS bodylover DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bodylover;

-- Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
    `password` VARCHAR(100) NOT NULL COMMENT 'Encrypted Password',
    `mode` VARCHAR(20) NOT NULL COMMENT 'User Mode: TEENAGER, ADULT, SENIOR',
    `age` INT COMMENT 'Age',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Information';

-- Health Records Table (For Teenager/All)
CREATE TABLE IF NOT EXISTS `health_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `height` DECIMAL(5, 2) COMMENT 'Height in cm',
    `weight` DECIMAL(5, 2) COMMENT 'Weight in kg',
    `bmi` DECIMAL(5, 2) COMMENT 'BMI Value',
    `record_date` DATE NOT NULL COMMENT 'Date of record',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Health Records';

-- Plans Table (Training & Diet)
CREATE TABLE IF NOT EXISTS `plans` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `title` VARCHAR(100) NOT NULL COMMENT 'Plan Title',
    `content` TEXT COMMENT 'Detailed Plan Content',
    `duration_hours` DECIMAL(4, 1) DEFAULT 0 COMMENT 'Duration in hours',
    `plan_type` VARCHAR(20) NOT NULL DEFAULT 'EXERCISE' COMMENT 'Type: EXERCISE, DIET',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Status: PENDING, COMPLETED',
    `scheduled_date` DATE NOT NULL COMMENT 'Planned Date',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Training and Diet Plans';

-- Create Database
CREATE DATABASE IF NOT EXISTS bodylover DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bodylover;

-- Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
    `password` VARCHAR(100) NOT NULL COMMENT 'Encrypted Password',
    `mode` VARCHAR(20) NOT NULL COMMENT 'User Mode: TEENAGER, ADULT, SENIOR',
    `age` INT COMMENT 'Age',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Information';

-- Health Records Table (For Teenager/All)
CREATE TABLE IF NOT EXISTS `health_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `height` DECIMAL(5, 2) COMMENT 'Height in cm',
    `weight` DECIMAL(5, 2) COMMENT 'Weight in kg',
    `bmi` DECIMAL(5, 2) COMMENT 'BMI Value',
    `record_date` DATE NOT NULL COMMENT 'Date of record',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Health Records';

-- Plans Table (Training & Diet)
CREATE TABLE IF NOT EXISTS `plans` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `title` VARCHAR(100) NOT NULL COMMENT 'Plan Title',
    `content` TEXT COMMENT 'Detailed Plan Content',
    `duration_hours` DECIMAL(4, 1) DEFAULT 0 COMMENT 'Duration in hours',
    `actual_minutes` INT DEFAULT 0 COMMENT 'Actual duration in minutes',
    `plan_type` VARCHAR(20) NOT NULL DEFAULT 'EXERCISE' COMMENT 'Type: EXERCISE, DIET',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Status: PENDING, COMPLETED',
    `scheduled_date` DATE NOT NULL COMMENT 'Planned Date',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Training and Diet Plans';

-- Family Relations Table
CREATE TABLE IF NOT EXISTS `family_relations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `requester_id` BIGINT NOT NULL COMMENT 'User who sent request',
    `receiver_id` BIGINT NOT NULL COMMENT 'User who received request',
    `relation_desc` VARCHAR(50) COMMENT 'Relationship description (e.g. Father, Son)',
    `status` VARCHAR(20) DEFAULT 'ACCEPTED' COMMENT 'Status: PENDING, ACCEPTED',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`requester_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`receiver_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Family Connections';
