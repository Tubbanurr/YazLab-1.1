create table tarifmalzeme
(
    tarifid       integer          not null
        references tarifler
            on delete cascade,
    malzemeid     integer          not null
        references malzemeler
            on delete cascade,
    malzememiktar double precision not null,
    primary key (tarifid, malzemeid)
);

alter table tarifmalzeme
    owner to postgres;

