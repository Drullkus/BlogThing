drop table users if exists;
drop table posts if exists;
drop table comments if exists;

create table users
(
    id       integer identity primary key,
    admin    boolean      not null,
    email    varchar(255) not null,
    githubid bigint,
    hash     varchar(255),
    name     varchar(255) not null,
    session  varchar(255)
);

create table posts
(
    id             integer identity primary key,
    author         integer not null,
    edit_timestamp bigint,
    text           text    not null,
    timestamp      bigint  not null
);

create table comments
(
    id             integer identity primary key,
    author         integer not null,
    edit_timestamp bigint,
    text           text    not null,
    timestamp      bigint  not null,
    posts_id       integer
);

alter table comments add constraint fk_comment_post_id foreign key (posts_id) references posts (id);