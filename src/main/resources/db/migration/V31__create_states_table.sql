CREATE TABLE `states`
(
    `id`         int NOT NULL AUTO_INCREMENT,
    `state_name` varchar(50) DEFAULT NULL,
    `status`     enum('A','D') DEFAULT NULL,
    PRIMARY KEY (`id`)
);
