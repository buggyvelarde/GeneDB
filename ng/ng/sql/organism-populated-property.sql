begin;

insert into dbxref (db_id, accession, description) (
    select db_id, 'populated', 'This property is set on organisms that are populated with data'
    from db
    where name = 'genedb_misc'
);

insert into cvterm (cv_id, dbxref_id, name, definition) (
    select cv_id, currval('dbxref_dbxref_id_seq')
         , 'populated', 'This property is set on organisms that are populated with data'
    from cv
    where cv.name = 'genedb_misc'
);

insert into cvterm_relationship (type_id, subject_id, object_id) (
    select t.cvterm_id, currval('cvterm_cvterm_id_seq'), phylo_organism_prop_type.cvterm_id
    from cvterm t join cv t_cv using (cv_id)
       , cvterm phylo_organism_prop_type join cv phylo_organism_prop_type_cv using (cv_id)
    where t_cv.name = 'relationship' and t.name = 'is_a'
    and   phylo_organism_prop_type_cv.name = 'genedb_misc' and phylo_organism_prop_type.name = 'phylo_organism_prop'
);

insert into organismprop (organism_id, type_id) (
    select distinct organism_id, currval('cvterm_cvterm_id_seq')
    from feature join featureprop using (feature_id)
    where featureprop.type_id = (
        select cvterm_id
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc' and cvterm.name = 'top_level_seq'
    )
);

commit;
