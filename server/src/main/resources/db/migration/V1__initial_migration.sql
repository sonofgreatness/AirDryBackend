CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================
-- Base table parent structure
-- Each child table gets its own UUID PK & common columns
-- ============================

-- USER TYPES
CREATE TABLE user_type (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID NULL,
    updated_by UUID NULL,
    displayname VARCHAR(50) NOT NULL
);

-- ORGANIZATIONS
CREATE TABLE organization (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID NULL,
    updated_by UUID NULL,
    displayname VARCHAR(42) NOT NULL,
    logo BYTEA NULL
);

-- DEPARTMENTS
CREATE TABLE department (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID NULL,
    updated_by UUID NULL,
    displayname VARCHAR(42) NOT NULL,
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE
);

-- SECTIONS
CREATE TABLE section (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID NULL,
    updated_by UUID NULL,
    displayname VARCHAR(42) NOT NULL,
    department_id UUID NOT NULL REFERENCES department(id) ON DELETE CASCADE
);

-- APP USERS
CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID NULL,
    updated_by UUID NULL,

    name VARCHAR(42) NOT NULL,
    surname VARCHAR(42) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    secondary_phone_number VARCHAR(15) UNIQUE NULL,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    user_type_id UUID NULL REFERENCES user_type(id) ON DELETE SET NULL,
    organization_id UUID NULL REFERENCES organization(id) ON DELETE SET NULL,
    department_id UUID NULL REFERENCES department(id) ON DELETE SET NULL,
    section_id UUID NULL REFERENCES section(id) ON DELETE SET NULL,

    email VARCHAR(255) UNIQUE NULL,
    password_hash VARCHAR(255) NULL
);