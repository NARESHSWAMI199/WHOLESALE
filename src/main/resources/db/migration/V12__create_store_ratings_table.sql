CREATE TABLE `store_ratings`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `store_id`   int    NOT NULL,
    `user_id`    BIGINT NOT NULL,
    `rating`     int    NOT NULL DEFAULT '1',
    `created_at` bigint NOT NULL,
    `updated_at` bigint NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (store_id) REFERENCES stores (id)
);