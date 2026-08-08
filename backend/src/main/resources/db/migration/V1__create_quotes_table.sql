CREATE TABLE quotes (
    id     BIGSERIAL PRIMARY KEY,
    text   VARCHAR(500) NOT NULL,
    author VARCHAR(100)
);

INSERT INTO quotes (text, author) VALUES
    ('Простотата е предпоставка за надеждност.', 'Edsger Dijkstra'),
    ('Преждевременната оптимизация е коренът на всяко зло.', 'Donald Knuth'),
    ('Кодът се чете далеч по-често, отколкото се пише.', 'Guido van Rossum'),
    ('Най-добрият код е кодът, който не си написал.', NULL);
