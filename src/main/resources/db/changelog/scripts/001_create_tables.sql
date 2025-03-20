--liquibase formatted sql

-- changeset ARM:build-roles
CREATE TABLE roles (
id BIGSERIAL PRIMARY KEY,
name VARCHAR(50) UNIQUE NOT NULL
);

-- changeset ARM:build-users
CREATE TABLE users (
id BIGSERIAL PRIMARY KEY,
username VARCHAR(255) UNIQUE NOT NULL,
password VARCHAR(255) NOT NULL,
is_blocked BOOLEAN NOT NULL DEFAULT FALSE
);



-- changeset ARM:build-user_roles
CREATE TABLE user_roles (
user_id BIGSERIAL NOT NULL,
role_id BIGSERIAL NOT NULL,
PRIMARY KEY (user_id, role_id),
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
