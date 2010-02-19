/* One-off ad hoc script to remove the bogus feature_property cvterms 'Note', 'Gap' and 'score'
 * These terms are not in the OBO file, and we don't know where they came from.
 */

update featureprop set type_id = 1672 where featureprop_id = 461623;
update featureprop set type_id = 1672, rank = 1 where featureprop_id = 465016;
delete from cvterm where cvterm_id in (1712, 1713, 1714);
