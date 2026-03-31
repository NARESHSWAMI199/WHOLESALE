CREATE TABLE `phonepe_trans`
(
    `id`                      bigint NOT NULL AUTO_INCREMENT,
    `merchant_transaction_id` varchar(50)  DEFAULT NULL,
    `user_id`                 BIGINT       DEFAULT NULL,
    `order_id`                bigint       DEFAULT NULL,
    `amount`                  float        DEFAULT NULL,
    `x_verify`                text,
    `transaction_id`          varchar(200) DEFAULT NULL,
    `bank_id`                 varchar(200) DEFAULT NULL,
    `response_code`           varchar(50)  DEFAULT NULL,
    `payment_type`            varchar(50)  DEFAULT NULL,
    `state`                   varchar(50)  DEFAULT NULL,
    `code`                    varchar(100) DEFAULT NULL,
    `message`                 text,
    `actual_response`         text,
    `status`                  enum('P','S','F') DEFAULT NULL,
    `created_at`              bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (order_id) REFERENCES item_orders (id)
);