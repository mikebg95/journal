ALTER TABLE tags ADD CONSTRAINT tags_value_trimmed   CHECK (value = btrim(value));
ALTER TABLE tags ADD CONSTRAINT tags_value_not_blank CHECK (length(value) > 0);
ALTER TABLE tags ADD CONSTRAINT tags_value_lowercase CHECK (value = lower(value));