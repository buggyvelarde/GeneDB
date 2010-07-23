begin;

/*
insert into featureprop (feature_id, type_id, value) (
        select feature_id, 26753, 'true'
         from feature where type_id in (
                select cvterm.cvterm_id
                from cvtermpath
                join cvterm on cvtermpath.subject_id = cvterm.cvterm_id
                join cvterm type on cvtermpath.type_id = type.cvterm_id
                where cvtermpath.object_id = 427 /*chromosome*/
                and cvtermpath.type_id = 35 /*is_a*/
        )
)
;
*/

insert into featureprop (feature_id, type_id, value) (
    select distinct s.feature_id, 26753, 'true'
    from feature g
    join featureloc using (feature_id)
    join feature s on featureloc.srcfeature_id = s.feature_id
    join cvterm st on s.type_id = st.cvterm_id
    where g.type_id = 792
    and featureloc.locgroup = 0
    and not exists (
      select 8
      from featureprop
      where featureprop.feature_id = s.feature_id
      and   featureprop.type_id = 26753
    )
);
