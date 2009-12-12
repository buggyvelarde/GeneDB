delete from feature
using organism
where feature.organism_id = organism.organism_id
and (
    (
        organism.genus = 'Staphylococcus'
        and organism.species = in ('aureus (EMRSA15)', 'aureus (LGA251)')
    ) or (
        organism.genus = 'Plasmodium'
        and organism.species in ('berghei', 'berghei (3x coverage)')
    ) or (
        organism.genus = 'Streptococcus'
        and organism.species in ('pneumoniae TIGR4', 'pneumoniae D39', 'pneumoniae OXC141')
    ) or (
    	organism.genus = ''
)
;
