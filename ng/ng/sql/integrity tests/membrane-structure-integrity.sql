#Membrane structure integrity

select membrane_structure_region.uniquename
     , fr.*
from feature membrane_structure_region
left join feature_relationship fr on membrane_structure_region.feature_id = fr.subject_id
left join feature membrane_structure on membrane_structure.feature_id = fr.object_id
where membrane_structure_region.type_id in (
    select cvterm.cvterm_id
    from cvterm join cv using (cv_id)
    where cv.name = 'sequence'
    and cvterm.name in (
        'cytoplasm_location',
        'non_cytoplasm_location',
        'transmembrane'
    )
)
and not exists (
    select *
    from feature_relationship fr
    join feature membrane_structure on membrane_structure.feature_id = fr.object_id
    where membrane_structure_region.feature_id = fr.subject_id
    and fr.type_id in (
        select cvterm.cvterm_id
        from cvterm join cv using (cv_id)
        where cv.name = 'relationship'
        and cvterm.name = 'part_of'
    )
    and membrane_structure.type_id in (
        select cvterm.cvterm_id
        from cvterm join cv using (cv_id)
        where cv.name = 'sequence'
        and cvterm.name = 'membrane_structure'
    )
);
