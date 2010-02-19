#Look for featurelocs with null src features
	
	select organism.common_name 
	       , feature.feature_id
	       , feature.uniquename
	       , featureloc.featureloc_id
	from feature
	join organism using (organism_id)
	join featureloc using (feature_id)
	where featureloc.srcfeature_id is null
	order by organism.common_name
	;
