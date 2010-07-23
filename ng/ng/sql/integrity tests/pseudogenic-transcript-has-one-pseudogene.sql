
#Checks that every transcript labelled as being a pseudogenic transcript has one psuedogene

select organism.common_name, feature_id, uniquename
from feature transcript
join organism on transcript.organism_id = organism.organism_id
where transcript.type_id = (
                            select cvterm.cvterm_id
                            from cvterm join cv on cvterm.cv_id = cv.cv_id
                            where cv.name = 'sequence'
                            and cvterm.name='pseudogenic_transcript' )
and not exists (
    select *
    from feature_relationship transcript_gene
    join feature gene on transcript_gene.object_id = gene.feature_id
    where transcript_gene.subject_id = transcript.feature_id
    and   gene.type_id = (
                            select cvterm.cvterm_id
                            from cvterm join cv on cvterm.cv_id = cv.cv_id
                            where cv.name = 'sequence'
                            and cvterm.name='pseudogene')
)
and organism.common_name != 'Lmexicana'
order by organism.common_name
;
