These data files are used for testing. They are based on real genetic data, but have
been tweaked so as to test certain corner cases and so forth, and large numbers of
features have been removed. In other words, they are useful only for testing the loader,
and not for anything else.

The files here at the time of writing are:

PB_PH0001.embl:
    A short contig from Plasmodium berghei, with one CDS. Used by EmblLoaderBergheiTest.

Smp_scaff000604.embl:
    An edited subset of the data from Schistosoma mansoni supercontig 604.
    Used by EmblLoaderMansoniTest and EmblFileTest.

synthetic.embl:
    Hand-written test data in PSU-EMBL format. Used by EmblLoaderSyntheticTest.

test1.fasta:
    Very short hand-constructed FASTA file, used by FastaFileTest and FastaLoaderTest.

{EMRSA15,MRSA252,MSSA476}_subset.embl:
    Subsets of the Staphylococcus aureus data, restricted to just the genes that
    are mentioned in the files Saureus_subset_{gene,transcript}names.ortho.
    Used by OrthologueLoaderImplicitClusterTest and OrthologueLoaderUnclusteredTest.

{EMRSA15,MRSA252,MSSA476}_clusters.embl:
    Subsets of the Staphylococcus aureus data, restricted to just the genes that
    are mentioned in the file Saureus_clusters.ortho. Used by OrthologueLoaderClusteredTest.

Saureus_subset_transcriptnames.ortho:
    A random subset of the orthology data for Staphylococcus aureus as predicted by
    reciprocal best FASTA hit, with transcript unique names in the file. Used by
    OrthologueLoaderUnclusteredTest and OrthologueLoaderImplicitClusterTest.

Saureus_subset_genenames.ortho:
    Another random subset of the reciprocal FASTA matches, disjoint from the above,
    with gene names rather than transcript names in the file. Also used by
    OrthologueLoaderUnclusteredTest and OrthologueLoaderImplicitClusterTest.

Saureus_clusters.ortho:
    A random subset of the orthologue clusters predicted by OrthoMCL for Staphylococcus
    aureus, specified by polypeptide name.

Pfalciparum_domains_subset.interpro
    The first 30 lines of the InterProScan raw output for Plasmodium falciparum 3D7,
    as generated on 2009-01-07.

skeleton.{backup,data,properties,script}:
    A copy of the skeleton database from genedb-db/test-data. This database is populated
    by the tests

-- rh11, 2008-11-27.
