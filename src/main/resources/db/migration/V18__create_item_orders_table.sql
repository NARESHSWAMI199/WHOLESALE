CREATE TABLE `item_orders`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `item_id`    BIGINT DEFAULT NULL,
    `user_id`    bigint DEFAULT NULL,
    `quantity`   int    DEFAULT NULL,
    `status`     enum('A','D') DEFAULT NULL,
    `updated_at` bigint DEFAULT NULL,
    `created_at` bigint DEFAULT NULL,
    `is_deleted` enum('Y','N') DEFAULT NULL,
    `updated_by` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);