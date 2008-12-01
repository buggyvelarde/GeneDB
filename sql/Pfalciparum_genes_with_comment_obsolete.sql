select gene.uniquename, featureprop.value
from feature gene
join featureprop using (feature_id)
where organism_id = 27 /* Pfalciparum */
and featureprop.type_id = 1672 /* comment */
and featureprop.value ilike '%obsolete%'
;
