begin;

create temporary table newprops (
    name character varying,
    value character varying
);

insert into organism (
    abbreviation, common_name, genus, species
) values (
    'Styphimurium', 'Styphimurium', 'Salmonella', 'typhimurium'
);

insert into newprops
    values ('translationTable', '11'),
           ('taxonId', 568708),
           ('curatorName', 'Nick Thomson'),
           ('curatorEmail', 'nrt@sanger.ac.uk'),
           ('htmlFullName', '<i>Salmonella enterica</i> subsp. enterica serovar Typhimurium str. D23580'),
           ('htmlShortName', '<i>S. Typhimurium</i>'),
           ('shortName', 'S. Typhimurium')
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
