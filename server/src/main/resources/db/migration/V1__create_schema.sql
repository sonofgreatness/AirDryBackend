
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE user_type (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  displayname VARCHAR(10) NOT NULL
);

CREATE TABLE organization (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  displayname VARCHAR(42) NOT NULL,
  logo BYTEA
);

CREATE TABLE department (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  displayname VARCHAR(42) NOT NULL,
  organization_id UUID NOT NULL REFERENCES organization(id)
);

CREATE TABLE section (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  displayname VARCHAR(42) NOT NULL,
  department_id UUID NOT NULL REFERENCES department(id)
);

CREATE TABLE app_user (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(42) NOT NULL,
  surname VARCHAR(42) NOT NULL,
  phone_number VARCHAR(20),
  secondary_phone_number VARCHAR(20),
  is_approved BOOLEAN DEFAULT FALSE,
  is_active BOOLEAN DEFAULT TRUE,
  user_type_id UUID REFERENCES user_type(id),
  organization_id UUID REFERENCES organization(id),
  section_id UUID REFERENCES section(id),
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

INSERT INTO user_type (id, displayname) VALUES
  (uuid_generate_v4(), 'admin'),
  (uuid_generate_v4(), 'capturer'),
  (uuid_generate_v4(), 'casual_worker'),
  (uuid_generate_v4(), 'super_user')


ON CONFLICT DO NOTHING;