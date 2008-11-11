delete from feature
using organism
where feature.organism_id = organism.organism_id
and organism.genus = 'Streptococcus'
;
