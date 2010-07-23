begin;

create temporary table new_genes as
select organism.organism_id
     , transcript.feature_id as transcript_id
     , transcript.uniquename as transcript
     , transcriptloc.srcfeature_id
     , transcriptloc.fmin
     , transcriptloc.fmax
     , transcriptloc.strand
     , nextval('feature_feature_id_seq'::regclass) as new_gene_id
     , substring(transcript.uniquename from 1 for position(':' in transcript.uniquename)-1) as new_gene_name
from feature transcript
join organism using (organism_id)
join featureloc transcriptloc using (feature_id)
where transcript.type_id in (
      321 /*mRNA*/
    , 339 /*rRNA*/
    , 340 /*tRNA*/
    , 361 /*snRNA*/
)
and not exists (
    select 8
    from feature_relationship transcript_gene
    join feature gene on transcript_gene.object_id = gene.feature_id
    where transcript_gene.subject_id = transcript.feature_id
    and   gene.type_id = 792 /*gene*/
)
;

insert into feature (feature_id, organism_id, uniquename, type_id)
    (select new_gene_id, organism_id, new_gene_name, 792 from new_genes)
;
insert into featureloc (feature_id, srcfeature_id, fmin, fmax, strand)
    (select new_gene_id, srcfeature_id, fmin, fmax, strand from new_genes)
;
insert into feature_relationship (subject_id, object_id, type_id)
    (select transcript_id, new_gene_id, 42 from new_genes)
;

commit;
