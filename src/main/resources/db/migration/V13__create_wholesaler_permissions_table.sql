CREATE TABLE `wholesaler_permissions`
(
    `id`            int NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT DEFAULT NULL,
    `permission_id` int    DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (permission_id) REFERENCES store_permissions (id),
    PRIMARY KEY (`id`)
);

