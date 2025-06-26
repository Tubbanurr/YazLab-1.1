create table malzemeler
(
    malzemeid    serial
        primary key,
    malzemeadi   varchar(255)   not null,
    toplammiktar varchar(50),
    malzemebirim varchar(50),
    birimfiyat   numeric(10, 2) not null
);

alter table malzemeler
    owner to postgres;

