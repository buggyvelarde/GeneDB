begin;

create temporary table rnaz as
select organism_id
     , feature_id as transcript_id
     , uniquename
     , nextval('feature_feature_id_seq'::regclass) as gene_id
from feature
where uniquename like 'RNAz%'
;

insert into feature (
        organism_id, feature_id, uniquename, type_id
) (
        select organism_id, gene_id, uniquename, 792 from rnaz
);

insert into featureloc (
        feature_id, srcfeature_id, fmin, fmax, strand, locgroup, rank
) (
        select rnaz.gene_id
             , srcfeature_id, fmin, fmax, strand, locgroup, rank
        from rnaz
        join featureloc on rnaz.transcript_id = featureloc.feature_id
);

insert into feature_relationship (type_id, subject_id, object_id) (
        select 42, transcript_id, gene_id
        from rnaz
);

update feature set uniquename = uniquename || ':transcript'
where uniquename like 'RNAz%'
and type_id = 761
;


select feature_id, uniquename
from feature transcript
where transcript.type_id in (
      321 /*mRNA*/
    , 339 /*rRNA*/
    , 340 /*tRNA*/
    , 361 /*snRNA*/
    , 761 /*transcript*/
)
and 1 <> (
    select count(*)
    from feature_relationship transcript_gene
    join feature gene on transcript_gene.object_id = gene.feature_id
    where transcript_gene.subject_id = transcript.feature_id
    and   gene.type_id = 792 /*gene*/
)
;
