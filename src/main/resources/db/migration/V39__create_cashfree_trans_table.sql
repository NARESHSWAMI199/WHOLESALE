CREATE TABLE `cashfree_trans`
(
    `id`              bigint NOT NULL AUTO_INCREMENT,
    `slug`            varchar(255) DEFAULT NULL,
    `user_id`         BIGINT NOT NULL,
    `order_id`        varchar(255) DEFAULT NULL,
    `cf_payment_id`   varchar(255) DEFAULT NULL,
    `status`          varchar(255) DEFAULT NULL,
    `amount`          varchar(255) DEFAULT NULL,
    `currency`        varchar(50)  DEFAULT NULL,
    `message`         text,
    `payment_time`    varchar(255) DEFAULT NULL,
    `payment_group`   varchar(255) DEFAULT NULL,
    `payment_method`  varchar(255) DEFAULT NULL,
    `actual_response` text,
    `created_at`      mediumtext,
    PRIMARY KEY (`id`),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);
