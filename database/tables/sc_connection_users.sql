CREATE TABLE service.sc_connection_users (
	login varchar NOT NULL,
	password_hash varchar NOT NULL,
	id numeric NOT NULL,
	CONSTRAINT sc_connection_users_un UNIQUE (login),
	CONSTRAINT sc_connection_users_pk PRIMARY KEY (id)
);
COMMENT ON TABLE service.sc_connection_users IS 'Users that can get connection to database';
