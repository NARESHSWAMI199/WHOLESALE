CREATE TABLE `wholesaler_future_plans`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT NOT NULL,
    `plan_id`    int    NOT NULL,
    `created_at` bigint       DEFAULT NULL,
    `updated_at` bigint       DEFAULT NULL,
    `status`     enum('O','N') DEFAULT NULL,
    `slug`       varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (plan_id) REFERENCES service_plans (id),
    UNIQUE KEY `slug` (`slug`),
    KEY          `plan_id` (`plan_id`)
);