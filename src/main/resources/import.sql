drop table if exists UserConnection;

create table UserConnection (userId varchar(255) not null,
    providerId varchar(255) not null,
    providerUserId varchar(255),
    rank int not null,
    displayName varchar(255),
    profileUrl varchar(512),
    imageUrl varchar(512),
    accessToken varchar(255) not null,
    secret varchar(255),
    refreshToken varchar(255),
    expireTime bigint,
    primary key (userId, providerId, providerUserId));
create unique index UserConnectionRank on UserConnection(userId, providerId, rank);

--Default user
insert into user values (null, '{UWGTzG420M6EFp602vuwLSqhudwjGCYVcGt1YJ8B6Oc=}c7ea13510646e1bf5efd4f1190d4e2a8cde382512b7bcbaa5ede07a3bb18a2e7', 'maicon');

--Default roles
insert into role values (null, 'ADMIN');
insert into role values (null, 'DEFAULT_USER');

insert into user_role values (1, 1);
insert into user_role values (2, 1);