CREATE TABLE `user_paginations`
(
    `id`            int    NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT NOT NULL,
    `pagination_id` int    NOT NULL,
    `rows_number`   int    NOT NULL DEFAULT '25',
    PRIMARY KEY (`id`),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (pagination_id) REFERENCES paginations (id)
);