delete from feature
using organism
where feature.organism_id = organism.organism_id
and (
    (
        organism.genus = 'Streptococcus'
        and organism.species = 'pneumoniae OXC141'
    ) or (
        organism.genus = 'Staphylococcus'
        and (
            organism.species = 'aureus (EMRSA15)'
            or organism.species = 'aureus (TW20)'
        )
    ) or (
        organism.genus = 'Plasmodium'
        and organism.species in ('berghei', 'berghei (3x coverage)')
    ) or (
        organism.genus = 'Streptococcus'
        and organism.species in ('pneumoniae TIGR4', 'pneumoniae D39')
    ) or (
        organism.genus = 'Eimeria'
        and organism.species = 'tenella'
    )
)
;
