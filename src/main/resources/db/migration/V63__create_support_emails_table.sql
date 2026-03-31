CREATE TABLE `support_emails`
(
    `id`           int NOT NULL AUTO_INCREMENT,
    `email`        varchar(100) DEFAULT NULL,
    `password_key` varchar(200) DEFAULT NULL,
    `support_type` enum('SUPPORT','ADVERTISEMENT','SUPER_ADMIN','PROMOTION') DEFAULT NULL,
    PRIMARY KEY (`id`)
);