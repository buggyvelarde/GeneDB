/* Make Bodo saltan features toplevel. This isn't a fix for any of the integrity tests, but thought 
 * I'd put it in here incase it was useful in the future (nds)*/

begin;

insert into featureprop (feature_id, type_id, value)
select feature_id, 26753, 'true' 
from feature
where type_id=236
and organism_id=210
and not exists (select * 
                from featureprop where type_id = 26753 
                and value='true'
                and feature_id=feature.feature_id);



commit;




