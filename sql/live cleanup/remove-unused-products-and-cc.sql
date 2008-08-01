delete from cvterm
where cv_id in (
      20 /*CC_genedb_controlledcuration*/
    , 25 /*genedb_products*/
)
and not exists (
    select 8 from feature_cvterm where cvterm_id = cvterm.cvterm_id
)
;
