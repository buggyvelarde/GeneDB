begin;

create temporary table missing_exons as
select organism_id
     , transcript.feature_id as transcript_feature_id
     , transcript.uniquename as transcript_uniquename
     , nextval('feature_feature_id_seq'::regclass) as exon_feature_id
from feature transcript
where transcript.type_id in (
      321 /*mRNA*/
    , 339 /*rRNA*/
    , 340 /*tRNA*/
    , 361 /*snRNA*/
)
and not exists (
    select *
    from feature_relationship exon_transcript
    join feature exon on exon_transcript.subject_id = exon.feature_id
    where exon_transcript.object_id = transcript.feature_id
    and   exon.type_id = 234 /*exon*/
)
;

insert into feature
    (feature_id, organism_id, type_id, uniquename)
    (select exon_feature_id, organism_id, 234, transcript_uniquename || ':auto_exon'
        from missing_exons)
;

insert into feature_relationship
    (subject_id, object_id, type_id)
    (select exon_feature_id, transcript_feature_id, 42 /*relationship:part_of*/
        from missing_exons)
;

insert into featureloc
    (feature_id, srcfeature_id, fmin, fmax, strand, locgroup, rank)
    (select exon_feature_id, srcfeature_id, fmin, fmax, strand, locgroup, rank
        from missing_exons
        join featureloc on missing_exons.transcript_feature_id = featureloc.feature_id)
;

rollback;
