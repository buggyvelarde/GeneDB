begin;

create temporary table tb927_uniquenames as
select  feature1.feature_id as feature1_id
     ,  'Tb927:'|| substr(feature1.uniquename, 9) as new_uniquename1
     ,  feature2.feature_id as feature2_id
     ,  organism2.common_name ||':'|| feature2.uniquename as new_uniquename2
from feature feature1
 join organism organism1 on feature1.organism_id = organism1.organism_id
 join cvterm type1 on feature1.type_id = type1.cvterm_id
 join feature feature2 using (uniquename)
 join organism organism2 on feature2.organism_id = organism2.organism_id
 join cvterm type2 on feature2.type_id = type2.cvterm_id
where feature1.feature_id < feature2.feature_id
and organism1.organism_id!=organism2.organism_id
and organism1.organism_id=19;
;

update feature
set uniquename=new_uniquename1
from tb927_uniquenames
where feature.feature_id=tb927_uniquenames.feature1_id
;

commit;