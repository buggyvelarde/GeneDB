#Checks if the length of the feature (e.g., chromosome) as dictated by its
#seqlen field is the same as the length of its residues.

select feature_id, seqlen, length(residues) as correct_seqlen
from feature
where type_id in (427, 236, 907, 831, 1045) /* test */
and seqlen <> length(residues)
;
