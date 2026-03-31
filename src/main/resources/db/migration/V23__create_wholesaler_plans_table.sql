-- test.wholesaler_plans definition

CREATE TABLE `wholesaler_plans`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `slug`        varchar(100) DEFAULT NULL,
    `user_id`     BIGINT       DEFAULT NULL,
    `plan_id`     int          DEFAULT NULL,
    `created_at`  bigint       DEFAULT NULL,
    `expiry_date` bigint       DEFAULT NULL,
    `created_by`  int          DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (plan_id) REFERENCES service_plans (id),
    UNIQUE KEY `slug_unique` (`slug`)
);