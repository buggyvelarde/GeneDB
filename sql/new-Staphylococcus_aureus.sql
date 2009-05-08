insert into organism (
    abbreviation, common_name, genus, species
) values (
    'Saureus_EMRSA15', 'Saureus_EMRSA15', 'Staphylococcus', 'aureus (EMRSA15)'
)
;


begin;

create temporary table newprops (
    name character varying,
    value character varying
);

insert into organism (
    abbreviation, common_name, genus, species
) values (
    'Saureus_TW20', 'Saureus_TW20', 'Staphylococcus', 'aureus (TW20)'
);

insert into newprops
    values ('translationTable', '11'),
           ('curatorName', 'Matt Holden'),
           ('curatorEmail', 'mh3@sanger.ac.uk'),
           ('htmlFullName', '<i>Staphylococcus aureus</i> TW20'),
           ('htmlShortName', '<i>S. aureus</i> TW20'),
           ('shortName', 'S. aureus TW20')
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
