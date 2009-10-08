#The feature_name should be equal to the primary_name, if there is a primary_name 

select * from feature_name
where primary_name is not null
and name <> primary_name
;
