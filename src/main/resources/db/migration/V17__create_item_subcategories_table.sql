CREATE TABLE `item_subcategories`
(
    `id`          int                                                          NOT NULL AUTO_INCREMENT,
    `slug`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `category_id` int                                                           DEFAULT NULL,
    `subcategory` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `unit`        varchar(50)                                                   DEFAULT NULL,
    `icon`        longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
    `updated_at`  bigint                                                        DEFAULT NULL,
    `is_deleted`  enum('Y','N') DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (category_id) REFERENCES item_categories (id),
    UNIQUE KEY `item_subcategory_slug` (`slug`),
    UNIQUE KEY `item_subcategory_unique_1` (`subcategory`)
);