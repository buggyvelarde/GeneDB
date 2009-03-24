package org.genedb.db.domain.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.Exon;
import org.genedb.db.domain.objects.Gap;
import org.genedb.db.domain.objects.Transcript;
import org.genedb.db.domain.objects.TranscriptComponent;

public class MockBasicGeneService implements BasicGeneService {

    @Override
    public Collection<Gap> findGapsOnChromosome(String organismCommonName,
            String chromosomeUniqueName) {
        Collection<Gap>  gap = new ArrayList<Gap>();
        return gap;
    }

    @Override
    public Collection<Gap> findGapsOverlappingRange(String organismCommonName,
            String chromosomeUniqueName, long locMin, long locMax) {
        Collection<Gap>  gap = new ArrayList<Gap>();
        return gap;
    }

    @Override
    public BasicGene findGeneByUniqueName(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> findGeneNamesByPartialName(String search) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<BasicGene> findGenesExtendingIntoRange(
            String organismCommonName, String chromosomeUniqueName, int strand,
            long locMin, long locMax) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<BasicGene> findGenesOnStrand(String organismCommonName,
            String chromosomeUniqueName, int strand) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<BasicGene> findGenesOverlappingRange(
            String organismCommonName, String chromosomeUniqueName, int strand,
            long locMin, long locMax) {
        Collection<BasicGene>  genes = new ArrayList<BasicGene>();
        BasicGene gene = new BasicGene();
        gene.setFmax(7000);
        gene.setFmin(6000);
        gene.setName("testtesttest");
        gene.setOrganism("organism_name");
        gene.setStrand(23);
        gene.setUniqueName("unique_name");
        
        List<Transcript> transcripts = createTranscripts();
        gene.setTranscripts(transcripts);
        genes.add(gene);
        return genes;
    }
    
    private List<Transcript> createTranscripts(){
        List<Transcript> transcripts = new ArrayList<Transcript>();
        for(int i=0; i<10; i++){
            Transcript e = new Transcript();
            //e.setProducts(new ArrayList<String>(){"test1", "test2"});
            e.setComponents(createTranscriptComponents());
            transcripts.add(e);
        }
        return transcripts;
    }
    
    private Set<TranscriptComponent> createTranscriptComponents(){
        Set<TranscriptComponent> components = new HashSet<TranscriptComponent>();
        int start = 1; int stop = 10;
        for(int i=1; i<10; i++){
            start = start*i;
            stop = start + (stop*i);
            TranscriptComponent tc = new Exon(start, stop);
            components.add(tc);
        }
        return components;
    }

}
