CREATE TABLE entries (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL CONSTRAINT entries_content_length_check CHECK (LENGTH(content) <= 20000),
    summary VARCHAR(500),
    mood VARCHAR(20) CONSTRAINT entries_mood_check CHECK(mood IN ('HAPPY', 'CALM', 'NEUTRAL', 'ANXIOUS', 'SAD', 'FRUSTRATED')),
    created_at TIMESTAMPTZ NOT NULL,
    last_updated TIMESTAMPTZ NOT NULL,
    analysed_at TIMESTAMPTZ
);