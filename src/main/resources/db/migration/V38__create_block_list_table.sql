CREATE TABLE `block_list`
(
    `id`           int NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT DEFAULT NULL,
    `chat_user_id` BIGINT DEFAULT NULL,
    `created_at`   mediumtext,
    PRIMARY KEY (`id`),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (chat_user_id) REFERENCES users (user_id),
    UNIQUE KEY `block_list_unique` (`user_id`,`chat_user_id`)
);
