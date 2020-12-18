CREATE TABLE todo (
                      id SERIAL PRIMARY KEY,
                      txt VARCHAR NOT NULL,
                      completed boolean default false
);