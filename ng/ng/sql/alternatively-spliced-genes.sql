select organism.common_name as organism_name
     , gene.uniquename
     , geneloc.fmin
     , geneloc.fmax
     , geneloc.strand
from feature gene
join featureloc geneloc using (feature_id)
join organism using (organism_id)
join feature_relationship transcript1_gene on transcript1_gene.object_id = gene.feature_id
join feature transcript1 on transcript1_gene.subject_id = transcript1.feature_id
join feature_relationship transcript2_gene on transcript2_gene.object_id = gene.feature_id
join feature transcript2 on transcript2_gene.subject_id = transcript2.feature_id
where gene.type_id = 792 /*gene*/
and transcript1.type_id = 321 /*mRNA*/
and transcript2.type_id = 321 /*mRNA*/
and transcript1.feature_id < transcript2.feature_id
;
