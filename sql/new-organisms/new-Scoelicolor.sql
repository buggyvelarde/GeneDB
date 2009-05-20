begin;

create temporary table newprops (
    name character varying,
    value character varying
);

insert into organism (
    abbreviation, common_name, genus, species
) values (
    'Scoelicolor', 'Scoelicolor', 'Streptomyces', 'coelicolor'
);

insert into newprops
    values ('translationTable', '11'),
           ('taxonId', 1902),
           ('curatorName', 'Matt Holden'),
           ('curatorEmail', 'mh3@sanger.ac.uk'),
           ('htmlFullName', '<i>Streptomyces coelicolor</i>'),
           ('htmlShortName', '<i>S. coelicolor</i>'),
           ('shortName', 'S. coelicolor')
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
