#Checks if every gene or pseudogene has a systematic or temporary systematic ID 

select * from feature_name
where type_id in (
    792,
    423
)
and systematic_id is null
and temporary_systematic_id is null
;

