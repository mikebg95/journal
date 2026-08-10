CREATE TABLE entries_tags (
    entry_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (entry_id, tag_id),
    CONSTRAINT fk_entry_id
        FOREIGN KEY (entry_id) REFERENCES entries (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_tag_id
        FOREIGN KEY (tag_id) REFERENCES tags (id)
            ON DELETE RESTRICT
);