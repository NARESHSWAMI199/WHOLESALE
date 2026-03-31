CREATE TABLE `store_notifications`
(
    `id`           BIGINT NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT       DEFAULT NULL,
    `store_id`     int          DEFAULT NULL,
    `title`        varchar(100) DEFAULT NULL,
    `message_body` text,
    `is_deleted`   enum('Y','N') DEFAULT NULL,
    `created_at`   bigint       DEFAULT NULL,
    `created_by`   int          DEFAULT NULL,
    `seen`         enum('Y','N') DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (store_id) REFERENCES stores (id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);