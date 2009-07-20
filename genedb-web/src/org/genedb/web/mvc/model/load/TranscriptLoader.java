package org.genedb.web.mvc.model.load;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
/**
 * 
 * @author lo2@sangerinstitute
 *
 */
@Transactional
public class TranscriptLoader {
    
    Logger logger = Logger.getLogger(TranscriptLoader.class);

    private SimpleJdbcTemplate template;
    
    public static void main(String args[]){
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"TranscriptLoaderTest-context.xml.xml"});
        TranscriptLoader transcriptLoader = ctx.getBean("transcriptLoader", TranscriptLoader.class);
        transcriptLoader.load("Tbruceibrucei427");
    }
    
    public void loadAll(){
        //Get all organisms
        List<OrganismMapper> organismList = template.query(
               OrganismMapper.SQL, new OrganismMapper());
        
           
        template.query(GeneMapper.SQL, new GeneMapper(getOrganisms(organismList), template));
    
    }
    

    /**
     * Choose organism to load
     * @param organimsName
     */
    public void load(String organimsName){
        

        OrganismMapper organism = template.queryForObject(
                "select * from organism where common_name = ?", new OrganismMapper(), organimsName);
        
        String sql = " select * " +
                " from feature f, featureloc fl " +
                " where f.feature_id = fl.feature_id" +
                " and f.organism_id = ? " +
                " and f.type_id in (select cvterm_id from cvterm where name in ('gene', 'pseudogene'))";        
        template.query(sql, new GeneMapper(organism, template), organism.getOrganismId());
    
    }
    
    private Map<Integer, OrganismMapper> getOrganisms(List<OrganismMapper> organismList){
        Map<Integer, OrganismMapper> map = new HashMap<Integer, OrganismMapper>();
        for(OrganismMapper organismMapper : organismList){
            map.put(organismMapper.getOrganismId(), organismMapper);
        }
        return map;
    }

    public SimpleJdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(SimpleJdbcTemplate template) {
        this.template = template;
    }
    
    
    
//    /**
//     * Get all the transcripts type id
//     * @return
//     */
//    private List<Integer> getTranscriptTypes(){
//        String sql = "select cvterm_id from cvterm where name in ('transcript', 'mRNA')";
//        
//        ParameterizedRowMapper<Integer> mapper = new ParameterizedRowMapper<Integer>() {
//            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
//                return rs.getInt("cvterm_id");                
//            }
//        };
//        return this.template.query(sql, mapper);
//    }
//        
//
//    private <T> String createPlaceholders(List<T> list){
//        //Create placeholder text
//        StringBuffer placeholders = new StringBuffer();
//        for(Iterator<T> iter = list.iterator(); iter.hasNext();){
//            placeholders.append("?");
//            if (iter.hasNext()){
//                placeholders.append(",");
//            }
//        }
//        return placeholders.toString();
//    }
}
