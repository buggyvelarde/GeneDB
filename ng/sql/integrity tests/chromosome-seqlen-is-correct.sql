#Checks if the length of the feature (e.g., chromosome) as dictated by its
#seqlen field is the same as the length of its residues.



select organism.common_name, feature_id, seqlen, length(residues) as correct_seqlen
from feature
join organism using (organism_id)
where type_id in (
    select cvterm.cvterm_id
    from cvterm join cv on cvterm.cv_id = cv.cv_id
    where cv.name = 'sequence'
    and cvterm.name in ('chromosome', 
                        'contig', 
                        'mitochondrial_chromosome', 
                        'apicoplast_sequence', 
                        'linear_double_stranded_DNA_chromosome')
     
) 
and seqlen <> length(residues)
;
