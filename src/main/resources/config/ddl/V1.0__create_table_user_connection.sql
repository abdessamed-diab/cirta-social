create table UserConnection (
    userId varchar(255) not null,
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

create table cirta_authority (
    id integer not null,
    authority varchar(50) not null,
    primary key (id)
);
create unique index cirta_authority_index on cirta_authority(id);

create table cirta_user (
    id bigint not null,
    facebook_id varchar(255),
    first_name varchar(255) not null,
    language tinyint,
    last_name varchar(255) not null,
    name varchar(255) not null,
    password varchar(255),
    profile_image varchar(500000),
    user_temp_auth bigint,
    primary key (id)
);
create unique index cirta_user_index on cirta_user(id, facebook_id, name);
CREATE SEQUENCE cirta_user_sequence
    MINVALUE        1
    START WITH      1
    INCREMENT BY    1
    NOCACHE
    NOCYCLE;

create table user_authority (
    cirta_user_id bigint not null,
    cirta_authority_id integer not null,
    primary key (cirta_user_id, cirta_authority_id)
);
