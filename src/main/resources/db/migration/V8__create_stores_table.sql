CREATE TABLE stores
(
    id             INT          NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       DEFAULT NULL,
    slug           VARCHAR(50)  DEFAULT NULL,
    category_id    INT          DEFAULT NULL,
    subcategory_id INT          DEFAULT NULL,
    name           VARCHAR(100) NOT NULL,
    avatar         TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
    address_id     INT          DEFAULT NULL,
    email          VARCHAR(100) DEFAULT NULL,
    phone          VARCHAR(12)  DEFAULT NULL,
    description    TEXT,
    rating         FLOAT        DEFAULT NULL,
    status         ENUM('A','D')   DEFAULT NULL,
    is_deleted     ENUM('Y','N')   DEFAULT NULL,
    expiry_date    BIGINT       DEFAULT NULL,
    created_at     BIGINT       DEFAULT NULL,
    created_by     INT          DEFAULT NULL,
    updated_at     BIGINT       DEFAULT NULL,
    updated_by     INT          DEFAULT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_phone (phone),

    CONSTRAINT fk_stores_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_stores_category FOREIGN KEY (category_id) REFERENCES store_categories (id),
    CONSTRAINT fk_stores_subcategory FOREIGN KEY (subcategory_id) REFERENCES store_subcategories (id)
);