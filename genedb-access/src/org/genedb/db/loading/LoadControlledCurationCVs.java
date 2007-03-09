/*
 * Copyright (c) 2006 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

package org.genedb.db.loading;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;

import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.cv.CvTermRelationship;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;



/**
 * This class is the main entry point for the new GeneDB data miners. It's designed to be
 * called from the command-line. It looks for a config. file which specifies which files
 * to process.
 *
 * Usage: NewRunner common_nane [config_file]
 *
 *
 * @author Chinmay Patel (cp2)
 */
public class LoadControlledCurationCVs implements ApplicationContextAware {

    private static String usage="LoadControlledCuration";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private ApplicationContext applicationContext;

    private CvDao cvDao;

    private GeneralDao generalDao;


    /**
     * This is called once the ApplicationContext has set up all of this
     * beans properties. It fetches/creates beans which can't be injected
     * as they depend on command-line args
     */
    public void afterPropertiesSet() {

    }

    private CharSequence blankString(char c, int size) {
        StringBuilder buf = new StringBuilder(size);
        for (int i =0; i < size; i++) {
            buf.append(c);
        }
        return buf;
    }



    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    /**
     * Main entry point. It uses a BeanPostProcessor to apply a set of overrides
     * based on a Properties file, based on the organism. This is passed in on
     * the command-line.
     *
     * @param args organism_common_name, [conf file path]
     * @throws IOException 
     */
    public static void main (String[] args) throws IOException {

        if (args.length != 0) {
            System.err.println("Unexpected Argument\n"+usage);
            System.exit(1);
        }

        // Override properties in Spring config file (using a
        // BeanFactoryPostProcessor) based on command-line args
        Properties overrideProps = new Properties();
        overrideProps.setProperty("dataSource.username", "chado");

        PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);

        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"NewRunner.xml"});

        LoadControlledCurationCVs lccc = (LoadControlledCurationCVs) ctx.getBean("loadControlledCuration", NewRunner.class);
        lccc.loadCvTerms();
        lccc.loadRileyDb();

    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }


    private void loadCvTerms() throws IOException {
        /* Load the cvterms from the file. 
         * 
         */

        // FIXME Put input file into CVS
        // FIXME Don't load PATO like this
        BufferedReader in = new BufferedReader(new FileReader("/nfs/team81/cp2/Scripts/cvterms"));
        String str;
        while (((str = in.readLine()) != null)) {
            String sections[] = str.split("\t");
            CvTerm cvTerm = this.cvDao.getCvTermByNameAndCvName(sections[1], "CC_%");
            if (cvTerm == null) {
                // Need to create a new CvTerm
                Db db = generalDao.getDbByName("CCGEN");
                String accession = "CCGEN_" + sections[1];
                DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(db,accession);
                if (dbXRef == null) { 
                    dbXRef = new DbXRef(db, accession);
                    generalDao.persist(dbXRef);
                }
                String name = "CC_" + sections[0];
                Cv cv = this.cvDao.getCvByName(name).get(0);
                if (cv == null) {
                    // TODO Do we want to create cvs dynamically? Or bail
                    //cv = new Cv();
                    //cv = new Cv(name);
                    //cv.setName(name);
                    //this.cvDao.persist(cv);
                    throw new RuntimeException("Can't find required cv of name'"+name+"'");
                }
                cvTerm = new CvTerm(cv, dbXRef, sections[1], sections[1]);
                this.cvDao.persist(cvTerm);
            }
        }
        in.close();
    }

    private void loadRileyDb() throws IOException {

        BufferedReader in = new BufferedReader(new FileReader("/nfs/pathdb/prod/data/input/linksManager/RILEY.dat"));
        String parent = null;
        CvTerm parentId = null;
        String child = null;
        CvTerm childId = null;
        Cv CV_RELATION = cvDao.getCvByName("relationship").get(0);
        CvTerm REL_PART_OF = cvDao.getCvTermByNameInCv("part_of", CV_RELATION).get(0);
        String str;
        while ((str = in.readLine()) != null) {
            String sections[] = str.split("\t");
            CvTerm cvTerm = this.cvDao.getCvTermByNameAndCvName(sections[2], "RILEY");
            if(cvTerm == null){
                cvTerm = new CvTerm();
                Db db = generalDao.getDbByName("RILEY");
                DbXRef dbXRef = new DbXRef(db, sections[1]);
                generalDao.persist(dbXRef);
                Cv cv = this.cvDao.getCvByName("RILEY").get(0);
                cvTerm = new CvTerm(cv, dbXRef, sections[2], sections[2]);
                this.cvDao.persist(cvTerm);

                if (!sections[1].startsWith("0.0")) {
                    String temp[] = sections[1].split("\\.");
                    if (parent != null) {
                        if (parent.equals(temp[0])){
                            if (child.equals(temp[1])) {
                                CvTermRelationship ctr = new CvTermRelationship(cvTerm,childId,REL_PART_OF);
                                this.cvDao.persist(ctr);
                            } else {
                                child = temp[1];
                                childId = cvTerm;
                                CvTermRelationship ctr = new CvTermRelationship(cvTerm,parentId,REL_PART_OF);
                                this.cvDao.persist(ctr);
                            }
                        } else {
                            parent = temp[0];
                            child = temp[1];
                            parentId = cvTerm;
                        }
                    } else {
                        parent = temp[0];
                        child = temp[1];
                        parentId = cvTerm;
                    }
                }
            }
        }
        in.close();
    }


}
