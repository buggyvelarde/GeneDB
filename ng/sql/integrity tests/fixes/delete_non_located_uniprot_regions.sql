/* Delete the uniprot features that are not located on anything (i.e., no featurelocs) 
 * They are not particularly useful anyway as they are not visible to anyone */

begin;

create temporary table uniprot as
select organism.common_name
     , feature.feature_id as feature_id
     , feature.uniquename
     , cvterm.name as type
from feature
join organism using (organism_id)
join cvterm on feature.type_id=cvterm.cvterm_id
and feature.type_id not in (
                             select cvterm.cvterm_id
                             from cvterm join cv on cvterm.cv_id = cv.cv_id
                             where cv.name = 'sequence'
                             and cvterm.name in ('BAC_end', 'EST'))
and not exists (
    select *
    from featureloc
    where featureloc.feature_id = feature.feature_id
)
and not exists (
    select *
    from featureprop
    join cvterm on featureprop.type_id = cvterm.cvterm_id
    join cv using (cv_id)
    where featureprop.feature_id = feature.feature_id
    and cv.name = 'genedb_misc'
    and cvterm.name = 'top_level_seq'
)
and feature.uniquename ilike '%uniprot%'
order by organism.common_name
;

delete from feature where feature_id in (select feature_id from uniprot);

delete from featureprop where feature_id in (select feature_id from uniprot);

select refs('feature', )

drop table uniprot;

commit;