begin;

create temporary table protein_property_terms (
      name character varying(1024) not null
    , description text not null
);
insert into protein_property_terms (name, description)
values ('GPI_anchored', 'A flag to indicate that the protein is GPI-anchored, as predicted by dgpi')
     , ('SignalP_prediction', 'The SignalP prediction for this protein. Possible values are ''Signal peptide'' and ''Signal anchor''.')
     , ('signal_peptide_probability', 'The probability that this protein is a signal peptide, as predicted by SignalP.')
     , ('signal_anchor_probability', 'The probability that this protein is a signal anchor, as predicted by SignalP.')
     , ('PlasmoAP_score', 'A score denoting how likely this protein is to target the apicoplast, as predicted by PlasmoAP.')
;

create temporary table signal_peptide_property_terms (
      name character varying(1024) not null
    , description text not null
);
insert into signal_peptide_property_terms (name, description)
values ('cleavage_site_probability', 'The probability that this predicted cleavage site is a cleavage site, as predicted by SignalP')
;

create temporary table GPI_anchor_cleavage_site_property_terms (
      name character varying(1024) not null
    , description text not null
);
insert into GPI_anchor_cleavage_site_property_terms (name, description)
values ('GPI_cleavage_site_score', 'The score assigned to this GPI cleavage site by dgpi')
;


create temporary table genedb_feature_type_terms (
      name character varying(1024) not null
    , description text not null
);
insert into genedb_feature_type_terms (name, description)
values ('GPI_anchor_cleavage_site', 'the site on the C-terminus of the protein which is cleaved off prior to addition of the glycolipid')
;

create temporary table new_cvterms (
      cv_id integer not null
    , parent_id integer
    , child_id integer not null
    , name character varying(1024) not null
    , description text not null
    , dbxref_id integer default nextval('dbxref_dbxref_id_seq'::regclass)
);
insert into new_cvterms (cv_id, child_id, name, description) (
    select cv_id
        , nextval('dbxref_dbxref_id_seq'::regclass)
        ,'protein_property'
        , 'A property of a polypeptide feature'
        from cv
        where name = 'genedb_misc'
);
insert into new_cvterms (cv_id, child_id, name, description) (
    select cv_id
        , nextval('dbxref_dbxref_id_seq'::regclass)
        ,'signal_peptide_property'
        , 'A property of a signal_peptide feature'
        from cv
        where name = 'genedb_misc'
);
insert into new_cvterms (cv_id, child_id, name, description) (
    select cv_id
        , nextval('dbxref_dbxref_id_seq'::regclass)
        , 'GPI_anchor_cleavage_site_property'
        , 'A property of a GPI_anchor_cleavage_site feature'
        from cv
        where name = 'genedb_misc'
);

insert into new_cvterms (cv_id, parent_id, child_id, name, description)
    (select cv.cv_id
          , new_cvterms.child_id
          , nextval('dbxref_dbxref_id_seq'::regclass)
          , protein_property_terms.name
          , protein_property_terms.description
     from new_cvterms, protein_property_terms, cv
     where new_cvterms.name = 'protein_property'
     and cv.name = 'genedb_misc')
;
insert into new_cvterms (cv_id, parent_id, child_id, name, description)
    (select cv.cv_id
          , new_cvterms.child_id
          , nextval('dbxref_dbxref_id_seq'::regclass)
          , signal_peptide_property_terms.name
          , signal_peptide_property_terms.description
     from new_cvterms, signal_peptide_property_terms, cv
     where new_cvterms.name = 'signal_peptide_property'
     and cv.name = 'genedb_misc')
;
insert into new_cvterms (cv_id, parent_id, child_id, name, description)
    (select cv.cv_id
          , new_cvterms.child_id
          , nextval('dbxref_dbxref_id_seq'::regclass)
          , GPI_anchor_cleavage_site_property_terms.name
          , GPI_anchor_cleavage_site_property_terms.description
     from new_cvterms, GPI_anchor_cleavage_site_property_terms, cv
     where new_cvterms.name = 'GPI_anchor_cleavage_site_property'
     and cv.name = 'genedb_misc')
;
insert into cv (name) values ('genedb_feature_type');
insert into new_cvterms (cv_id, child_id, name, description)
    (select cv.cv_id
          , nextval('dbxref_dbxref_id_seq'::regclass)
          , genedb_feature_type_terms.name
          , genedb_feature_type_terms.description
     from genedb_feature_type_terms, cv
     where cv.name = 'genedb_feature_type'
     )
;



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
