
#Are the uniquenames truly unique?

select feature1.uniquename
     , feature1.feature_id as feature1_id
     , type1.name          as feature1_type
     , fl1.srcfeature_id as located_on
     , organism1.genus || ' ' || organism1.species as feature1_organism
     , feature2.feature_id as feature2_id
     , type2.name          as feature2_type
     , fl2.srcfeature_id as located_on
     , organism2.genus || ' ' || organism2.species as feature2_organism
from feature feature1
 join organism organism1 on feature1.organism_id = organism1.organism_id
 join cvterm type1 on feature1.type_id = type1.cvterm_id
 join featureloc fl1 on feature1.feature_id=fl1.feature_id
join feature feature2 using (uniquename)
 join organism organism2 on feature2.organism_id = organism2.organism_id
 join cvterm type2 on feature2.type_id = type2.cvterm_id
 join featureloc fl2 on feature2.feature_id=fl2.feature_id
where feature1.feature_id < feature2.feature_id
;
