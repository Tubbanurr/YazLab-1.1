create table tarifler
(
    tarifid         serial
        primary key,
    tarifadi        varchar(255)   not null,
    kategori        varchar(100)   not null,
    hazirlamasuresi integer        not null,
    talimatlar      text           not null,
    maliyet         numeric(10, 2) not null,
    gorselyolu      varchar(255)
);

alter table tarifler
    owner to postgres;

