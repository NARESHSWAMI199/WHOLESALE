CREATE TABLE `item_reports`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `category_id` int    DEFAULT NULL,
    `item_id`     bigint DEFAULT NULL,
    `user_id`     BIGINT DEFAULT NULL,
    `message`     text,
    `created_at`  bigint DEFAULT NULL,
    `updated_at`  bigint DEFAULT NULL,
    PRIMARY KEY (`id`)
);