CREATE TABLE `item_comments`
(
    `id`         bigint      NOT NULL AUTO_INCREMENT,
    `slug`       varchar(50) NOT NULL,
    `item_id`    BIGINT DEFAULT NULL,
    `store_id`   int    DEFAULT NULL,
    `parent_id`  int    DEFAULT NULL,
    `user_id`    BIGINT DEFAULT NULL,
    `likes`      bigint DEFAULT NULL,
    `dislikes`   bigint DEFAULT NULL,
    `is_deleted` enum('Y','N') DEFAULT NULL,
    `message`    text,
    `created_at` mediumtext,
    `updated_at` mediumtext,
    PRIMARY KEY (`id`),
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (store_id) REFERENCES stores (id),
    UNIQUE KEY `slug` (`slug`)
);

