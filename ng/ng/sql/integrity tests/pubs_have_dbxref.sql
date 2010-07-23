#This checks if all rows in the pub tables has a corresponding dbxref
#Pub with pub_id=1 is ignored as it appears to be a special case used in instances when there is no publication to
#comply with the chado schema
 

select * from pub 
where not exists (
                  select * from pub_dbxref 
                  where pub.pub_id=pub_dbxref.pub_id)
and pub_id!=1; 
