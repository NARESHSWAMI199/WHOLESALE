CREATE TABLE `service_plans`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `slug`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `name`        varchar(500)                                                  DEFAULT NULL,
    `price`       float                                                         DEFAULT NULL,
    `discount`    float                                                         DEFAULT NULL,
    `status`      enum('A','D') DEFAULT NULL,
    `icon`        text,
    `months`      int                                                           DEFAULT NULL,
    `description` text,
    `is_deleted`  enum('Y','N') DEFAULT NULL,
    `created_at`  bigint                                                        DEFAULT NULL,
    `created_by`  int                                                           DEFAULT NULL,
    `updated_at`  bigint                                                        DEFAULT NULL,
    `updated_by`  int                                                           DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `name_unique` (`name`),
    UNIQUE KEY `price_unique` (`price`),
    UNIQUE KEY `slug_unique` (`slug`)
);