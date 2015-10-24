INSERT INTO oauth_client( id, active, clientid, hashedpassword, salt) VALUES (1, true, 'AppClient001', E'\\xd5a5b13933cd6427c0e977492db8f8039dac5e7315389b1cc71bf4074822fa0a', E'\\xDEADBEEF');

INSERT INTO users( id, active, username, hashedpassword, salt) VALUES (1, true, 'thomas', E'\\xd5a5b13933cd6427c0e977492db8f8039dac5e7315389b1cc71bf4074822fa0a', E'\\xDEADBEEF');
INSERT INTO users( id, active, username, hashedpassword, salt) VALUES (2, true, 'user2', E'\\xd5a5b13933cd6427c0e977492db8f8039dac5e7315389b1cc71bf4074822fa0a', E'\\xDEADBEEF');

INSERT INTO role( role_type, id) VALUES ('OAUTH_USER', 1);
INSERT INTO oauth_user_role( id) VALUES (1);

INSERT INTO users_role( user_id, roles_id) VALUES (1, 1);
INSERT INTO users_role( user_id, roles_id) VALUES (2, 1);

INSERT INTO einkaufsliste( id, name, owner_id) VALUES (1, 'kw45', 1);
INSERT INTO item( id, data_version, menge, name, status, unit, ekl_id) VALUES (1, 1, 3, 'Körniger Frischkäse', 'needed', 'unit', 1);
INSERT INTO item( id, data_version, menge, name, status, unit, ekl_id) VALUES (2, 1, 10, 'Diät Puddings', 'needed', 'unit', 1);