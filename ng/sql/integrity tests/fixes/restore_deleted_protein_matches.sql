begin;


/* restore deleted uniprot regions */

insert into feature (uniquename, type_id, organism_id, seqlen, is_obsolete, is_analysis)
select uniquename, type_id, organism_id, seqlen, is_obsolete, is_analysis from audit.feature where organism_id=91 and type='DELETE' 
and uniquename like 'Saureus_TW20%UniProt%' and time > '2009-12-03 00:00:00';

/* add srcfeature_ids to the match features that do not have any */

create temporary table y as
select organism.common_name 
           , feature.feature_id
           , 'Saureus_TW20' || substr(feature.uniquename, 6, length(feature.uniquename)) as name
           , featureloc.featureloc_id as featureloc_id
    from feature
    join organism using (organism_id)
    join featureloc using (feature_id)
    where featureloc.srcfeature_id is null
    and organism.common_name='Saureus_TW20'
    order by organism.common_name
    ;
    

update featureloc set srcfeature_id=feature.feature_id 
from feature, y
where feature.uniquename = y.name
and featureloc.featureloc_id=y.featureloc_id;

drop table y;

commit;


