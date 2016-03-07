CREATE TABLE users (
  id integer NOT NULL,
  active boolean,
  hashed_password bytea,
  salt bytea,
  username character varying(255) NOT NULL,
  CONSTRAINT users_pkey PRIMARY KEY (id),
  CONSTRAINT users_username_key UNIQUE (username)
);
CREATE TABLE users_role (
  user_id uuid NOT NULL,
  roles_id integer NOT NULL,
  CONSTRAINT users_role_pkey PRIMARY KEY (user_id, roles_id),
  CONSTRAINT fk_users_role_roles_id FOREIGN KEY (roles_id)
      REFERENCES role (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_users_role_user_id FOREIGN KEY (user_id)
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
CREATE TABLE role (
  id integer NOT NULL,
  role_type character varying(32),
  CONSTRAINT role_pkey PRIMARY KEY (id)
);
CREATE TABLE einkaufsliste
(
  id uuid NOT NULL,
  name character varying(255),
  owner_id integer,
  CONSTRAINT einkaufsliste_pkey PRIMARY KEY (id),
  CONSTRAINT fk_einkaufsliste_owner_id FOREIGN KEY (owner_id)
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
CREATE TABLE einkauf
(
  id integer NOT NULL,
  end_ts timestamp without time zone,
  name character varying(255) NOT NULL,
  start_ts timestamp without time zone NOT NULL,
  "einkÄufer_id" integer,
  ekl_id integer,
  location_id integer,
  CONSTRAINT einkauf_pkey PRIMARY KEY (id),
  CONSTRAINT "fk_einkauf_einkÄufer_id" FOREIGN KEY ("einkÄufer_id")
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_einkauf_ekl_id FOREIGN KEY (ekl_id)
      REFERENCES einkaufsliste (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_einkauf_location_id FOREIGN KEY (location_id)
      REFERENCES location (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
CREATE TABLE einkauf_item
(
  einkauf_id integer NOT NULL,
  boughtitems_id integer NOT NULL,
  CONSTRAINT einkauf_item_pkey PRIMARY KEY (einkauf_id, boughtitems_id),
  CONSTRAINT fk_einkauf_item_boughtitems_id FOREIGN KEY (boughtitems_id)
      REFERENCES item (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_einkauf_item_einkauf_id FOREIGN KEY (einkauf_id)
      REFERENCES einkauf (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
