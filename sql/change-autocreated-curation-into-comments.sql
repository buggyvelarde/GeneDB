/* Assuming cvterm_ids curation=26765 and comment=1672 */

begin;

create temporary table featureprop_changes as
    select featureprop_id
         , feature_id
         , rank as old_rank
         , ( select coalesce(1+max(rank),0)
             from featureprop x
             where x.feature_id=featureprop.feature_id
             and x.type_id=1672
             ) as new_rank
    from featureprop
    where type_id = 26765 and value like '(Autocreated)%'
;

update featureprop
set type_id = 1672
  , rank = featureprop_changes.new_rank
from featureprop_changes
where featureprop.featureprop_id = featureprop_changes.featureprop_id
;

update featureprop
set rank = rank - 1
from featureprop_changes
where featureprop.feature_id = featureprop_changes.feature_id
and   featureprop.type_id = 26765
and   featureprop.rank > featureprop_changes.old_rank
;

commit;
