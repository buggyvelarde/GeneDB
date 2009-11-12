begin;

create temporary table newprops (
    name character varying,
    value character varying
) on commit drop;

insert into organism (
    abbreviation, common_name, genus, species
) values (
    'Styphi', 'Styphi', 'Salmonella', 'typhi'
);

insert into newprops
    values ('translationTable', '11'),
           ('taxonId', 90370),
           ('curatorName', 'Nick Thomson'),
           ('curatorEmail', 'nrt@sanger.ac.uk'),
           ('htmlFullName', '<i>Salmonella enterica</i> subsp. enterica serovar Typhi'),
           ('htmlShortName', '<i>S. Typhi</i>'),
           ('shortName', 'S. Typhi')
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

commit;
