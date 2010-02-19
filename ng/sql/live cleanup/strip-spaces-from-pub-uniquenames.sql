begin;

create temporary table pub_clash on commit drop as
select w.pub_id as with_spaces_pub_id
     , wo.pub_id as without_spaces_pub_id
     , w.uniquename as with_spaces_uniquename
     , wo.uniquename as without_spaces_uniquename
from pub w, pub wo
where w.uniquename ~ E'\\s' and wo.uniquename = regexp_replace(w.uniquename, E'\\s', '', 'g')
;

delete from feature_pub
using pub_clash
where pub_clash.with_spaces_pub_id = feature_pub.pub_id
and exists (
	select 8
	from feature_pub inner_fp
	where inner_fp.feature_id = feature_pub.feature_id
	and inner_fp.pub_id = pub_clash.without_spaces_pub_id
);

update feature_pub
set pub_id = (
	select without_spaces_pub_id from pub_clash
	where feature_pub.pub_id = pub_clash.with_spaces_pub_id
) where pub_id in (
	select with_spaces_pub_id from pub_clash
);

delete from pub where pub_id in (select with_spaces_pub_id from pub_clash);

commit;