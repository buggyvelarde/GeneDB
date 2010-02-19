/*
 * There was a loading problem that, possibly in conjunction with
 * a misconceived attempt to fix the problem, resulted in some
 * spurious products. These could be distinguished by the presence
 * of an 'attribution' property.
 *
 * This code removes any product with an 'attribution' property,
 * therefore.
 */

delete from feature_cvterm
using feature
  ,   feature_cvtermprop
  ,   cvterm join cv using (cv_id)
where feature.feature_id = feature_cvterm.feature_id
and   feature_cvtermprop.feature_cvterm_id = feature_cvterm.feature_cvterm_id
and   cvterm.cvterm_id = feature_cvterm.cvterm_id
and feature_cvtermprop.type_id = (
    select cvterm.cvterm_id
    from cvterm
    join cv using (cv_id)
    where cv.name = 'genedb_misc'
    and cvterm.name = 'attribution')
and cv.name = 'genedb_products'
;
