CREATE TABLE `store_permissions`
(
    `id`                 int          NOT NULL AUTO_INCREMENT,
    `permission`         varchar(50) UNIQUE    DEFAULT NULL,
    `display_name`       varchar(100)          DEFAULT NULL,
    `default_permission` enum('Y','N') DEFAULT NULL,
    `permission_for`     varchar(100) NOT NULL DEFAULT 'Others',
    PRIMARY KEY (`id`)
);