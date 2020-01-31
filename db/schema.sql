create table users (
    id int generated always as identity primary key,
    user_name text,
    display_name text,
    role text
);

create table repositories (
    id int generated always as identity primary key,
    name text
);

create table directories (
    id int generated always as identity primary key,
    repository_id int,
    name text
);

create table permissions (
    id int generated always as identity primary key,
    directory_id int,
    user_name text,
    permission text
);

create table managerships (
    id int generated always as identity primary key,
    repository_id int,
    user_id integer
);

create table repository_users (
    id int generated always as identity primary key,
    repository_id int,
    user_id int
);
