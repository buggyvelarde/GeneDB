begin;

create temporary table chr_seqlen_correction as
select feature_id, length(residues) as corrected_seqlen
from feature
where type_id in (427, 236, 907, 831, 1045)
and seqlen <> length(residues)
;

update feature
set seqlen = (
    select corrected_seqlen from chr_seqlen_correction
    where chr_seqlen_correction.feature_id = feature.feature_id
)
where feature_id in (
    select feature_id from chr_seqlen_correction
)
;

commit;
