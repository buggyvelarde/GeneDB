/* 2008-07-07 */

delete from feature
where type_id = 87 /*region*/
and uniquename like 'UniProt:%'
;

update feature
set type_id = 788 /*remark*/
where type_id = 87 /*region*/
and uniquename in (
      'UNSURE1'
    , 'misc_feature_1' 
)
;

update feature
set type_id = 500 /*sequence_difference*/
where type_id = 87 /*region*/
and uniquename = 'variation1'
;

update feature
set type_id = 1481
where type_id = 87 /*region*/
and uniquename like 'SECIS%'
;

/* need to relocate onto transcript */
