-- 25.1.2010: It was noticed that some Smansoni MRNAs have weird featurelocs. They have two featurelocs - on the contig and the supercontig - with the
-- same fmin and fmax. More recently Tina has added correct featurelocs on contigs where the fmin/fmax were calculated using the
-- values for the supercontig. It is likely that the old (wrong) featurelocs on the contigs may need to be deleted.


-- All the wrong featurelocs on contigs?

-- Smansoni features with two different featurelocs on the same contig
delete from featureloc where featureloc_id in (
select contigloc.featureloc_id
from featureloc contigloc
join feature using (feature_id)
join featureloc anothercontigloc on contigloc.feature_id = anothercontigloc.feature_id
where feature.organism_id= (select organism_id 
                            from organism 
                            where common_name='Smansoni')
and contigloc.srcfeature_id = anothercontigloc.srcfeature_id                            
and 236 = (select type_id from feature where feature_id=contigloc.srcfeature_id)
and 236 = (select type_id from feature where feature_id=anothercontigloc.srcfeature_id)
and anothercontigloc.fmin < contigloc.fmin)
;
            

-- Smansoni features with two different featurelocs on two contigs that are not adjacent
select feature.feature_id
     , feature.uniquename 
     , contigloc.srcfeature_id as contig1
     , contigloc.fmin as fmin1
     , contigloc.fmax as fmax1
     , anothercontigloc.srcfeature_id as contig2
     , anothercontigloc.fmin as fmin2
     , anothercontigloc.fmax as fmax2
from featureloc contigloc
join feature using (feature_id)
join featureloc anothercontigloc on contigloc.feature_id = anothercontigloc.feature_id
where feature.organism_id= (select organism_id 
                            from organism 
                            where common_name='Smansoni')
and 236 = (select type_id from feature where feature_id=contigloc.srcfeature_id)
and 236 = (select type_id from feature where feature_id=anothercontigloc.srcfeature_id)
and contigloc.srcfeature_id < anothercontigloc.srcfeature_id
and (contigloc.is_fmax_partial = 'f' and contigloc.is_fmin_partial = 'f')
and (anothercontigloc.is_fmin_partial = 'f' and anothercontigloc.is_fmax_partial = 'f')
;


