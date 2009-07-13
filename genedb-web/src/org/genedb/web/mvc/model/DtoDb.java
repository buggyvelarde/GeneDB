package org.genedb.web.mvc.model;

import org.genedb.web.gui.ImageMapSummary;

import org.gmod.schema.utils.PeptideProperties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;



import com.google.common.collect.Maps;

@Transactional
public class DtoDb {
    
    Logger logger = Logger.getLogger(DtoDb.class);

    private SimpleJdbcTemplate template;
    
    public int persistDTO(TranscriptDTO dto) throws Exception{
        logger.debug("persistDTO...");
        //Integer index = findDTO(dto.getFeatureId());
        Integer index = findDTO(dto.getTranscriptId());
        if (index != null) {
            return 0;
        }

        Map<String, Object> args = Maps.newHashMap();
        args.put("transcript_id", dto.getTranscriptId());
        args.put("alternative_transcript", dto.isAnAlternateTranscript());
        args.put("gene_name", dto.getGeneName());
        if (dto.getLastModified()>0){
            args.put("last_modified_date", new Date(dto.getLastModified()));
        }else{
            args.put("last_modified_date", null);
        }
        args.put("max", dto.getMax());
        args.put("min", dto.getMin());
        args.put("organism_common_name", dto.getOrganismCommonName());
        args.put("organism_html_short_name", dto.getOrganismHtmlShortName());
        args.put("proper_name", dto.getProperName());
        args.put("protein_coding", dto.isProteinCoding());
        args.put("pseudo", dto.isPseudo());
        args.put("strand", dto.getStrand());
        args.put("top_level_feature_displayname", dto.getTopLevelFeatureDisplayName());
        args.put("top_level_feature_length", dto.getTopLevelFeatureLength());
        args.put("top_level_feature_type", dto.getTopLevelFeatureType());
        args.put("top_level_feature_uniquename", dto.getTopLevelFeatureUniqueName());
        args.put("type_description", dto.getTypeDescription());
        args.put("uniquename", dto.getUniqueName());
        
        args.put("cluster_ids", new DtoArrayField(dto.getClusterIds()));
        args.put("comments", new DtoArrayField(dto.getComments()));
        args.put("notes", new DtoArrayField(dto.getNotes()));
        args.put("obsolete_names", new DtoArrayField(dto.getObsoleteNames()));
        args.put("orthologue_names", new DtoArrayField(dto.getOrthologueNames()));
        args.put("publications", new DtoArrayField(dto.getPublications()));
        args.put("synonyms", new DtoArrayField(dto.getSynonyms()));
        
        args.put("algorithm_data", getBytes(getBytes(dto.getAlgorithmData())));
        args.put("controlled_curation", getBytes(dto.getControlledCurations()));
        args.put("dbx_ref_dtos", getBytes(dto.getDbXRefDTOs()));
        args.put("domain_information", getBytes(dto.getDomainInformation()));
        args.put("go_biological_processes", getBytes(dto.getGoBiologicalProcesses()));
        args.put("go_cellular_components", getBytes(dto.getGoCellularComponents()));
        args.put("go_molecular_functions", getBytes(dto.getGoMolecularFunctions()));
        args.put("image_map_summary", getBytes(dto.getIms()));
        args.put("polypeptide_properties", getBytes(dto.getPolypeptideProperties()));
        args.put("products", getBytes(dto.getProducts()));        
        args.put("synonyms_by_types", getBytes(dto.getSynonymsByTypes()));

        return template.update("insert into transcript_cache " +
        		
                " values(nextval('transcript_cache_seq')," +
                ":transcript_id," +
                ":alternative_transcript," +
                ":gene_name," +
                ":last_modified_date," +
                ":max," +
                ":min," +
                ":organism_common_name," +
                ":organism_html_short_name," +
                ":proper_name," +
                ":protein_coding," +
                ":pseudo," +
                ":strand," +
                ":top_level_feature_displayname," +
                ":top_level_feature_length," +
                ":top_level_feature_type," +
                ":top_level_feature_uniquename," +
                ":type_description," +
                ":uniquename," +
                
                ":cluster_ids," +
                ":comments," +
                ":notes," +
                ":obsolete_names," +
                ":orthologue_names," +
                ":publications," +
                ":synonyms," +
                
                ":algorithm_data," +
                ":controlled_curation," +
                ":dbx_ref_dtos," +
                ":domain_information," +
                ":go_biological_processes," +
                ":go_cellular_components," +
                ":go_molecular_functions," +
                ":image_map_summary," +
                ":polypeptide_properties," +
                ":products," +
                ":synonyms_by_types" +
                ") ", 
                args);
       
    }  
    
