#Checks if systematic_id and temporary_systematic_id are mutually exclusive 

select * from feature_name
where systematic_id is not null
and temporary_systematic_id is not null
;
