package org.genedb.web.mvc.model.simple;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleGeneMapper extends SimpleFeatureMapper implements RowMapper<SimpleGene> {

    Logger logger = Logger.getLogger(SimpleGeneMapper.class);

    public static final String GENE_TYPE_SQL =
            " select cvterm_id " + " from cvterm cvt, cv " + " where cvt.cv_id = cv.cv_id "
                    + " and cvt.name in ('gene', 'pseudogene')" + " and cv.name = 'sequence'";

    public static final String SQL =
            " select fl.*, f.*, cvt.name as cvtname " + " from feature f, featureloc fl, cvterm cvt "
                    + " where f.feature_id = fl.feature_id" + " and f.type_id = cvt.cvterm_id " + " and f.type_id in ("
                    + GENE_TYPE_SQL + " )";

    public static final String SQL_WITH_PARAMS =
            " select fl.*, f.*, cvt.name as cvtname " + " from feature f, featureloc fl, cvterm cvt "
                    + " where f.feature_id = fl.feature_id" + " and f.type_id = cvt.cvterm_id "
                    + " and f.organism_id = ? " + " and f.type_id in (" + GENE_TYPE_SQL + ")";

    public static final String GET_GENES_SQL_WITH_LIMIT_AND_OFFSET_PARAMS =
            " select fl.*, f.*, cvt.name as cvtname " + " from feature f, featureloc fl, cvterm cvt "
                    + " where f.feature_id = fl.feature_id" + " and f.type_id = cvt.cvterm_id "
                    + " and f.organism_id = ? " + " and f.type_id in (" + GENE_TYPE_SQL + ")" + " limit ?"
                    + " offset ?";

    public static final String SQL_WITH_GENE_ID_PARAMS =
            " select fl.*, f.*, cvt.name as cvtname " + " from feature f, featureloc fl, cvterm cvt "
                    + " where f.feature_id = fl.feature_id" + " and f.type_id = cvt.cvterm_id "
                    + " and f.feature_id in (:placeholders)";

    public static final String SQL_WITH_TRANSCRIPT_ID_PARAM =
            " select fl.*, f.*, cvt.name as cvtname" + " from feature f, featureloc fl, cvterm cvt"
                    + " where f.feature_id = fl.feature_id" + " and f.type_id = cvt.cvterm_id "
                    + " and f.feature_id in (" + " select object_id" + " from feature_relationship fr"
                    + " where fr.subject_id = ?)";

    @Override
    public SimpleGene mapRow(ResultSet rs, int rowNum) throws SQLException {
        SimpleGene gene = new SimpleGene();
        super.mapRow(gene, rs);
        gene.setFmin(rs.getInt("fmin"));
        gene.setSourceFeatureId(rs.getInt("srcfeature_id"));
        gene.setCvtName(rs.getString("cvtname"));
        // gene.setTopLevelFeatureUniqueName(rs.getString("top_level_name"));
        return gene;
    }

}
