/*
select feature_id
     , cc_type.name
from feature_cvterm cc join cvterm cc_type using (cvterm_id)
join feature_cvterm pr using (feature_id)
join cvterm pr_type on pr.cvterm_id = pr_type.cvterm_id
where cc_type.cv_id = 20
and   pr_type.cv_id = 25
and   cc_type.name = pr_type.name
;
*/

begin;

delete from feature_cvterm cc
using cvterm cc_type
    , feature_cvterm pr
      join cvterm pr_type using (cvterm_id)
where cc_type.cvterm_id = cc.cvterm_id
and   cc.feature_id = pr.feature_id
and   cc_type.cv_id = 20 /*CC_genedb_controlledcuration*/
and   pr_type.cv_id = 25 /*genedb_products*/
and   cc_type.name = pr_type.name
;

delete from cvterm
where cv_id in (
      20 /*CC_genedb_controlledcuration*/
    , 25 /*genedb_products*/
)
and not exists (
    select 8 from feature_cvterm where cvterm_id = cvterm.cvterm_id
)
;

commit;
