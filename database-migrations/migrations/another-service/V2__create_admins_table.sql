CREATE TABLE IF NOT EXISTS admins
    (
        id         BIGSERIAL NOT NULL              ,
        username   VARCHAR(255) NOT NULL UNIQUE    ,
        email      VARCHAR(255) NOT NULL UNIQUE    ,
        role       VARCHAR(255) NOT NULL           ,
        created_at TIMESTAMP NOT NULL DEFAULT NOW(),
        CONSTRAINT pk_admins PRIMARY KEY (id)
    );