package org.genedb.web.mvc.controller;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.biojava.bio.BioException;
import org.biojava.bio.proteomics.IsoelectricPointCalc;
import org.biojava.bio.proteomics.MassCalc;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;
import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.PhylogenyDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.web.menu.CompositeMenu;
import org.genedb.web.menu.Menu;
import org.genedb.web.menu.SimpleMenu;
import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.phylogeny.Phylonode;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;
import org.gmod.schema.utils.CountedName;
import org.gmod.schema.utils.PeptideProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 * 
 * @author Adrian Tivey
 */
public class SearchController extends MultiActionController implements InitializingBean {

    private SequenceDao sequenceDao;
    private CvDao cvDao;
    private PhylogenyDao phylogenyDao;
    private String listProductsView;

    public void setListProductsView(String listProductsView) {
        this.listProductsView = listProductsView;
    }

    public void setPhylogenyDao(PhylogenyDao phylogenyDao) {
        this.phylogenyDao = phylogenyDao;
    }

/*    public void setViewChecker(FileCheckingInternalResourceViewResolver viewChecker) {
        this.viewChecker = viewChecker;
    }
*/
    public void afterPropertiesSet() throws Exception {
        // if (clinic == null) {
        // throw new ApplicationContextException("Must set clinic bean property
        // on " + getClass());
        // }
    }

    // handlers

    /**
     * Custom handler for gene test
     * 
     * @param request current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render the response
     */
    @SuppressWarnings("unused")
    public ModelAndView simpleQueryHandler(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("featureView");
    }

    /**
     * Custom handler for MOD common URL
     * 
     * @param request current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render the response
     */
    @SuppressWarnings("unused")
    public ModelAndView booleanQueryHandler(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("commonURLView");
    }

    /**
     * Custom handler for examples
     * 
     * @param request current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render the response
     */
    @SuppressWarnings("unused")
    public ModelAndView examplesHandler(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("examplesView");
    }

