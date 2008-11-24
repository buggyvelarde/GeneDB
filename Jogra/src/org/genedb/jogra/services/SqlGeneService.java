package org.genedb.jogra.services;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.genedb.jogra.domain.Gene;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

import com.google.common.collect.Lists;

public class SqlGeneService implements GeneService {

    private JdbcTemplate jdbcTemplate;
    private SqlUpdate sqlUpdate;
	private int transcriptId;
	
	@PostConstruct
	@SuppressWarnings("unused")  // Init method
	private void setUpConstants() {
		String sql = "select cvterm_id from cvterm where name='mRNA'";
		transcriptId = this.jdbcTemplate.queryForInt(sql);
		System.err.println(transcriptId);
	}


    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        String sql = "update feature_cvterm set cvterm_id=? where feature_cvterm.cvterm_id in (?)";
        sqlUpdate = new SqlUpdate();
        sqlUpdate.setDataSource(dataSource);
        sqlUpdate.setSql(sql);
        sqlUpdate.declareParameter(new SqlParameter(Types.INTEGER));
        sqlUpdate.declareParameter(new SqlParameter(Types.CHAR));
        sqlUpdate.compile();


    }

    @Override
    public Gene findGeneByUniqueName(String name) {
        Gene ret = new Gene();

        String sql = "select * from Feature where uniquename='" + name + "'";
        Map<String, Object> map =  this.jdbcTemplate.queryForMap(sql);

      	ret.setUniqueName((String)map.get("uniquename"));
      	ret.setName((String)map.get("name"));
      	
      	int featureId = ((Integer) map.get("feature_id")).intValue();

        sql = "select * from FeatureLoc where feature_id='" + featureId + "'";
        List<Map<String, Object>> names =  this.jdbcTemplate.queryForList(sql);
      	
        List<String> synonyms = Lists.newArrayList();
        for (Map<String, Object> map2 : names) {
			synonyms.add((String)map.get("name"));
		}
        ret.setSynonyms(synonyms);
        
        return ret;
    }

    //@Override
    @SuppressWarnings("unchecked")
	public List<String> findTranscriptNamesByPartialName(String search) {

        String sql = "select uniquename from Feature where uniquename like '%"+search+"%' and type_id="+transcriptId+" order by uniquename";

        Object[] args = new Object[] {search};
        return this.jdbcTemplate.queryForList(sql, String.class);
    }

}
