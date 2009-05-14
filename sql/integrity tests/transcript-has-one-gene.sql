select feature_id, uniquename
from feature transcript
where transcript.type_id in (
      321 /*mRNA*/
    , 339 /*rRNA*/
    , 340 /*tRNA*/
    , 361 /*snRNA*/
    , 362 /*snoRNA*/
    , 743 /*ncRNA*/
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

select feature_id, uniquename
from feature transcript
where transcript.type_id = 604 /*pseudogenic_transcript*/
and 1 <> (
    select count(*)
    from feature_relationship transcript_gene
    join feature gene on transcript_gene.object_id = gene.feature_id
    where transcript_gene.subject_id = transcript.feature_id
    and   gene.type_id = 423 /*pseudogene*/
)
;
