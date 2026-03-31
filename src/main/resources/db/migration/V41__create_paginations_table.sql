CREATE TABLE `paginations`
(
    `id`        int         NOT NULL AUTO_INCREMENT,
    `field_for` varchar(50) NOT NULL,
    `can_see`   enum('S','W','B') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'S',
    PRIMARY KEY (`id`)
);