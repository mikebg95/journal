CREATE TABLE todos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    value VARCHAR(1000) NOT NULL,
    entry_id BIGINT NOT NULL,
    CONSTRAINT fk_entry_id
        FOREIGN KEY (entry_id) REFERENCES entries (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_todos_entry_id ON todos (entry_id);