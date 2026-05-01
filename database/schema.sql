CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE role (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name text NOT NULL,
    code citext NOT NULL UNIQUE,
    description text,
    is_system boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,
    version bigint NOT NULL DEFAULT 0
);

CREATE TABLE app_user (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    keycloak_sub varchar(255) NOT NULL UNIQUE,
    email citext NOT NULL UNIQUE,
    role_id bigint NOT NULL REFERENCES role(id) ON DELETE RESTRICT,
    is_active boolean NOT NULL DEFAULT true,
    last_login_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,
    version bigint NOT NULL DEFAULT 0
);