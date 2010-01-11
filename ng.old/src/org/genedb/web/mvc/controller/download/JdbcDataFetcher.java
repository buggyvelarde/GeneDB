package org.genedb.web.mvc.controller.download;

import org.genedb.web.mvc.model.BerkeleyMapFactory;
import org.genedb.web.mvc.model.FeatureCvTermDTO;
import org.genedb.web.mvc.model.TranscriptDTO;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JdbcDataFetcher implements DataFetcher<String> {

    private Logger logger = Logger.getLogger(JdbcDataFetcher.class);

    private static final String SQL = "select * from transcript where transcript_uniquename in (?)";

    private JdbcDataRowMapper jdbcDataRowMapper;

    public void setJdbcDataRowMapper(JdbcDataRowMapper jdbcDataRowMapper) {
        this.jdbcDataRowMapper = jdbcDataRowMapper;
    }

    private SimpleJdbcTemplate template;

    //@Required
    public void setDatasource(DataSource dataSource) {
        this.template = new SimpleJdbcTemplate(dataSource);
    }

    public TroubleTrackingIterator<DataRow> iterator(List<String> ids, String delimeter) {

        String idList = StringUtils.collectionToCommaDelimitedString(ids);
        idList += ":mRNA"; // FIXME
        List<DataRow> rows = this.template.query(SQL, jdbcDataRowMapper, idList);
        logger.error("The number of rows is "+rows.size()+" with an arg of '"+idList+"'");
        //return rows.iterator();
        return null; // FIXME
    }

}

class JdbcDataRowMapper implements RowMapper<DataRow> {

    private BerkeleyMapFactory bmf;

    public void setBmf(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }

    @Override
    public DataRow mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        JdbcDataRow jdr = new JdbcDataRow(resultSet);
        TranscriptDTO dto = bmf.getDtoMap().get(resultSet.getInt("transcript_id"));
        jdr.setDto(dto);
        return jdr;
    }

}

class JdbcDataRow implements DataRow {

    private static final Logger logger = Logger.getLogger(JdbcDataRow.class);

    private Map<OutputOption, String> results = Maps.newHashMap();
    private TranscriptDTO dto;
    private String fieldDelim = ";";


    @Override
    public String getValue(OutputOption oo) {
        if (results.containsKey(oo)) {
            return results.get(oo);
        }

        Map<String, List<String>> mapping = dto.getSynonymsByTypes();
        logger.error("The mapping is '"+mapping+"'");




        // Get the data from the DTO
        switch (oo) {
        case EC_NUMBERS:
        case ORGANISM:
        case CHROMOSOME:
        case SYS_ID:
            return "who_knows?"; // FIXME

        case GENE_TYPE:
            return dto.getTypeDescription();

        case GO_IDS:
            break;

        case GPI_ANCHOR:
            break;

        case INTERPRO_IDS:
            break;

        case ISOELECTRIC_POINT:
            break;

        case LOCATION:
            break;

        case MOL_WEIGHT:
            return dto.getPolypeptideProperties().getMass();

        case NUM_TM_DOMAINS:
            break;

        case PFAM_IDS:
            break;

        case PREV_SYS_ID:
            break;

        case PRIMARY_NAME:
            return dto.getGeneName();

        case PRODUCT:
            List<String> list = Lists.newArrayList();
            for (FeatureCvTermDTO fct : dto.getProducts()) {
                list.add(fct.getTypeName());
            }
            return StringUtils.collectionToDelimitedString(list, fieldDelim);

        case SIG_P:
            return dto.getAlgorithmData().containsKey("SignalP") ? "true" : "";

        case SYNONYMS:
            return "look in dto";
        }
        return "";
    }

    public JdbcDataRow(ResultSet resultSet) {

        for (OutputOption oo : OutputOption.values()) {
            switch (oo) {
            case CHROMOSOME:
                try {
                    results.put(oo, resultSet.getString("top_level_feature_uniquename"));
                } catch (SQLException exp) {
                    exp.printStackTrace();
                    results.put(oo, "Internal problem");
                }
                break;

//        case LOCATION:
//            try {
//                String fmin = resultSet.getString("gene_fmin");
//                String fmax = resultSet.getString("gene_fmax");
//                int strand = resultSet.getInt("gene_strand");
//                // TODO Interbase
//                results.put(oo, ""+ strand + " " + fmin + "-" +fmax);
//            } catch (SQLException exp) {
//                exp.printStackTrace();
//                results.put(oo, "Internal problem");
//            }
//            break;

            case ORGANISM:
                try {
                    results.put(oo, resultSet.getString("organism_common_name"));
                } catch (SQLException exp) {
                    exp.printStackTrace();
                    results.put(oo, "Internal problem");
                }
                break;

            case SYS_ID:
                try {
                    results.put(oo, resultSet.getString("transcript_uniquename"));
                } catch (SQLException exp) {
                    exp.printStackTrace();
                    results.put(oo, "Internal problem");
                }
                break;

            default:
                break;
            }
        }
    }

    public void setDto(TranscriptDTO dto) {
        this.dto = dto;
    }

}
