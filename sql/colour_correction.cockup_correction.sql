select featureprop_id
from featureprop
where exists (
        select polypeptide_colour
        from exon_colour_corrections
        where exon_feature_id = featureprop.feature_id)
and   type_id <> 26768 /*colour*/
;


 featureprop_id 
----------------
         458563
         459425
         460185
         460342
         460757
         458564
         459024
         459025
         459026
         459034
         460343
         460816
         461565
         461566
         461625
         462095
         462096
         462097
         462098
         462765
(20 rows)

select 'update featureprop set value = '''||value||''' where featureprop_id='||featureprop_id||';'
from featureprop
where featureprop_id in (
        458563, 459425, 460185, 460342, 460757, 458564, 459024,
        459025, 459026, 459034, 460343, 460816, 461565, 461566,
        461625, 462095, 462096, 462097, 462098, 462765)
;

update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_13.seq.00250.out' where featureprop_id=458563;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_13.seq.00250.out' where featureprop_id=458564;
update featureprop set value = '/nfs/team81/barrell/DATABASES/apicomplexans:fasta/Pf3D7_06.seq.02861.out' where featureprop_id=459024;
update featureprop set value = '/nfs/team81/barrell/DATABASES/apicomplexans:fasta/Pf3D7_06.seq.02861.out' where featureprop_id=459025;
update featureprop set value = '/nfs/team81/barrell/DATABASES/apicomplexans:fasta/Pf3D7_06.seq.02861.out' where featureprop_id=459026;
update featureprop set value = '/nfs/team81/barrell/DATABASES/apicomplexans:fasta/Pf3D7_06.seq.02861.out' where featureprop_id=459034;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_14.seq.00385.out' where featureprop_id=459425;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_11.seq.00465.out' where featureprop_id=460185;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_11.seq.00488.out' where featureprop_id=460342;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_11.seq.00488.out' where featureprop_id=460343;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_08.seq.00558.out' where featureprop_id=460757;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_08.seq.00568.out' where featureprop_id=460816;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_01.seq.00668.out' where featureprop_id=461565;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_01.seq.00668.out' where featureprop_id=461566;
update featureprop set value = '%uniprot_eukaryota:fasta/Pf3D7_03.seq.03038.out' where featureprop_id=461625;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_13.seq.00712.out' where featureprop_id=462095;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_13.seq.00712.out' where featureprop_id=462096;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_13.seq.00712.out' where featureprop_id=462097;
update featureprop set value = '/nfs/pathdata/Plasmodium/falciparum/3D7/workshop/DATABASES/apicomplexans:fasta/Pf3D7_13.seq.00712.out' where featureprop_id=462098;
update featureprop set value = '%uniprot_eukaryota:fasta/Pf3D7_06.seq.00748.out' where featureprop_id=462765;
