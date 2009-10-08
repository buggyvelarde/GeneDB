 
#No feature has more than one (temporary) systematic ID 

select a.*, b.*
from feature_name a, feature_name b
where a.feature_id = b.feature_id
and coalesce(a.temporary_systematic_id, a.systematic_id, 'NONE') <> coalesce(b.temporary_systematic_id, b.systematic_id, 'NONE')
;

