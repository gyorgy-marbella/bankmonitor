CREATE TABLE IF NOT EXISTS transaction (
                                           id INT NOT NULL PRIMARY KEY,
                                           data VARCHAR(1000) NOT NULL,
                                           created_at TIMESTAMP NOT NULL default NOW()
);

