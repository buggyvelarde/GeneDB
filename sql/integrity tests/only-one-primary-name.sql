#No feature has more than one primary name 

select a.*, b.*
from feature_name a, feature_name b
where a.feature_id = b.feature_id
and a.primary_name <> b.primary_name
;

