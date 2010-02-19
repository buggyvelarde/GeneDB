/*
 In situ patch for some Schistosoma mansoni data errors
 caused by bugs in the loading code. It's quicker to fix
 them in place than to reload the data with the corrected
 code.
*/

create temporary table new_featurelocs as
select geneloc.featureloc_id
     , max(transcriptloc.fmax) as new_fmax
from feature gene
join featureloc geneloc
    on gene.feature_id = geneloc.feature_id
join feature_relationship transcript_gene
    on transcript_gene.object_id = gene.feature_id
join feature transcript
    on transcript_gene.subject_id = transcript.feature_id
join featureloc transcriptloc
    on transcriptloc.feature_id = transcript.feature_id
where gene.type_id = 792
and transcript.type_id in (
      321 /*mRNA*/
    , 339 /*rRNA*/
    , 340 /*tRNA*/
    , 361 /*snRNA*/
)
and gene.organism_id = 13
and transcript.organism_id = 13
and transcriptloc.rank = geneloc.rank
group by geneloc.featureloc_id, geneloc.fmax
having geneloc.fmax <> max(transcriptloc.fmax)
;

select feature.uniquename
     , featureloc.fmax
     , new_featurelocs.new_fmax
from new_featurelocs
join featureloc using (featureloc_id)
join feature using (feature_id)
;


update featureloc
set fmax = new_featurelocs.new_fmax
from new_featurelocs
where featureloc.featureloc_id = new_featurelocs.featureloc_id
;


insert into featureloc (
    feature_id, srcfeature_id,
    locgroup, rank, fmin, fmax,
    strand, phase
) (
    select polypeptide.feature_id
         , transcriptloc.srcfeature_id
         , transcriptloc.locgroup
         , transcriptloc.rank
         , transcriptloc.fmin
         , transcriptloc.fmax
         , transcriptloc.strand
         , transcriptloc.phase
    from feature polypeptide
    join feature_relationship polypeptide_transcript
        on polypeptide_transcript.subject_id = polypeptide.feature_id
    join feature transcript
        on polypeptide_transcript.object_id = transcript.feature_id
    join featureloc transcriptloc
        on transcriptloc.feature_id = transcript.feature_id
    where polypeptide.type_id = 191
    and transcript.type_id in (
          321 /*mRNA*/
        , 339 /*rRNA*/
        , 340 /*tRNA*/
        , 361 /*snRNA*/
    )
    /*
    and not exists (
        select 8
        from featureloc
        where featureloc.feature_id = polypeptide.feature_id
    )
    */
    and transcript.organism_id = 13
    and polypeptide.organism_id = 13
);

update feature
set uniquename = substr(uniquename, 1, length(uniquename)-9) || ':pep'
where uniquename like '%:mRNA:pep'
;

/*
 * This is substantially faster than the more obvious
 * 'not in' formulation of the query, using Postgres 8.3.
 */
create temporary table new_synonyms as
select nextval('synonym_synonym_id_seq'::regclass) as synonym_id
     , feature_id
     , uniquename
from feature
where feature_id in (
  select feature_id from feature
      where organism_id = 13
      and type_id = 321
  except
  select feature_id from feature_synonym join synonym using (synonym_id)
      where synonym.type_id in (26803, 26804)
      and feature_synonym.is_current
      and not feature_synonym.is_internal
);

insert into synonym (synonym_id, type_id, name, synonym_sgml)
    (select synonym_id, 26804, uniquename, uniquename from new_synonyms);

insert into feature_synonym (feature_id, synonym_id, pub_id)
    (select feature_id, synonym_id, 1 from new_synonyms);
