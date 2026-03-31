CREATE TABLE `item_reviews`
(
    `id`         bigint      NOT NULL AUTO_INCREMENT,
    `slug`       varchar(50) NOT NULL,
    `item_id`    bigint DEFAULT NULL,
    `rating`     float  DEFAULT '0',
    `store_id`   int    DEFAULT NULL,
    `parent_id`  int    DEFAULT NULL,
    `user_id`    int    DEFAULT NULL,
    `likes`      bigint DEFAULT NULL,
    `dislikes`   bigint DEFAULT NULL,
    `is_deleted` enum('Y','N') DEFAULT NULL,
    `message`    text,
    `created_at` bigint DEFAULT NULL,
    `updated_at` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (item_id) REFERENCES items (id),
    UNIQUE KEY `slug` (`slug`)
);