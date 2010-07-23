begin;

create temporary table domainloc_fix on commit drop as
select audit_id, featureloc_id, old_fmin, old_fmax
from audit.featureloc_update
join feature using (feature_id)
join cvterm on feature.type_id = cvterm.cvterm_id
join feature srcfeature on featureloc_update.srcfeature_id = srcfeature.feature_id
where old_fmin < fmin and old_fmax < fmax
and fmin - old_fmin = fmax - old_fmax
and srcfeature.type_id = 191
;

update featureloc
set fmin = domainloc_fix.old_fmin, fmax = domainloc_fix.old_fmax
from domainloc_fix
where featureloc.featureloc_id = domainloc_fix.featureloc_id
;

commit;