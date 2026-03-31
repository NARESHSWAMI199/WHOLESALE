CREATE TABLE `cities`
(
    `id`        int NOT NULL AUTO_INCREMENT,
    `city_name` varchar(50) DEFAULT NULL,
    `state_id`  int         DEFAULT NULL,
    `status`    enum('A','D') DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (state_id) REFERENCES states (id)
);
