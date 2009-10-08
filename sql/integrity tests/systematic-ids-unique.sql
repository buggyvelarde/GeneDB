 
#Checks if all the systematic IDs are unique 

select a.*, b.*
from feature_name a, feature_name b
where a.feature_id < b.feature_id
and a.systematic_id = b.systematic_id
;

