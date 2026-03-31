CREATE TABLE `store_report`
(
    `id`         BIGINT NOT NULL AUTO_INCREMENT,
    `store_id`   int    DEFAULT NULL,
    `user_id`    BIGINT DEFAULT NULL,
    `message`    text,
    `created_at` mediumtext,
    `updated_at` mediumtext,
    PRIMARY KEY (`id`),
    FOREIGN KEY (store_id) REFERENCES stores (id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);