    private byte[] getBytes(Object value)throws Exception{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(value);
        oos.flush();
        oos.close();
        bos.close();
        return bos.toByteArray();
    }
    
    public TranscriptDTO retrieveDTO(int featureId) {
        Integer index = findDTO(featureId);
        
        if (index == null) {
            return null;
        }
        
        TranscriptDTOMapper transcriptDTOMapper = new TranscriptDTOMapper();
        
        TranscriptDTO ret = template.queryForObject(
                "select * from transcript_cache where transcript_cache_id=?",
                transcriptDTOMapper,
                index);
        
        return ret;
    }
     

    private Integer findDTO(int featureId) {
        logger.debug("findDTO, featureId: " + featureId);
        try{
            return template.queryForInt("select transcript_cache_id from transcript_cache where transcript_id=?", featureId);
        }catch(EmptyResultDataAccessException e){
            logger.warn(String.format("No feature id %d found...", featureId));
            return null;
        }
    }
    
    
    
    class TranscriptDTOMapper implements ParameterizedRowMapper<TranscriptDTO> {
        
        @SuppressWarnings("unchecked")
        @Override
        public TranscriptDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            TranscriptDTO ret = new TranscriptDTO();
        
        // Simple types
        ret.setAnAlternateTranscript(rs.getBoolean("alternative_transcript"));
        ret.setGeneName(rs.getString("gene_name"));
        
        Date lastModifiedDate = rs.getDate("last_modified_date"); 
        if(lastModifiedDate!=null){
            ret.setLastModified(lastModifiedDate.getTime());
        }
        
        ret.setMax(rs.getInt("max"));
        ret.setMin(rs.getInt("min"));
        ret.setOrganismCommonName(rs.getString("organism_common_name"));
        ret.setOrganismHtmlShortName(rs.getString("organism_html_short_name"));
        ret.setProperName(rs.getString("proper_name"));
        ret.setProteinCoding(rs.getBoolean("protein_coding"));
        ret.setPseudo(rs.getBoolean("pseudo"));
        ret.setStrand(rs.getShort("strand"));
        ret.setTopLevelFeatureDisplayName(rs.getString("top_level_feature_displayname"));
        ret.setTopLevelFeatureLength(rs.getInt("top_level_feature_length"));
        ret.setTopLevelFeatureType(rs.getString("top_level_feature_type"));
        ret.setTopLevelFeatureUniqueName(rs.getString("top_level_feature_uniquename"));
        ret.setTypeDescription(rs.getString("type_description"));
        ret.setUniqueName(rs.getString("uniquename"));
        
        // Array types
        ret.setClusterIds(sqlArrayAsListString(rs, "cluster_ids"));
        ret.setComments(sqlArrayAsListString(rs, "comments"));
        ret.setNotes(sqlArrayAsListString(rs, "notes"));
        ret.setObsoleteNames(sqlArrayAsListString(rs, "obsolete_names"));
        ret.setOrthologueNames(sqlArrayAsListString(rs, "orthologue_names"));
        ret.setPublications(sqlArrayAsListString(rs, "publications"));
        ret.setSynonyms(sqlArrayAsListString(rs, "synonyms"));
        
