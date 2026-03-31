CREATE TABLE `store_categories`
(
    `id`         int NOT NULL AUTO_INCREMENT,
    `slug`       varchar(50)                                                   DEFAULT NULL,
    `category`   varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `icon`       longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
    `is_deleted` enum('Y','N') DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `store_category_unique` (`slug`)
);