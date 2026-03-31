CREATE TABLE `chat_rooms`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `name`        varchar(255) DEFAULT NULL,
    `description` text,
    `slug`        varchar(255) DEFAULT NULL,
    `created_at`  bigint       DEFAULT NULL,
    `updated_at`  bigint       DEFAULT NULL,
    PRIMARY KEY (`id`)
);



