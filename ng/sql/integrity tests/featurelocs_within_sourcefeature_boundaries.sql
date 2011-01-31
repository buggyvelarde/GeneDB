#The fmin and fmax values of the featurelocs should lie within the boundaries of the sourcefeature. This is often the case but when source features
#are shortened, some of its features silently fall off the end. In many cases, e.g. GPI-anchors, the script needs to be rerun to locate new features
#appropriately.

-- This test is in two parts for sourcefeatures located on toplevel features and those that are toplevel themselves
select distinct displacedfeature.feature_id,
       displacedfeature.uniquename,
       cvterm.name as type,
       organism.common_name
from feature displacedfeature
join featureloc displacedfloc using (feature_id)
join feature sourcefeature on sourcefeature.feature_id = displacedfloc.srcfeature_id
join featureloc sourcefloc on sourcefeature.feature_id=sourcefloc.feature_id -- sourcefeature's featureloc
join organism on organism.organism_id = displacedfeature.organism_id
join cvterm on cvterm.cvterm_id=displacedfeature.type_id
where -- sourcefeature is toplevel
(displacedfloc.fmax > sourcefeature.seqlen 
and exists (select * 
            from featureprop 
            where feature_id=sourcefeature.feature_id 
            and type_id = (select cvterm_id
                           from cvterm
                           where name='top_level_seq')))
or --sourcefeature's sourcefeature is toplevel
(displacedfloc.fmax > (sourcefloc.fmax-sourcefloc.fmin)
and exists (select * 
            from featureprop 
            where feature_id=sourcefloc.srcfeature_id 
            and type_id = (select cvterm_id
                           from cvterm
                           where name='top_level_seq')))
;




