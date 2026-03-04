create table if not exists "user" (
    id UUID primary key,
    email varchar not null unique
);