    public ModelAndView FeatureById(HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {
        int id = ServletRequestUtils.getIntParameter(request, "id", -1);
        if (id == -1) {

        }
        Feature feat = sequenceDao.getFeatureById(id);
        Map<String,Object> model = new HashMap<String,Object>(3);
        model.put("feature", feat);
        String viewName = "features/gene";
        // String type = feat.getCvTerm().getName();
        // TODO
        // Check if features/type is known about
        // otherwise go to features/generic
        viewName = "features/generic";
        return new ModelAndView(viewName, model);
    }

    private static final String NO_VALUE_SUPPLIED = "_NO_VALUE_SUPPLIED";

    public ModelAndView DummyGeneFeature(@SuppressWarnings("unused") HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {
        Feature feat = new Feature();
        feat.setName("dummy_name");
        feat.setUniqueName("dummy_id");
        CvTerm cvTerm = new CvTerm();
        cvTerm.setName("gene");
        feat.setCvTerm(cvTerm);
        Map<String,Object> model = new HashMap<String,Object>(3);
        model.put("feature", feat);
        String viewName = "features/gene";
        return new ModelAndView(viewName, model);
    }

    public ModelAndView FeatureByName(HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {
        String name = ServletRequestUtils.getStringParameter(request, "name", NO_VALUE_SUPPLIED);
        if (name.equals(NO_VALUE_SUPPLIED)) {

        }
        List<Feature> features = sequenceDao.getFeaturesByUniqueName(name);
        if (features.size() == 0)
            throw new RuntimeException(String.format("Could not find feature with unique name '%s'", name));
        else if (features.size() > 1)
            logger.error(String.format("Found more than one feature with uniqueName LIKE '%s'", name));
            
        Feature feat = features.get(0);
        Map<String,Object> model = new HashMap<String,Object>(4);
        model.put("feature", feat);
        String viewName = "features/generic";
        String type = feat.getCvTerm().getName();
        // TODO
        // Check if features/type is known about
        // otherwise go to features/generic
        if (type != null && type.equals("gene")) {
            viewName = "features/gene";
            Feature mRNA = null;
            Collection<FeatureRelationship> frs = feat.getFeatureRelationshipsForObjectId();
            for (FeatureRelationship fr : frs) {
                mRNA = fr.getFeatureBySubjectId();
                break;
            }
            Feature polypeptide = null;
            Collection<FeatureRelationship> frs2 = mRNA.getFeatureRelationshipsForObjectId();
            for (FeatureRelationship fr : frs2) {
                Feature f = fr.getFeatureBySubjectId();
                if ("polypeptide".equals(f.getCvTerm().getName())) {
                    polypeptide = f;
                }
            }
            model.put("transcript", mRNA);
            model.put("polypeptide", polypeptide);
            PeptideProperties pp = calculatePepstats(polypeptide);
            model.put("polyprop", pp);
        }
        return new ModelAndView(viewName, model);
        /*
         * try { PrintWriter out = response.getWriter(); out.write("feature name
         * is " + feat.getUniqueName()); } catch (IOException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); } return null;
         */
    }

    private PeptideProperties calculatePepstats(Feature polypeptide) {

        // String seqString = FeatureUtils.getResidues(polypeptide);
        String seqString = new String(polypeptide.getResidues());
        // System.err.println(seqString);
        Alphabet protein = ProteinTools.getAlphabet();
        SymbolTokenization proteinToke = null;
        SymbolList seq = null;
        PeptideProperties pp = new PeptideProperties();
        try {
            proteinToke = protein.getTokenization("token");
            seq = new SimpleSymbolList(proteinToke, seqString);
        } catch (BioException e) {

        }
        IsoelectricPointCalc ipc = new IsoelectricPointCalc();
        Double cal = 0.0;
        try {
            cal = ipc.getPI(seq, false, false);
        } catch (IllegalAlphabetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BioException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DecimalFormat df = new DecimalFormat("#.##");
        pp.setIsoelectricPoint(df.format(cal));
        pp.setAminoAcids(Integer.toString(seqString.length()));
        MassCalc mc = new MassCalc(SymbolPropertyTable.AVG_MASS, false);
        try {
            cal = mc.getMass(seq) / 1000;
        } catch (IllegalSymbolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pp.setMass(df.format(cal));

        // cal = WebUtils.getCharge(seq);
        pp.setCharge(df.format(cal));
        return pp;
    }

    @SuppressWarnings("unused")
    public ModelAndView GeneOntology(HttpServletRequest request, HttpServletResponse response) {

        // String go = ServletRequestUtils.getStringParameter(request,"go",
        // NO_VALUE_SUPPLIED);
        String viewName = "features/GO";
        Map<String,Object> model = new HashMap<String,Object>(2);

        // List <Feature> feat = sequenceDao.getFeatureByGO(go);
        // model.put("feature", feat);
        return new ModelAndView(viewName, model);
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    @SuppressWarnings("unused")
    public ModelAndView FindCvByName(HttpServletRequest request, HttpServletResponse response) {
        String name = ServletRequestUtils.getStringParameter(request, "name", "%");
        List<Cv> cvs = this.cvDao.getCvByName(name);
        Map<String,Object> model = new HashMap<String,Object>();
        String viewName = "db/listCv";
        if (cvs.size() == 1) {
            viewName = "db/cv";
            model.put("cv", cvs.get(0));
        } else {
            model.put("cvs", cvs);
        }
        return new ModelAndView(viewName, model);
    }

    public ModelAndView Products(@SuppressWarnings("unused") HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>(1);
        String viewName = listProductsView;
        /*
         * HashMap hm = sequenceDao.getProducts();
         * 
         * List<String> products = new ArrayList<String>(); List<String>
         * numbers = new ArrayList<String>();
         * 
         * int count = 0; Set mappings = hm.entrySet(); for (Iterator i =
         * mappings.iterator(); i.hasNext();) { Map.Entry me =
         * (Map.Entry)i.next(); Object product = me.getKey(); Object number =
         * me.getValue(); products.add(count, product.toString());
         * numbers.add(count,number.toString()); count++; }
         * model.put("products", products); model.put("numbers", numbers);
         */
        List<CountedName> products = sequenceDao.getProducts();
        model.put("products", products);
        return new ModelAndView(viewName, model);
    }

    public ModelAndView CvTermByCvName(HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {
        String cvName = ServletRequestUtils
                .getStringParameter(request, "cvName", NO_VALUE_SUPPLIED);
        String cvTermName = ServletRequestUtils.getStringParameter(request, "cvTermName", "*");
        System.err.println("cvName=" + cvName + "   :   cvTermName=" + cvTermName);
        cvTermName = cvTermName.replace('*', '%');
        List<Cv> cvs = cvDao.getCvByName(cvName);
        Cv cv = cvs.get(0);
        System.err.println("cv=" + cv);
        List<CvTerm> cvTerms = cvDao.getCvTermByNameInCv(cvTermName, cv);
        String viewName = "db/listCvTerms";
        Map<String,Object> model = new HashMap<String,Object>();

        if (cvTerms.size() == 1) {
            viewName = "db/cvTerm";
            model.put("cvTerm", cvTerms.get(0));
        } else {
            model.put("cvTerms", cvTerms);
        }
        System.err
                .println("viewName is '" + viewName + "' and cvTerms length is " + cvTerms.size());
        return new ModelAndView(viewName, model);
    }

    public ModelAndView GenesByCvTermAndCvName(HttpServletRequest request,
            HttpServletResponse response) {
        String cvName = ServletRequestUtils
                .getStringParameter(request, "cvName", NO_VALUE_SUPPLIED);
        String cvTermName = ServletRequestUtils.getStringParameter(request, "cvTermName",
            NO_VALUE_SUPPLIED);
        String viewName = "list/features";
        List<Feature> features = sequenceDao.getFeaturesByCvTermNameAndCvName(cvTermName, cvName);

        if (features == null || features.size() == 0) {
            logger.info("result is null");
            try {
                ServletOutputStream out = response.getOutputStream();
                out.print("There is no Gene in the database coresponding to CvTerm " + cvTermName);
                out.close();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        logger.info("Features size is " + features.size());
        Map<String,Object> model = new HashMap<String,Object>();
        List<Feature> results = new ArrayList<Feature>();
        for (Feature feature : features) {
            if ("gene".equals(feature.getCvTerm().getName())) {
                results.add(feature);
            } else {
                Feature mRNA = null;
                Collection<FeatureRelationship> frs = feature.getFeatureRelationshipsForSubjectId();
                if (frs != null) {
                    for (FeatureRelationship fr : frs) {
                        mRNA = fr.getFeatureByObjectId();
                        break;
                    }
                    if (mRNA != null) {
                        Feature gene = null;
                        Collection<FeatureRelationship> frs2 = mRNA
                                .getFeatureRelationshipsForSubjectId();
                        for (FeatureRelationship fr : frs2) {
                            Feature f = fr.getFeatureByObjectId();
                            if ("gene".equals(f.getCvTerm().getName())) {
                                gene = f;
                                results.add(gene);
                            }
                        }
                    }
                }
            }
        }
        logger.info("results size is " + results.size());
        model.put("results", results);

        return new ModelAndView(viewName, model);
    }

    // public ModelAndView PublicationById(HttpServletRequest request,
    // HttpServletResponse response) {
    // int id = ServletRequestUtils.getIntParameter(request, "id", -1);
    // if (id == -1) {
    //		    
    // }
    // Pub pub = pubHome.findById(id);
    // Map model = new HashMap(3);
    // model.put("pub", pub);
    // return new ModelAndView("db/pub", model);
    // }

   // @SuppressWarnings("unchecked")
    public ModelAndView FeatureByCvTermNameAndCvName(HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response) {
        String viewName = null;
        Map<String,Object> model = null;
        String name = ServletRequestUtils.getStringParameter(request, "name", NO_VALUE_SUPPLIED);
        String cvName = ServletRequestUtils
                .getStringParameter(request, "cvName", NO_VALUE_SUPPLIED);
        List<Feature> features = sequenceDao.getFeaturesByCvTermNameAndCvName(name, cvName);
        List<Feature> feats = null;
        String length = null;
        if (features.size() == 1) {
            Feature feat = features.get(0);
            model = new HashMap<String,Object>(3);
            String type = feat.getCvTerm().getName();
            if (type != null && type.equals("gene")) {
                model.put("feature", feat);
                viewName = "features/gene";
                Feature mRNA = null;
                Collection<FeatureRelationship> frs = feat.getFeatureRelationshipsForObjectId();
                for (FeatureRelationship fr : frs) {
                    mRNA = fr.getFeatureBySubjectId();
                    break;
                }
                Feature polypeptide = null;
                Collection<FeatureRelationship> frs2 = mRNA.getFeatureRelationshipsForObjectId();
                for (FeatureRelationship fr : frs2) {
                    Feature f = fr.getFeatureBySubjectId();
                    if ("polypeptide".equals(f.getCvTerm().getName())) {
                        polypeptide = f;
                    }
                }
                model.put("polypeptide", polypeptide);
            } else {
                viewName = "features/gene";
                model.put("polypeptide", feat);
                Feature mRNA = null;
                Collection<FeatureRelationship> frs = feat.getFeatureRelationshipsForSubjectId();
                for (FeatureRelationship fr : frs) {
                    mRNA = fr.getFeatureByObjectId();
                    break;
                }
                Feature gene = null;
                Collection<FeatureRelationship> frs2 = mRNA.getFeatureRelationshipsForSubjectId();
                for (FeatureRelationship fr : frs2) {
                    Feature f = fr.getFeatureByObjectId();
                    if ("gene".equals(f.getCvTerm().getName())) {
                        gene = f;
                    }
                }
                model.put("feature", gene);
            }
        } else {
            boolean polypep = false;
            model = new HashMap<String,Object>(2);
            for (Feature feature : features) {
                if ("polypeptide".equals(feature.getCvTerm().getName())) {
                    polypep = true;
                }
                break;
            }
            if (polypep) {
                feats = new ArrayList<Feature>();
                for (Feature feature : features) {
                    Collection<FeatureRelationship> frs = feature
                            .getFeatureRelationshipsForSubjectId();
                    Feature mRNA = null;
                    for (FeatureRelationship relationship : frs) {
                        mRNA = relationship.getFeatureByObjectId();
                        break;
                    }
                    Collection<FeatureRelationship> frs2 = mRNA
                            .getFeatureRelationshipsForSubjectId();
                    for (FeatureRelationship relationship : frs2) {
                        Feature f = relationship.getFeatureByObjectId();
                        if ("gene".equals(f.getCvTerm().getName())) {
                            feats.add(f);
                        }
                    }
                }
                model.put("features", feats);
                File tmpDir = new File(getServletContext().getRealPath("/GViewer/data"));
                length = GeneDBWebUtils.buildGViewerXMLFiles(feats, tmpDir);
                model.put("length", length);
            } else {
                model.put("features", features);
                File tmpDir = new File(getServletContext().getRealPath("/GViewer/data"));
                length = GeneDBWebUtils.buildGViewerXMLFiles(features, tmpDir);
                model.put("length", length);
            }
            viewName = "list/features1";
        }
        return new ModelAndView(viewName, model);

    }

    public ModelAndView Phylogeny(@SuppressWarnings("unused") HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {
        Phylonode root = this.phylogenyDao.getPhylonodeByName("Root").get(0);
        List<Phylonode> topLevel = this.phylogenyDao.getPhylonodesByParent(root);
        StringBuffer sb = new StringBuffer();
        StringBuffer top = new StringBuffer();
        int j = 1;
        for (Phylonode phylonode : topLevel) {
            CompositeMenu menu = new CompositeMenu(Integer.toString(Menu.counter++), phylonode
                    .getLabel(), null, true);
            top.append(Menu.counter - 1);
            top.append(",");
            menu.setLevelCoord(Integer.toString(j));
            j++;
            buildMenu(phylonode, menu);
            sb.append(menu.render(j - 1));
        }
        top.deleteCharAt(top.length() - 1);
        // System.out.println(sb.toString());
        sb.append("<input type=\"hidden\" id=\"itemsLength\" value=\"" + Menu.counter + "\"/>");
        sb.append("<input type=\"hidden\" id=\"topItems\" value=\"" + top.toString() + "\"/>");
        Map<String,String> model = new HashMap<String,String>(1);
        model.put("nodes", sb.toString());
        Menu.counter = 0;
        return new ModelAndView("features/phylogeny", model);
    }

    private void buildMenu(Phylonode phylonode, CompositeMenu comSrc) {
        List<Phylonode> childs = this.phylogenyDao.getPhylonodesByParent(phylonode);
        for (Phylonode child : childs) {
            if (isLeaf(child)) {
                SimpleMenu sm = new SimpleMenu(Integer.toString(Menu.counter++), child.getLabel(),
                        null);
                comSrc.add(sm);
            } else {
                CompositeMenu parentMenu = new CompositeMenu(Integer.toString(Menu.counter++),
                        child.getLabel(), null, false);
                comSrc.add(parentMenu);
                buildMenu(child, parentMenu);
            }
        }

    }

    private boolean isLeaf(Phylonode child) {
        List<Phylonode> childs = this.phylogenyDao.getPhylonodesByParent(child);
        if (childs == null) {
            return true;
        }
        return false;
    }

    public ModelAndView Menu(@SuppressWarnings("unused") HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {
        return new ModelAndView("features/menu", null);
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    // public void setPubHome(PubHome pubHome) {
    // this.pubHome = pubHome;
    // }

}