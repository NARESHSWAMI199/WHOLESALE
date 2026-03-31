CREATE TABLE wallet_transactions
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    slug             VARCHAR(255) NOT NULL,
    user_id          BIGINT       NOT NULL,
    amount           FLOAT        NOT NULL,
    created_at       BIGINT       NOT NULL,
    transaction_type VARCHAR(50)  NOT NULL,
    status           VARCHAR(50)  NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    UNIQUE KEY uk_slug (slug)
);