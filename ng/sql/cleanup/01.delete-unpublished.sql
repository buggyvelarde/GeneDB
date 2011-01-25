delete from organism where 
    (
        organism.genus = 'Staphylococcus'
        and organism.species in ('aureus (EMRSA15)', 'aureus (LGA251)', 'aureus (TW20)')
    )
    or (
        organism.genus = 'Plasmodium'
        and organism.species in ('berghei (3x coverage)', 'reichenowi')
    )
    or (
        organism.genus = 'Streptococcus'
        and organism.species in ('pneumoniae TIGR4', 'pneumoniae D39', 'pneumoniae OXC141', 'pneumoniae ATCC 700669')
    ),
    or (
        organism.genus = 'Leishmania'
        and organism.species in ('donovani')
    )
;
