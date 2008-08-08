package org.genedb.db.loading;


import org.genedb.db.dao.GeneralDao;

import org.gmod.schema.mapped.Db;

import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;

public class DbUtilsBean implements InitializingBean {
    
    private GeneralDao generalDao;
    private Map<String, String> aliases = new HashMap<String, String>(0);
    
    public Db getDbByName(String dbName) {
        String lookup = dbName;
        String dbNameUpper = dbName.toUpperCase(); 
        if (aliases.containsKey(dbNameUpper)) {
            lookup = aliases.get(dbNameUpper);
        }
        return generalDao.getDbByName(lookup);
    }
    
    

    public void afterPropertiesSet() {
//        so = cvDao.getCvByName("sequence").get(0);
//        Cv CV_GENEDB = cvDao.getCvByName("genedb_misc").get(0);
//        GENEDB_TOP_LEVEL = cvDao.getCvTermByNameInCv(QUAL_TOP_LEVEL, CV_GENEDB).get(0);
    }

    public void setAliases(Map<String, String> mixedCaseAliases) {
        for (Map.Entry<String, String> entry : mixedCaseAliases.entrySet()) {
            this.aliases.put(entry.getKey().toUpperCase(), entry.getValue());
        }
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }
    
}
