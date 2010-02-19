update organismprop
set value = 'P. ' || organism.species
from cvterm, organism
where organismprop.type_id = cvterm.cvterm_id
  and organismprop.organism_id = organism.organism_id
  and organism.genus = 'Plasmodium'
  and cvterm.name = 'htmlShortName'
;

update organismprop
set value = 'Plasmodium ' || organism.species
from cvterm, organism
where organismprop.type_id = cvterm.cvterm_id
  and organismprop.organism_id = organism.organism_id
  and organism.genus = 'Plasmodium'
  and cvterm.name = 'htmlFullName'
;



select organismprop.organismprop_id
     , organism.common_name as organism
     , cvterm.name
     , organismprop.value
from organismprop
join cvterm on organismprop.type_id = cvterm.cvterm_id
join organism using (organism_id)
where organism.genus = 'Plasmodium'
;
