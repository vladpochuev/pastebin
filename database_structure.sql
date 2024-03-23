CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR NOT NULL UNIQUE,
    password VARCHAR NOT NULL
);

CREATE TABLE deactivated_tokens (
    id UUID PRIMARY KEY,
    expirationTime TIMESTAMP NOT NULL
    CONSTRAINT expiration_time_check CHECK (expirationTime > NOW())
);

CREATE TABLE bin (
    id VARCHAR(10) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    messageUUID VARCHAR(50) NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    color VARCHAR(7) NOT NULL,
    expirationTime TIMESTAMPTZ,
    amountOfTime VARCHAR(30) NOT NULL,
    username VARCHAR(50) NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username),
    CONSTRAINT check_x CHECK (x >= -100 AND x <= 100),
    CONSTRAINT check_y CHECK (y >= -100 AND y <= 100),
    CONSTRAINT expiration_time_check CHECK (expirationTime > NOW()),
    CONSTRAINT check_unique UNIQUE (x, y)
)