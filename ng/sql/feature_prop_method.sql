begin;

create temporary table genedb_featureprop_terms (
      name character varying(1024) not null
    , description text not null
    , cv_id integer not null
    , parent_term_id integer
    , cvterm_id integer not null
    , db_id integer not null
    , dbxref_id integer not null
);
insert into genedb_featureprop_terms (
    select 'method' as name
         , 'Free text note field for method' as description
         , cv.cv_id
         , cvterm.cvterm_id as parent_term_id
         , nextval('cvterm_cvterm_id_seq'::regclass) as cvterm_id
         , db.db_id
         , nextval('dbxref_dbxref_id_seq'::regclass) as dbxref_id
    from db, cv, cvterm
    where db.name = 'genedb_misc'
    and   cv.name = 'genedb_misc'
    and   cvterm.cv_id = cv.cv_id
    and   cvterm.name = 'feature_props'
);

insert into dbxref (dbxref_id, db_id, accession, description) (
    select dbxref_id, db_id, name, description
    from genedb_featureprop_terms
);

insert into cvterm (cvterm_id, cv_id, name, definition, dbxref_id)
    (select cvterm_id, cv_id, name, description, dbxref_id from genedb_featureprop_terms)
;

insert into cvterm_relationship (type_id, subject_id, object_id) (
    select type.cvterm_id, genedb_featureprop_terms.cvterm_id, genedb_featureprop_terms.parent_term_id
    from genedb_featureprop_terms, cvterm type join cv type_cv using (cv_id)
    where type_cv.name = 'relationship' and type.name = 'is_a'
);

commit;