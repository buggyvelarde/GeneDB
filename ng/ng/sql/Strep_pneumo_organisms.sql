begin;
insert into organism (genus, species, abbreviation, common_name)
        values ('Streptococcus', 'pneumoniae ATCC 700669',
                'Spneumoniae_ATCC700669', 'Spneumoniae_ATCC700669')
;
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, 'Nick Croucher'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'curatorName'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, 'nc3@sanger.ac.uk'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'curatorEmail'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '561276'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'taxonId'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '<i>Streptococcus pneumoniae</i> ATCC 700669'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'htmlFullName'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '<i>S. pneumoniae</i> ATCC 700669'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'htmlShortName'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '11'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'translationTable'
);
commit;

begin;
insert into organism (genus, species, abbreviation, common_name)
        values ('Streptococcus', 'pneumoniae D39',
                'Spneumoniae_D39', 'Spneumoniae_D39')
;
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, 'Nick Croucher'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'curatorName'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, 'nc3@sanger.ac.uk'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'curatorEmail'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '373153'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'taxonId'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '<i>Streptococcus pneumoniae</i> D39'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'htmlFullName'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '<i>S. pneumoniae</i> D39'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'htmlShortName'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '11'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'translationTable'
);
commit;

begin;
insert into organism (genus, species, abbreviation, common_name)
        values ('Streptococcus', 'pneumoniae OXC141',
                'Spneumoniae_OXC141', 'Spneumoniae_OXC141')
;
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, 'Nick Croucher'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'curatorName'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, 'nc3@sanger.ac.uk'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'curatorEmail'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '1313'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'taxonId'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '<i>Streptococcus pneumoniae</i> OXC141'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'htmlFullName'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '<i>S. pneumoniae</i> OXC141'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'htmlShortName'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '11'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'translationTable'
);
commit;


begin;
insert into organism (genus, species, abbreviation, common_name)
        values ('Streptococcus', 'pneumoniae TIGR4',
                'Spneumoniae_TIGR4', 'Spneumoniae_TIGR4')
;
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, 'Nick Croucher'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'curatorName'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, 'nc3@sanger.ac.uk'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'curatorEmail'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '170187'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'taxonId'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '<i>Streptococcus pneumoniae</i> TIGR4'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'htmlFullName'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '<i>S. pneumoniae</i> TIGR4'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'htmlShortName'
);
insert into organismprop (organism_id, type_id, value) (
        select currval('organism_organism_id_seq'::regclass),
                cvterm.cvterm_id, '11'
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'translationTable'
);
commit;
