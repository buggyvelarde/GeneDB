begin;

create temporary table new_cvterms (
      cv_id integer not null
    , parent_id integer
    , child_id integer not null
    , name character varying(1024) not null
    , description text not null
    , dbxref_id integer default nextval('dbxref_dbxref_id_seq'::regclass)
);

insert into new_cvterms (cv_id, child_id, name, description) (
    select cv.cv_id
         , nextval('cvterm_cvterm_id_seq'::regclass)
         , 'archived_metadata'
         , 'Metadata about a feature that comes from some external source, typically a data file, and is archived in its source form'
    from cv
    where cv.name = 'genedb_misc'
);

insert into new_cvterms (cv_id, parent_id, child_id, name, description) (
    select new_cvterms.cv_id
         , new_cvterms.child_id
         , nextval('cvterm_cvterm_id_seq'::regclass)
         , 'EMBL_qualifier'
         , 'An unparsed EMBL qualifier, for archival purposes'
    from new_cvterms
);

insert into dbxref (db_id, dbxref_id, accession, description) (
    select db.db_id
         , new_cvterms.dbxref_id
         , new_cvterms.name
         , new_cvterms.description
    from db, new_cvterms
    where db.name = 'genedb_misc'
);

insert into cvterm (cv_id, cvterm_id, dbxref_id, name, definition) (
    select new_cvterms.cv_id
         , new_cvterms.child_id
         , new_cvterms.dbxref_id
         , new_cvterms.name
         , new_cvterms.description
    from new_cvterms
);

insert into cvterm_relationship (type_id, subject_id, object_id) (
    select isa.cvterm_id
         , new_cvterms.parent_id
         , new_cvterms.child_id
    from new_cvterms
       , cvterm isa
    join cv isa_cv using (cv_id)
    where isa.name = 'is_a'
      and isa_cv.name = 'relationship'
      and new_cvterms.parent_id is not null
);

commit;
