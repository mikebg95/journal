CREATE TABLE tags (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    value VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX tags_value_normalized_key ON tags (lower(btrim(value)));