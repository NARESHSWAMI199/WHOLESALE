CREATE TABLE `chats`
(
    `id`                  bigint NOT NULL AUTO_INCREMENT,
    `parent_id`           bigint                                                        DEFAULT NULL,
    `user_id`             BIGINT                                                        DEFAULT NULL,
    `receiver_key`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `sender_key`          varchar(255)                                                  DEFAULT NULL,
    `message`             text,
    `is_sent`             enum('S','F') DEFAULT 'S',
    `images`              text,
    `created_at`          mediumtext,
    `updated_at`          mediumtext,
    `seen`                tinyint(1) DEFAULT NULL,
    `is_sender_deleted`   enum('N','Y','H') DEFAULT NULL,
    `is_receiver_deleted` enum('N','Y','H') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (parent_id) REFERENCES chats (id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

