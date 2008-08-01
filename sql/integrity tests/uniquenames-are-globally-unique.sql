select feature1.uniquename
     , feature1.feature_id as feature1_id
     , feature1.type_id    as feature1_type
     , feature2.feature_id as feature2_id
     , feature2.type_id    as feature2_type
from feature feature1
join feature feature2 using (uniquename)
where feature1.feature_id < feature2.feature_id
;
