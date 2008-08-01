/* List the popularity of different colours for each feature type */
select feature_type.name as feature
     , coalesce(colour.value, '(blank)') as colour
     , count(*)
from feature
join featureprop colour using (feature_id)
join cvterm feature_type on feature.type_id = feature_type.cvterm_id
where colour.type_id = 26768 /*colour*/
group by feature_type.name, colour.value
;

/* Uncoloured polypeptides in P. falciparum */
select feature.uniquename
from feature
where type_id = 191 /*polypeptide*/
and organism_id = 27 /*P. falciparum*/
and not exists (
    select 8
    from featureprop colour
    where colour.feature_id = feature.feature_id
    and colour.type_id = 26768 /*colour*/
    and colour.value is not null
)
;


/* List features with colour 8. Uli wanted this once. */
select feature.uniquename
from feature
join featureprop colour using (feature_id)
where feature.type_id = 191
and organism_id = 27
and colour.type_id = 26768 /*colour*/
and colour.value = '8'
;
