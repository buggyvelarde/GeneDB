# This test checks that all exon names are null (i.e., feature.name field)

select organism.common_name,
       feature.feature_id,
       feature.uniquename,
       feature.name
from feature 
join cvterm on feature.type_id=cvterm.cvterm_id 
join organism using (organism_id)
where cvterm.name='exon' 
and feature.name is not null
order by organism.common_name;
