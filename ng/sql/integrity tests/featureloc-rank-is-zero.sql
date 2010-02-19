#Checks if the rank in featureloc is always zero.

select feature_id, rank
from featureloc
where not exists (
    select * from featureloc other
    where other.feature_id = featureloc.feature_id
    and other.featureloc_id <> featureloc.featureloc_id)
and rank <> 0;


