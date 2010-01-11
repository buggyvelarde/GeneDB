package org.genedb.web.mvc.model.load;


import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.genedb.db.audit.MockChangeSetImpl;
import org.genedb.web.mvc.model.IndexUpdater;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TranscriptLoaderTest {
    Logger logger = Logger.getLogger(TranscriptLoaderTest.class);

    @Autowired
    private IndexUpdater indexUpdater;

    @Autowired
    private TranscriptLoader transcriptLoader;

    @Autowired
    private SimpleJdbcTemplate jdbcTemplate;

    @Before
    public void setUpLogging() {
        String log4jprops = "/log4j.TranscriptLoader.properties";
        URL url = this.getClass().getResource(log4jprops);
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);
    }

    @Test
    public void loadTbruceibrucei427()throws Exception{
        //empty the table
        jdbcTemplate.update("delete from transcript ");

        //load
        int loadCount = transcriptLoader.load("Tbruceibrucei427", 500);

        //Something is loaded
        Assert.isTrue(loadCount > 0);
    }

    @Test
    public void testNewGenesFromChangeSet()throws Exception{
        //Size of records to use for testing
        int limit = 4, offset=limit;

        //Get some gene ids to use for test
        List<Integer> geneIds = findRandomGeneIds(offset, limit);

        //Assert size of genes is the limit asked for
        Assert.isTrue(geneIds.size()==limit);

        //Now delete the transcripts of those genes
        int noOfDeletedRecs = deleteTransciptsByGeneIdsIds(geneIds);

        //Assert those records are truly deleted
        Assert.isTrue(noOfDeletedRecs>0);

        //Set up the changeset
        MockChangeSetImpl changeSet = new MockChangeSetImpl();
        Map<Class<? extends Feature>, List<Integer>> map = changeSet.getNewMap();
        map.put(AbstractGene.class, geneIds);

        //Proper test begins i.e. update the cache with transcripts
        int inserted = indexUpdater.updateTranscriptCache(changeSet);

        //Assert that those new records are inserted by the transcriptUpdater
        Assert.isTrue(findGeneIds(geneIds).size()>= inserted);
    }

    @Test
    public void testChangedGenesFromChangeSet()throws Exception{
        //Size of records to use for testing
        int limit = 4, offset=limit;

        //Get some gene ids to use for test
        List<Integer> geneIds = findRandomGeneIds(offset, limit);

        //Assert size of genes is the limit asked for
        Assert.isTrue(geneIds.size()==limit);

        //Set up the changeset
        MockChangeSetImpl changeSet = new MockChangeSetImpl();
        Map<Class<? extends Feature>, List<Integer>> map = changeSet.getChangedMap();
        map.put(AbstractGene.class, geneIds);

        //Proper test begins i.e. update the cache with transcripts
        int changed = indexUpdater.updateTranscriptCache(changeSet);

        //Assert that those changed records are updated by the transcriptUpdater
        Assert.isTrue(changed == geneIds.size());
    }



    @Test
    public void testDeletedGenesFromChangeSet()throws Exception{
        //Size of records to use for testing
        int limit = 2, offset=limit;

        //Get some gene ids to use for test
        List<Integer> geneIds = findRandomGeneIds(offset, limit);

        //Assert size of genes is the limit asked for
        Assert.isTrue(geneIds.size()==limit);

        //Set up the changeset
        MockChangeSetImpl changeSet = new MockChangeSetImpl();
        Map<Class<? extends Feature>, List<Integer>> map = changeSet.getDeletedMap();
        map.put(AbstractGene.class, geneIds);

        //Proper test begins i.e. delete the transcripts in the cache
        int deleted = indexUpdater.updateTranscriptCache(changeSet);

        //Assert that those records are deleted by the transcriptUpdater
        Assert.isTrue(deleted == geneIds.size());

        //Restore deleted
        map.clear();
        map = changeSet.getNewMap();
        map.put(AbstractGene.class, geneIds);
        int restored = indexUpdater.updateTranscriptCache(changeSet);

        //Assert that deleted is restored
        Assert.isTrue(deleted == restored);
    }

    @Test
    public void testNewTranscriptsFromChangeSet()throws Exception{
        //Size of records to use for testing
        int limit = 4, offset=limit;

        //Get some transcript ids to use for test
        List<Integer> transcriptIds = findRandomTranscriptIds(offset, limit);

        //Assert size of transcripts is the limit asked for
        Assert.isTrue(transcriptIds.size()==limit);

        //Now delete those transcripts
        int noOfDeletedRecs = deleteTransciptsByTranscriptIds(transcriptIds);

        //Assert those records are truly deleted
        Assert.isTrue(noOfDeletedRecs==limit);

        //Set up the changeset
        MockChangeSetImpl changeSet = new MockChangeSetImpl();
        Map<Class<? extends Feature>, List<Integer>> map = changeSet.getNewMap();
        map.put(Transcript.class, transcriptIds);

        //Proper test begins i.e. update the cache with transcripts
        int inserted = indexUpdater.updateTranscriptCache(changeSet);

        //Assert that those new records are inserted by the transcriptUpdater
        Assert.isTrue(inserted == findTranscriptIds(transcriptIds).size());
    }


    @Test
    public void testChangedTranscriptsFromChangeSet()throws Exception{
        //Size of records to use for testing
        int limit = 4, offset=limit;

        //Get some transcript ids to use for test
        List<Integer> transcriptIds = findRandomTranscriptIds(offset, limit);

        //Assert size of transcripts is the limit asked for
        Assert.isTrue(transcriptIds.size()==limit);

        //Set up the changeset
        MockChangeSetImpl changeSet = new MockChangeSetImpl();
        Map<Class<? extends Feature>, List<Integer>> map = changeSet.getChangedMap();
        map.put(Transcript.class, transcriptIds);

        //Proper test begins i.e. update the cache with transcripts
        int changed = indexUpdater.updateTranscriptCache(changeSet);

        //Assert that those changed records are updated by the transcriptUpdater
        Assert.isTrue(changed == transcriptIds.size());
    }



    @Test
    public void testDeletedTranscriptsFromChangeSet()throws Exception{
        //Size of records to use for testing
        int limit = 2, offset=limit;

        //Get some transcript ids to use for test
        List<Integer> transcriptIds = findRandomTranscriptIds(offset, limit);

        //Assert size of transcript is the limit asked for
        Assert.isTrue(transcriptIds.size()==limit);

        //Set up the changeset
        MockChangeSetImpl changeSet = new MockChangeSetImpl();
        Map<Class<? extends Feature>, List<Integer>> map = changeSet.getDeletedMap();
        map.put(Transcript.class, transcriptIds);

        //Proper test begins i.e. delete the transcripts in the cache
        int deleted = indexUpdater.updateTranscriptCache(changeSet);

        //Assert that those records are deleted by the transcriptUpdater
        Assert.isTrue(deleted == transcriptIds.size());

        //Restore deleted
        map.clear();
        map = changeSet.getNewMap();
        map.put(Transcript.class, transcriptIds);
        int restored = indexUpdater.updateTranscriptCache(changeSet);

        //Assert that deleted is restored
        Assert.isTrue(deleted == restored);
    }


    private List<Integer> findRandomGeneIds(int offset, int limit){
        return jdbcTemplate.query(
                " select gene_id from transcript" +
                " limit ? offset ?",
                new ParameterizedRowMapper<Integer>(){
                    @Override
                    public Integer mapRow(ResultSet rs, int rowCount)
                            throws SQLException {
                        return rs.getInt("gene_id");
                    }
                },
                offset, limit);

    }


    private List<Integer> findRandomTranscriptIds(int offset, int limit){
        return jdbcTemplate.query(
                " select transcript_id from transcript" +
                " limit ? offset ?",
                new ParameterizedRowMapper<Integer>(){
                    @Override
                    public Integer mapRow(ResultSet rs, int rowCount)
                            throws SQLException {
                        return rs.getInt("transcript_id");
                    }
                },
                offset, limit);

    }


    private List<Integer> findGeneIds(List<Integer> geneIds){
        String sql =
            " select gene_id from transcript" +
            " where gene_id in(:placeholder)";
        sql = sql.replace(":placeholder", formatPlaceholders(geneIds.size()));
        return jdbcTemplate.query(
                sql,
                new ParameterizedRowMapper<Integer>(){
                    @Override
                    public Integer mapRow(ResultSet rs, int rowCount)
                            throws SQLException {
                        return rs.getInt("gene_id");
                    }
                },
                geneIds.toArray((Object[])new Integer[0]));

    }


    private List<Integer> findTranscriptIds(List<Integer> transcriptIds){
        String sql =
            " select transcript_id from transcript" +
            " where transcript_id in(:placeholder)";
        sql = sql.replace(":placeholder", formatPlaceholders(transcriptIds.size()));
        return jdbcTemplate.query(
                sql,
                new ParameterizedRowMapper<Integer>(){
                    @Override
                    public Integer mapRow(ResultSet rs, int rowCount)
                            throws SQLException {
                        return rs.getInt("transcript_id");
                    }
                },
                transcriptIds.toArray((Object[])new Integer[0]));

    }

    private int deleteTransciptsByGeneIdsIds(List<Integer> ids){
        String sql = "delete from transcript " +
        "where gene_id " +
        "in(:placeholders)";
        sql =  sql.replace(":placeholders", formatPlaceholders(ids.size()));
        Object obj[] = ids.toArray(new Integer[0]);
        return jdbcTemplate.update(
                sql, obj);
    }

    private int deleteTransciptsByTranscriptIds(List<Integer> ids){
        String sql = "delete from transcript " +
        "where transcript_id " +
        "in(:placeholders)";
        sql =  sql.replace(":placeholders", formatPlaceholders(ids.size()));
        Object obj[] = ids.toArray(new Integer[0]);
        return jdbcTemplate.update(
                sql, obj);
    }

    private String formatPlaceholders(int count){
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<count; ++i){
            sb.append("?");
            if(i+1<count){
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
