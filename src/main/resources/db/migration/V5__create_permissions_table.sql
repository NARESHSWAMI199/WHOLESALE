CREATE TABLE `permissions`
(
    `id`             int          NOT NULL AUTO_INCREMENT,
    `permission`     varchar(50) UNIQUE    DEFAULT NULL,
    `display_name`   varchar(100) UNIQUE   DEFAULT NULL,
    `permission_for` varchar(100) NOT NULL DEFAULT 'Others',
    PRIMARY KEY (`id`)
);