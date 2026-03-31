CREATE TABLE `item_categories`
(
    `id`         int                                                          NOT NULL AUTO_INCREMENT,
    `slug`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `category`   varchar(50) DEFAULT NULL,
    `icon`       longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `is_deleted` enum('Y','N') DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `item_category_slug` (`slug`),
    UNIQUE KEY `item_category` (`category`)
);