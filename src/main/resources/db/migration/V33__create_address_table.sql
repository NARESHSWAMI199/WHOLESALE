CREATE TABLE `address`
(
    `id`         int NOT NULL AUTO_INCREMENT,
    `slug`       varchar(50) DEFAULT NULL,
    `zip_code`   varchar(6)  DEFAULT NULL,
    `street`     text,
    `city`       int         DEFAULT NULL,
    `state`      int         DEFAULT NULL,
    `latitude`   float       DEFAULT NULL,
    `altitude`   float       DEFAULT NULL,
    `created_at` bigint      DEFAULT NULL,
    `created_by` int         DEFAULT NULL,
    `updated_at` bigint      DEFAULT NULL,
    `updated_by` int         DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (state) REFERENCES states (id),
    FOREIGN KEY (city) REFERENCES cities (id),
    UNIQUE KEY `slug` (`slug`)
);

