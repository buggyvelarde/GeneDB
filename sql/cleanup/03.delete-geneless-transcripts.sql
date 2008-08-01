delete
from feature transcript
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
