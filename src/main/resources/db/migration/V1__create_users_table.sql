CREATE TABLE `users`
(
    `user_id`     BIGINT                                                       NOT NULL AUTO_INCREMENT,
    `slug`        varchar(50)                                                   DEFAULT NULL,
    `otp`         varchar(6)                                                    DEFAULT NULL,
    `username`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `email`       varchar(50)                                                  NOT NULL,
    `contact`     varchar(12)                                                  NOT NULL,
    `password`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `avtar`       text,
    `user_type`   enum('S','SA','R','W') DEFAULT NULL,
    `status`      enum('A','D') DEFAULT NULL,
    `is_deleted`  enum('Y','N') DEFAULT NULL,
    `created_at`  bigint                                                        DEFAULT NULL,
    `updated_at`  bigint                                                        DEFAULT NULL,
    `last_seen`   bigint                                                        DEFAULT NULL,
    `created_by`  int                                                           DEFAULT NULL,
    `updated_by`  int                                                           DEFAULT NULL,
    `active_plan` int                                                           DEFAULT NULL,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `email` (`email`),
    UNIQUE KEY `contact` (`contact`)
);