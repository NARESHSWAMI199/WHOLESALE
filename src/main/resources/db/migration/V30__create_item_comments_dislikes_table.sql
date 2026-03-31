CREATE TABLE `item_comment_dislikes`
(
    `comment_id` bigint NOT NULL,
    `user_id`    BIGINT NOT NULL,
    PRIMARY KEY (`comment_id`, `user_id`),
    FOREIGN KEY (comment_id) REFERENCES item_comments (id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);
