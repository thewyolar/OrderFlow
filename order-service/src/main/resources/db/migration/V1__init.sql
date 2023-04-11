create table merchants
(
    id uuid not null primary key,
    name varchar(255),
    site_url varchar(255)
);

create table orders
(
    id uuid not null primary key,
    amount double precision,
    currency varchar(255),
    date_create timestamp(6),
    date_update timestamp(6),
    expired_date timestamp(6),
    name varchar(255),
    status varchar(255),
    merchant_id uuid
);

create table transactions
(
    id uuid not null primary key,
    amount double precision,
    context varchar(2048),
    currency varchar(255),
    date_create timestamp(6),
    date_update timestamp(6),
    status varchar(255),
    type varchar(255),
    merchant_id uuid,
    order_id uuid
);

alter table if exists orders
    add constraint merchant_id_fk
    foreign key (merchant_id) references merchants;

alter table if exists transactions
    add constraint order_id_fk
        foreign key (order_id) references orders;

alter table if exists transactions
    add constraint merchant_id_fk
        foreign key (merchant_id) references merchants;