        // Special types
        ret.setAlgorithmData(objectFromSerializedStream(rs, "algorithm_data", Map.class));
        ret.setControlledCurations(objectFromSerializedStream(rs, "controlled_curation", List.class));
        ret.setDbXRefDTOs(objectFromSerializedStream(rs, "dbX_ref_dtos", List.class));
        ret.setDomainInformation(objectFromSerializedStream(rs, "domain_information", List.class));
        ret.setGoBiologicalProcesses(objectFromSerializedStream(rs, "go_biological_processes", List.class));
        ret.setGoCellularComponents(objectFromSerializedStream(rs, "go_cellular_components", List.class));
        ret.setGoMolecularFunctions(objectFromSerializedStream(rs, "go_molecular_functions", List.class));
        ret.setIms(objectFromSerializedStream(rs, "image_map_summary", ImageMapSummary.class));
        ret.setPolypeptideProperties(objectFromSerializedStream(rs, "polypeptide_properties", PeptideProperties.class));
        ret.setProducts(objectFromSerializedStream(rs, "products", List.class));
        ret.setSynonymsByTypes(objectFromSerializedStream(rs, "synonyms_by_types", Map.class));

        return ret;
    }
        
        private <T> T objectFromSerializedStream(ResultSet rs, String columnName, Class<T> expectedType) {

            ObjectInputStream ois = null;
            try {
                byte[] colValue = rs.getBytes(columnName);
                ois = new ObjectInputStream(new ByteArrayInputStream(colValue));
                @SuppressWarnings("unchecked") T ret = (T) ois.readObject();
                ois.close();
                ois = null;
                return ret;
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(ois);
            }
            throw new RuntimeException(
                    String.format("Unable to deserialize '%s' as '%s' from dto cache", columnName, expectedType.getCanonicalName()));
        }
        
        private List<String> sqlArrayAsListString(ResultSet rs, String columnName) throws SQLException {
            return Arrays.asList((String[])rs.getArray(columnName).getArray());
        }
    }
    
    class TranscriptDTOReverseMapper {

        public Map<String, Object> mapRow(TranscriptDTO td) throws SQLException {
            Map<String, Object> ret = Maps.newHashMap();
            
            // Simple types
            ret.put("anAlternateTranscript", td.isAnAlternateTranscript());
            ret.put("geneName", td.getGeneName());
            ret.put("lastModified", td.getLastModified());
            ret.put("max", td.getMax());
            ret.put("min", td.getMin());
            ret.put("organismCommonName", td.getOrganismCommonName());
            ret.put("organismHtmlShortName", td.getOrganismHtmlShortName());
            ret.put("properName", td.getProperName());
            ret.put("proteinCoding", td.isProteinCoding());
            ret.put("pseudo", td.isPseudo());
            ret.put("strand", td.getStrand());
            ret.put("topLevelFeatureDisplayName", td.getTopLevelFeatureDisplayName());
            ret.put("topLevelFeatureLength", td.getTopLevelFeatureLength());
            ret.put("topLevelFeatureType", td.getTopLevelFeatureType());
            ret.put("topLevelFeatureUniqueName", td.getTopLevelFeatureUniqueName());
            ret.put("typeDescription", td.getTypeDescription());
            ret.put("uniqueName", td.getUniqueName());
            
            // Array types
            ret.put("clusterIds", td.getClusterIds());
            ret.put("comments", td.getComments());
            ret.put("notes", td.getNotes());
            ret.put("obsoleteNames", td.getObsoleteNames());
            ret.put("orthologueNames", td.getOrthologueNames());
            ret.put("publications", td.getPublications());
            ret.put("synonyms", td.getSynonyms());
            
            // Special types
            ret.put("algoritmData", td.getAlgorithmData());
            ret.put("controlledCurations", td.getControlledCurations());
            ret.put("dbXRefDTOs", td.getDbXRefDTOs());
            ret.put("domainInformation", td.getDomainInformation());
            ret.put("goBiologicalProcesses", td.getGoBiologicalProcesses());
            ret.put("goCellularComponents", td.getGoCellularComponents());
            ret.put("goMolecularFunctions", td.getGoMolecularFunctions());
            ret.put("ims", td.getIms());
            ret.put("polypeptideProperties", td.getPolypeptideProperties());
            ret.put("products", td.getProducts());
            ret.put("synonymsByTypes", td.getSynonymsByTypes());

            return ret;
        }


    }

    public SimpleJdbcTemplate getTemplate() {
        return template;
    }


    public void setTemplate(SimpleJdbcTemplate template) {
        this.template = template;
    }

}

