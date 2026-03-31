-- test.plan_offers definition

CREATE TABLE `service_plans_offers`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `plan_id`     int          DEFAULT NULL,
    `name`        varchar(500) DEFAULT NULL,
    `discount`    float        DEFAULT NULL,
    `status`      enum('A','D') DEFAULT NULL,
    `description` text,
    `start_date`  bigint       DEFAULT NULL,
    `expiry_date` bigint       DEFAULT NULL,
    `created_at`  bigint       DEFAULT NULL,
    `created_by`  int          DEFAULT NULL,
    `updated_at`  bigint       DEFAULT NULL,
    `updated_by`  int          DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (plan_id) REFERENCES service_plans (id)
);