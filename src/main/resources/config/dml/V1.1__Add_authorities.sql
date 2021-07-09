-- insert available authorities
insert into cirta_authority values (1, 'DEVELOPER');
insert into cirta_authority values (2, 'TESTER');

-- add admin
insert into cirta_user (id, name, password, first_name, last_name) values (1, 'tester', 'rahba', 'abdessamed', 'diab');
insert into user_authority values (1, 1);
insert into user_authority values (1, 2);
