#This checks if all features that are 
# - not top level features
# - have a sequence 
# - not a BAC_end
# - not an EST
# - not a nucleotide_match or protein_match
# - not an ORTHOMCL match region
# - not a unassembled PKnowlesi contig (review this decision later)
# have a sourcefeature.


select organism.common_name
     , feature.feature_id
     , feature.uniquename
     , cvterm.name as type
from feature
join organism using (organism_id)
join cvterm on feature.type_id=cvterm.cvterm_id
and feature.type_id not in (
                             select cvterm.cvterm_id
                             from cvterm join cv on cvterm.cv_id = cv.cv_id
                             where cv.name = 'sequence'
                             and cvterm.name in ('BAC_end', 'EST', 'protein_match', 'nucleotide_match'))                          
and not exists (
    select *
    from featureloc
    where featureloc.feature_id = feature.feature_id
)
and not exists (
    select *
    from featureprop
    join cvterm on featureprop.type_id = cvterm.cvterm_id
    join cv using (cv_id)
    where featureprop.feature_id = feature.feature_id
    and cv.name = 'genedb_misc'
    and cvterm.name = 'top_level_seq'
)
and not (organism.common_name='Pknowlesi' and feature.type_id=236)
and feature.uniquename not ilike '%uniprot%'
and feature.uniquename not ilike '%ORTHO%'
order by organism.common_name
;