insert into users values (1, true, 'a@person.test', null, '$2a$12$8Ytu3rgxd063HGrtCfpQoOTfAHcLE5VDaMExEdMdVSocYnhDpyRKm', 'Person A', null);
insert into users values (2, false, 'b@person.test', null, '$2a$12$adcjMRpJicFHfUJOtGMNd.5v1n5nPi5Qg2TQsxLNTxKErskBBYOli', 'Person B', 'HQZzVijA318CvDdEqW');
insert into users values (3, false, 'c@person.test', null, '$2a$12$fNYu6RKfcOG5NsKD8rFjQ.UzVa7PIvy6UzQznBKqmJWbPV/b5FzJ6', 'Person C', null);

insert into posts values (1, 1, null, 'First!', 1645226285);
insert into posts values (2, 2, 1645226406, 'This is an Edited Post!', 1645226394);
insert into posts values (3, 3, null, 'My First Post!', 1645226400);
insert into posts values (4, 3, null, 'My Second Post!', 1645226425);
insert into posts values (5, 2, null, 'Wow!', 1645226444);

insert into comments values (1, 2, null, 'First!', 1645226511, 1);
insert into comments values (2, 3, 1645226539, 'Second!', 1645226534, 1);
insert into comments values (3, 1, null, 'Wowow!', 1645226576, 5);