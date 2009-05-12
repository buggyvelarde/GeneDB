begin;

create temporary table newprops (
    name character varying,
    value character varying
);

insert into organism (
    abbreviation, common_name, genus, species
) values (
    'Ldonovani', 'Ldonovani', 'Leishmania', 'donovani'
);

insert into newprops
    values ('translationTable', '1'),
           ('taxonId', 5661)
           ('curatorName', 'Christiane Hertz-Fowler'),
           ('curatorEmail', 'chf@sanger.ac.uk'),
           ('htmlFullName', '<i>Leishmania donovani</i>'),
           ('htmlShortName', '<i>L. donovani</i>'),
           ('shortName', 'L. donovani')
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

begin;

create temporary table newprops (
    name character varying,
    value character varying
);

insert into organism (
    abbreviation, common_name, genus, species
) values (
    'Lmexicana', 'Lmexicana', 'Leishmania', 'mexicana'
);

insert into newprops
    values ('translationTable', '1'),
           ('taxonId', 5665)
           ('curatorName', 'Christiane Hertz-Fowler'),
           ('curatorEmail', 'chf@sanger.ac.uk'),
           ('htmlFullName', '<i>Leishmania mexicana</i>'),
           ('htmlShortName', '<i>L. mexicana</i>'),
           ('shortName', 'L. mexicana')
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


/* Now use InsertPhylonode (genedb.db.adhoc.InsertPhylonode from genedb-access)
   to insert corresponding phylonodes */
