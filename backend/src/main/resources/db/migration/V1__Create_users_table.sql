CREATE TABLE users (
   id          BIGSERIAL PRIMARY KEY,
   first_name  VARCHAR(50) NOT NULL,
   last_name   VARCHAR(50) NOT NULL,
   email       VARCHAR(255) NOT NULL UNIQUE,
   password    VARCHAR(255) NOT NULL,
   created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
   updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
