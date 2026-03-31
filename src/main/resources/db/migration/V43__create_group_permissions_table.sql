CREATE TABLE group_permissions
(
    group_id      INT NOT NULL,
    permission_id INT NOT NULL,

    PRIMARY KEY (group_id, permission_id),

    CONSTRAINT fk_group_permissions_group
        FOREIGN KEY (`group_id`)
            REFERENCES `groups` (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT fk_group_permissions_permission
        FOREIGN KEY (`permission_id`)
            REFERENCES `permissions` (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);