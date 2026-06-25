CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50)
);


INSERT INTO users (username, password, role)
VALUES (
    'admin',
    '$2b$12$7HZh6xnghV898h7VMbadtu6Nq0A7wgBYcie8CJ5kdApstMtPldh52',
    'ADMIN'
)
ON CONFLICT (username) DO NOTHING;
