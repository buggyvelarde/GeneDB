#Checks that all genes have atleast one transcript. Some of the genes returned by this test
#are non-coding-genes which are not expected to have a transcript. Change query or label
#these genes appropriately.

select organism.common_name, gene.feature_id, gene.uniquename
from feature gene
join organism on gene.organism_id=organism.organism_id
where gene.type_id = 792 
and not exists (
    select *
    from feature_relationship transcript_gene
    join feature transcript on transcript_gene.subject_id = transcript.feature_id
    where transcript_gene.object_id = gene.feature_id
    and   transcript.type_id in (
                                  select cvterm.cvterm_id
                                  from cvterm join cv on cvterm.cv_id = cv.cv_id
                                  where cv.name = 'sequence'
                                  and cvterm.name in ('mRNA', 'rRNA', 'tRNA', 'snRNA', 
                                                      'transcript', 'ncRNA', 'snoRNA', 'scRNA' )
    )
    
)
order by organism.common_name
;
