
begin;

/* See PostgreSQL bug #2393: a unique constraint must be
   satisfied at all points _during_ the execution of an
   UPDATE, in violation of the SQL spec. We just have to
   drop the constraint here and add it back at the end.
   
   Note that Postgres DDL can participate in transactions
   fully, so rollback will even undo the alter table.
   This seems odd to someone from an Oracle background,
   but it's pretty convenient here. If something goes
   wrong and we roll back the transaction, everything
   will be back to normal. Also, other sessions will
   always see the constraint, so there's no danger that
   a non-unique name will slip in while our guard is
   down. I think I like this better than the Oracle way!
 */
alter table feature drop constraint feature_c1;

create or replace temporary view feature_name as
select feature.feature_id
     , feature.type_id
     , feature.name
     , feature.uniquename
     , feature_systematic_id.systematic_id
from feature
left join (
        select feature_id, synonym.synonym_sgml as systematic_id
        from feature_synonym
        join synonym using (synonym_id)
        where synonym.type_id = 26803
        and is_current = true
) feature_systematic_id using (feature_id)
;

create temporary view inconsistent_names as
select feature_id, uniquename, systematic_id
     , coalesce(
         (select synonym_id from synonym where name = feature_name.uniquename and type_id = 26800),
         nextval('synonym_synonym_id_seq'::regclass)) as synonym_id
     , exists(select synonym_id from synonym where name = feature_name.uniquename and type_id = 26800) as synonym_already
from feature_name
where type_id = 792
and systematic_id <> uniquename
;

insert into synonym (synonym_id, name, type_id, synonym_sgml)
    (select synonym_id, uniquename, 26800 /*genedb_synonyms:synonym*/, uniquename
        from inconsistent_names
        where not synonym_already)
;

insert into feature_synonym
    (synonym_id, feature_id, is_current, pub_id)
    (select inconsistent_names.synonym_id, inconsistent_names.feature_id, false, pub.pub_id
        from inconsistent_names
           , pub
        where pub.uniquename = 'null'
        and not exists (
            select * from feature_synonym
            where synonym_id = inconsistent_names.synonym_id
              and feature_id = inconsistent_names.feature_id
        ))
;

update feature
set uniquename = inconsistent_names.systematic_id
from inconsistent_names
where inconsistent_names.feature_id = feature.feature_id
and feature.feature_id in (select feature_id from inconsistent_names)
;

/* This should return 0 rows! */
select a.feature_id, b.feature_id, a.uniquename
from feature a, feature b
where a.type_id = b.type_id
and a.organism_id = b.organism_id
and a.uniquename = b.uniquename
and a.feature_id < b.feature_id
;


update feature transcript
set uniquename = regexp_replace(transcript.uniquename, '[^:]*', gene.uniquename)
from feature gene
join feature_relationship transcript_gene on transcript_gene.object_id = gene.feature_id
where transcript_gene.subject_id = transcript.feature_id
and gene.type_id in (
        792 /*gene*/
      , 423 /*pseudogene*/
)
and transcript.type_id in (
        321 /*mRNA*/
      , 339 /*rRNA*/
      , 340 /*tRNA*/
      , 361 /*snRNA*/
      , 604 /*pseudogenic_transcript*/
)
and substring(transcript.uniquename from '[^:]*') <> gene.uniquename
;

update feature exon
set uniquename = regexp_replace(exon.uniquename, '[^:]*', gene.uniquename)
from feature gene
join feature_relationship transcript_gene on transcript_gene.object_id = gene.feature_id
join feature transcript on transcript_gene.subject_id = transcript.feature_id
join feature_relationship exon_transcript on exon_transcript.object_id = transcript.feature_id
where exon_transcript.subject_id = exon.feature_id
and gene.type_id in (
        792 /*gene*/
      , 423 /*pseudogene*/
)
and transcript.type_id in (
        321 /*mRNA*/
      , 339 /*rRNA*/
      , 340 /*tRNA*/
      , 361 /*snRNA*/
      , 604 /*pseudogenic_transcript*/
)
and exon.type_id in (
        234 /*exon*/
      , 595 /*pseudogenic_exon*/
)
and substring(exon.uniquename from '[^:]*') <> gene.uniquename
;

update feature polypeptide
set uniquename = regexp_replace(polypeptide.uniquename, '[^:{]*', gene.uniquename)
from feature gene
join feature_relationship transcript_gene on transcript_gene.object_id = gene.feature_id
join feature transcript on transcript_gene.subject_id = transcript.feature_id
join feature_relationship polypeptide_transcript on polypeptide_transcript.object_id = transcript.feature_id
where polypeptide_transcript.subject_id = polypeptide.feature_id
and gene.type_id in (
        792 /*gene*/
      , 423 /*pseudogene*/
)
and transcript.type_id in (
        321 /*mRNA*/
      , 339 /*rRNA*/
      , 340 /*tRNA*/
      , 361 /*snRNA*/
      , 604 /*pseudogenic_transcript*/
)
and polypeptide.type_id in (
        191 /*polypeptide*/
)
and substring(polypeptide.uniquename from '[^:]*') <> gene.uniquename
and (
    polypeptide.uniquename like '%:pep'
    or polypeptide.uniquename like '%{pep}'
)
;

alter table feature add constraint feature_c1 unique (organism_id, type_id, uniquename);

commit;
