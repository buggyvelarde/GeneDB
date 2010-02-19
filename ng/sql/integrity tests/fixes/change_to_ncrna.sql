begin;

create temporary table pf as
select distinct substr(feature.uniquename,0,length(feature.uniquename)-10) || ':ncRNA' as uniquename, 
                feature.feature_id as feature_id, 
                feature.type_id as type_id
from feature
join feature_relationship on feature_relationship.object_id=feature.feature_id
and feature_relationship.subject_id in 
(select feature_id from audit.feature where type='INSERT' and uniquename like '%exon' and username like 'nds%' and time > '2009-11-24 12:00:00');

update feature set type_id=743 where feature_id in (select feature_id from pf);

update feature set uniquename=pf.uniquename from pf where feature.feature_id=pf.feature_id;

drop table pf;

commit;