
#Checks if all transcripts have one gene. In some cases (e.g. some transcripts in PFalciparum)
#it is considered acceptable for transcripts to have no associated genes.

select organism.common_name, feature_id, uniquename
from feature transcript
join organism on transcript.organism_id=organism.organism_id
where transcript.type_id in (
    select cvterm.cvterm_id
    from cvterm join cv on cvterm.cv_id = cv.cv_id
    where cv.name = 'sequence'
    and cvterm.name in ('mRNA', 
                        'rRNA', 
                        'tRNA',
                        'snRNA',
                        'snoRNA',
                        'ncRNA',
                        'transcript')
 
)
and not exists (
    select *
    from feature_relationship transcript_gene
    join feature gene on transcript_gene.object_id = gene.feature_id
    where transcript_gene.subject_id = transcript.feature_id
    and   gene.type_id = (
                          select cvterm.cvterm_id
                          from cvterm join cv on cvterm.cv_id = cv.cv_id
                          where cv.name = 'sequence'
                          and cvterm.name='gene')

)
order by organism.common_name
;
