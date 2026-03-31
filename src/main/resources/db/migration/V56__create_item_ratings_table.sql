CREATE TABLE item_ratings
(
    id         BIGINT NOT NULL AUTO_INCREMENT,
    item_id    BIGINT NULL,
    user_id    BIGINT NULL,
    rating     INT NULL,
    created_at BIGINT NULL,
    updated_at BIGINT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);