begin;

create temporary table newprops (
    name character varying,
    value character varying
);

insert into organism (
    abbreviation, common_name, genus, species
) values (
    'Ncaninum', 'Ncaninum', 'Neospora', 'caninum'
);

insert into newprops
    values ('translationTable', '1'),
           ('taxonId', 29176),
           ('curatorName', 'Adam Reid'),
           ('curatorEmail', 'ar11@sanger.ac.uk'),
           ('htmlFullName', '<i>Neospora caninum</i>'),
           ('htmlShortName', '<i>N. caninum</i>'),
           ('shortName', 'N. caninum')
;

insert into organismprop (
    organism_id, type_id, value
) (
    select currval('organism_organism_id_seq' :: regclass), cvterm.cvterm_id, newprops.value
    from cvterm
    join newprops on newprops.name = cvterm.name
    join cv using (cv_id)
    where cv.name = 'genedb_misc'
);

drop table newprops;

commit;
