-- Create schema if not exists
CREATE TABLE IF NOT EXISTS users
    (
        id         BIGSERIAL NOT NULL              ,
        first_name VARCHAR(255) NOT NULL           ,
        last_name  VARCHAR(255) NOT NULL           ,
        user_name  VARCHAR(255) NOT NULL UNIQUE    ,
        email      VARCHAR(255) NOT NULL UNIQUE    ,
        role       VARCHAR(255) NOT NULL           ,
        created_at TIMESTAMP NOT NULL DEFAULT NOW(),
        CONSTRAINT pk_users PRIMARY KEY (id)
    );