update feature
    set type_id = 427 /*chromosome*/
from organism
where feature.organism_id = organism.organism_id
and organism.common_name = 'Saureus_MRSA252'
and feature.type_id = 235 /*supercontig*/
;

/*
malaria_workshop=# select feature_id, uniquename from feature join organism using (organism_id) where organism.common_name = 'Saureus_MRSA252' and feature.type_id = 235;
 feature_id | uniquename 
------------+------------
    2901137 | BX571856
    2912404 | EM0000001
    2922869 | BX571857
(3 rows)
*/

update feature
    set organism_id = 
