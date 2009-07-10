package org.genedb.web.mvc.model;

import org.genedb.web.gui.ImageMapSummary;

import org.gmod.schema.utils.PeptideProperties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class DtoDb {
    
    Logger logger = Logger.getLogger(DtoDb.class);

    SimpleJdbcTemplate template;
    
    public int persistDTO(TranscriptDTO dto) {
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
                ":synonyms" +
                ") ", 
                args);
       
    }   
    
    public TranscriptDTO retrieveDTO(int featureId) {
        Integer index = findDTO(featureId);
        
        if (index == null) {
            return null;
        }
        
        TranscriptDTOMapper transcriptDTOMapper = new TranscriptDTOMapper();
        
        TranscriptDTO ret = template.queryForObject(
                "select * from transcript_cache where feature_id='%1'",
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
        ret.setAnAlternateTranscript(rs.getBoolean("anAlternateTranscript"));
        ret.setGeneName(rs.getString("geneName"));
        ret.setLastModified(rs.getLong("lastModified"));
        ret.setMax(rs.getInt("max"));
        ret.setMin(rs.getInt("min"));
        ret.setOrganismCommonName(rs.getString("organismCommonName"));
        ret.setOrganismHtmlShortName(rs.getString("organismHtmlShortName"));
        ret.setProperName(rs.getString("properName"));
        ret.setProteinCoding(rs.getBoolean("proteinCoding"));
        ret.setPseudo(rs.getBoolean("pseudo"));
        ret.setStrand(rs.getShort("strand"));
        ret.setTopLevelFeatureDisplayName(rs.getString("topLevelFeatureDisplayName"));
        ret.setTopLevelFeatureLength(rs.getInt("topLevelFeatureLength"));
        ret.setTopLevelFeatureType(rs.getString("topLevelFeatureType"));
        ret.setTopLevelFeatureUniqueName(rs.getString("topLevelFeatureUniqueName"));
        ret.setTypeDescription(rs.getString("typeDescription"));
        ret.setUniqueName(rs.getString("uniqueName"));
        
        // Array types
        ret.setClusterIds(sqlArrayAsListString(rs, "clusterIds"));
        ret.setComments(sqlArrayAsListString(rs, "comments"));
        ret.setNotes(sqlArrayAsListString(rs, "notes"));
        ret.setObsoleteNames(sqlArrayAsListString(rs, "obsoleteNames"));
        ret.setOrthologueNames(sqlArrayAsListString(rs, "orthologueNames"));
        ret.setPublications(sqlArrayAsListString(rs, "publications"));
        ret.setSynonyms(sqlArrayAsListString(rs, "synonyms"));
        
        // Special types
        ret.setAlgorithmData(objectFromSerializedStream(rs, "algoritmData", Map.class));
        ret.setControlledCurations(objectFromSerializedStream(rs, "controlledCurations", List.class));
        ret.setDbXRefDTOs(objectFromSerializedStream(rs, "dbXRefDTOs", List.class));
        ret.setDomainInformation(objectFromSerializedStream(rs, "domainInformation", List.class));
        ret.setGoBiologicalProcesses(objectFromSerializedStream(rs, "goBiologicalProcesses", List.class));
        ret.setGoCellularComponents(objectFromSerializedStream(rs, "goCellularComponents", List.class));
        ret.setGoMolecularFunctions(objectFromSerializedStream(rs, "goMolecularFunctions", List.class));
        ret.setIms(objectFromSerializedStream(rs, "ims", ImageMapSummary.class));
        ret.setPolypeptideProperties(objectFromSerializedStream(rs, "polypeptideProperties", PeptideProperties.class));
        ret.setProducts(objectFromSerializedStream(rs, "products", List.class));
        ret.setSynonymsByTypes(objectFromSerializedStream(rs, "synonymsByTypes", Map.class));

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

