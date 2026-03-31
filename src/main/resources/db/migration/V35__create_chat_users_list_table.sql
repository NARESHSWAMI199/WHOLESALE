CREATE TABLE `chat_users_list`
(
    `id`                   int NOT NULL AUTO_INCREMENT,
    `user_id`              BIGINT DEFAULT NULL,
    `chat_user_id`         BIGINT DEFAULT NULL,
    `sender_accept_status` enum('P','A','R') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'P',
    PRIMARY KEY (`id`),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (chat_user_id) REFERENCES users (user_id)

);

