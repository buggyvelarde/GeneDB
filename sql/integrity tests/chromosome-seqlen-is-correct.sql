select feature_id, seqlen, length(residues) as correct_seqlen
from feature
where type_id in (427, 236, 907, 831, 1045)
and seqlen <> length(residues)
;
