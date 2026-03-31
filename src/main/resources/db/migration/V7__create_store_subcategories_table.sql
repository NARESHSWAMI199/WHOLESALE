CREATE TABLE `store_subcategories`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `slug`        varchar(50)                                                   DEFAULT NULL,
    `category_id` int                                                           DEFAULT NULL,
    `subcategory` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `icon`        longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
    `updated_at`  bigint                                                        DEFAULT NULL,
    `is_deleted`  enum('Y','N') DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `store_subcategory_unique` (`slug`),
    FOREIGN KEY (category_id) REFERENCES store_subcategories (id)
);