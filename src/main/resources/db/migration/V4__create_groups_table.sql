CREATE TABLE `groups`
(
    `id`         int         NOT NULL AUTO_INCREMENT,
    `slug`       varchar(50) NOT NULL,
    `name`       varchar(50) NOT NULL,
    `is_deleted` enum('Y','N') DEFAULT NULL,
    `created_at` bigint DEFAULT NULL,
    `created_by` int    DEFAULT NULL,
    `updated_at` bigint DEFAULT NULL,
    `updated_by` int    DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `slug` (`slug`),
    UNIQUE KEY `name` (`name`)
);