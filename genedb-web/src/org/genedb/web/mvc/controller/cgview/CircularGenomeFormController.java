package org.genedb.web.mvc.controller.cgview;


import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.multipart.support.StringMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import uk.ac.sanger.artemis.circular.DNADraw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * @author Adrian Tivey
 */
public class CircularGenomeFormController extends SimpleFormController implements InitializingBean{
    
    private List<String> digestNames;
    
    private EmbossDigestParser embossDigestParser;
    
    private CachedFileFactory cachedFileFactory;

    private Map<String, String> orgs = new HashMap<String, String>();
    
    private String embossDirectoryName;
    
    private String diagramView;
    
    private String fileName = null;
    
    private String organism = null;
    
    public String getDiagramView() {
        return diagramView;
    }


    public void setDiagramView(String diagramView) {
        this.diagramView = diagramView;
    }


    public CircularGenomeFormController() {
        String ROOT = "/nfs/team81/art/circ_genome_data/";
        orgs.put("S. typhi", ROOT+"styphi/chr1/St.embl");
        orgs.put("S. pyogenes", ROOT+"spyogenes/curated/SP.art");
        orgs.put("C. jejuni", ROOT+"cjejuni/curated/chr1/Cj.embl");
    }


    /**
     * Custom handler for homepage
     * @param request current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render the response
     */
    @SuppressWarnings("unchecked")
    @Override
    public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, 
            Object command, BindException errors) throws Exception{
        
        CircularGenomeCommandBean cgcb = (CircularGenomeCommandBean) command;
        
        CgviewFromGeneDBFactory factory = new CgviewFromGeneDBFactory();
        
        Map<String,Object> model = new HashMap<String,Object>(4);

        try {
            if (!cgcb.getTaxon().equals("User uploaded file")) {
                organism = cgcb.getTaxon();
                fileName = orgs.get(organism);
            } else {
                String file = cgcb.getFile();
                if (file == null) {
                    // hmm, that's strange, the user did not upload anything
                    // otherwise save to a temp file
                    errors.reject("no.file.upload");
                    logger.error("No file was uploaded");
                    return showForm(request,response,errors);
                } else {
                    System.err.println("Received an upload file");
                    Writer fw = null;
                    BufferedReader r = null;
                    try {
                        File outFile = File.createTempFile("cg_input", ".embl");
                        // Save to a temp file
                        fw = new FileWriter(outFile);
                        r = new BufferedReader(new StringReader(file));
                        String line;
                        while ((line = r.readLine()) != null) {
                            fw.write(line);
                            fw.write('\n');
                        }
                        fileName = outFile.getCanonicalPath();
                    }
                    finally {
                        if (fw != null) {
                            fw.close();
                        }
                        if (r != null) {
                            r.close();
                        }
                    }
                }
            }
            
            File output = File.createTempFile("circular_genome", ".txt");
            String[] args = {embossDirectoryName+"/bin/restrict", fileName, "-auto", "-limit", "y", "-enzymes", cgcb.getEnzymeName(), "-out", output.getCanonicalPath() };
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            System.err.print("**");
            try {
                InputStream is = p.getInputStream();
                int inchar;
                while ((inchar = is.read())!=-1) {
                    char c = (char) inchar;
                    System.err.print(c);
                }
                System.err.println("**");
                p.waitFor();
                System.err.println("Process exited with '"+p.exitValue()+"'");
            } catch (InterruptedException exp) {
                errors.reject("emboss.error");
                logger.error(exp);
                return showForm(request, response, errors);
            }
            p.waitFor();
            ReportDetails rd = factory.findCutSitesFromEmbossReport(output.getCanonicalPath());
            //ReportDetails rd = factory.findCutSitesFromEmbossReport("/nfs/team81/cp2/circular_genome6317.txt");
            if (rd.cutSites.size() == 0) {
                errors.reject("emboss.no.results");
                return showForm(request, response, errors);
            }
            DNADraw dna = factory.createDnaFromReportDetails(rd);
            
            //Map<String,Location> miscFeatures = factory.parseEmblForMiscFeatures(addFileName);
            CachedFile pngFile = cachedFileFactory.getCachedFile("organism-enzyme-"+cgcb.getEnzymeName()+".PNG");

            RenderedImage bi = writeCgviewToImage(dna, false);
            String table = makeTable(dna.getFeaturePoints());
            ImageIO.write(bi, "PNG", pngFile.getFile());
            
            
            String browserPath = pngFile.getBrowserPath(request);
            String circular = ("<img style=\"border:0\" src=\"" + StringEscapeUtils.escapeHtml(browserPath) + "\" width=\"" + Integer.toString(600) + "\" height=\"" + Integer.toString(600) + "\" />" + '\n');
            
            VirtualDigest vDigest = new VirtualDigest();
            vDigest.setLength(rd.length);
            vDigest.setCutSites(rd.cutSites);
            BufferedImage bi2 = vDigest.draw();
            String array = makeArray(vDigest.getCoords());
            CachedFile pngFile2 = cachedFileFactory.getCachedFile("organism-"+cgcb.getTaxon()+":"+"enzyme-"+cgcb.getEnzymeName()+"-gel-image");
            ImageIO.write(bi2, "PNG", pngFile2.getFile());
            
            String gel = ("<img id=\"gel\" style=\"border:0\" src=\"" + StringEscapeUtils.escapeHtml(pngFile2.getBrowserPath(request)) + "\" width=\"" + Integer.toString(100) + "\" height=\"" + Integer.toString(600) + "\" />" + '\n');
            
            model.put("circular", circular);
            model.put("gel", gel);
            model.put("array", array);
            model.put("table", table);
            return new ModelAndView(diagramView, model);
        } catch (IOException exp) {
            errors.reject("emboss.error");
            logger.error(exp);
            return showForm(request, response, errors);
        }
        //return null;
    }


    private String makeTable(Hashtable<String,Point[]> points) {
        StringBuilder table = new StringBuilder();
        String[] keys = new String[points.size()];
        int width = 600;
        Iterator<String> iter = points.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            String[] values = key.split(";");
            int count = Integer.parseInt(values[0]);
            keys[count-1] = key;
        }
        table.append("<table clas=\"simple\">");
        table.append("<tr>");
        table.append("<th>Feature</th>");
        table.append("<th>Links</th>");
        table.append("</tr>");
        for(int i=0;i<keys.length;i++) {
            table.append("<tr>");
            Point[] point = points.get(keys[i]);
            Point start = point[0];
            Point end = point[2];
            
            int x1 = (int) start.getX();
            int y1 = (int) start.getY();
            
            int x3 = (int) end.getX();
            int y3 = (int) end.getY();

            if(x3 + 20 > width) {
                x3 = width - 25;
            } else if (x3 - 20 < 0) {
                x3 = 25;
            }
            
            if(y3 + 10 > width) {
                y3 = width - 15;
            } else if (y3 - 20 < 0) {
                y3 = 15;
            }
            String feature = keys[i].split(";")[0];
            String coord = keys[i].split(";")[1];
            String[] values = coord.split("\\.\\.");
            String orgOrFileLink = String.format("taxon=User uploaded file&file=%s", fileName);
            if (fileName == null) {
                orgOrFileLink = String.format("organism=%s",organism);
            }
            String hrefArtemis = String.format("href=\"FlatFileReport?outputFormat=Artemis&%s&min=%s&max=%s\"",orgOrFileLink,values[0],values[1]);
            String hrefTable = String.format("href=\"FlatFileReport?outputFormat=Table&%s&min=%s&max=%s\"",orgOrFileLink,values[0],values[1]);
           
            String coords = String.format("%d,%d,%d,%d,%s", x1,x3,y1,y3,feature);
            table.append("<td>");
            table.append(String.format("<a href=\"#\" onmouseover= \"highlight(%s)\" onmouseout= \"highlightOff()\"> %s </a>",coords,feature));
            table.append("</td>");
            table.append("<td>");
            table.append(String.format("<a %s> Annotation </a> | <a %s> Artemis </a>",hrefTable,hrefArtemis));
            table.append("</td>");
            table.append("</tr>");
        }
        table.append("</table>");
        return table.toString();
    }

    private String makeArray(Map<String, List<String>> coords) {
        Iterator<String> iterator = coords.keySet().iterator();
        StringBuilder ret = new StringBuilder(); 
        ret.append("<script type=\"text/javascript\">");
        ret.append(String.format("var coords = new Array(%d);",coords.size()));
        while (iterator.hasNext()) {
           String id = iterator.next();
           List<String> coord = coords.get(id);
           String topY = coord.get(0);
           String bottomX = coord.get(1);
           String bottomY = coord.get(2);
           int count = Integer.parseInt(id) - 1;
           ret.append(String.format("coords[%d] = new Array(3);",count));
           ret.append(String.format("coords[%d][0] = %s;",count,topY));
           ret.append(String.format("coords[%d][1] = %s;",count,bottomX));
           ret.append(String.format("coords[%d][2] = %s;",count,bottomY));
        }  
        ret.append("</script>");
        
        
        return ret.toString();
    }
    
    public static BufferedImage writeCgviewToImage(DNADraw dna, boolean keepLastLabels) {

        int height = (int)dna.getHeight();
        int width = (int)dna.getWidth();
        dna.setOpaque(true);
        BufferedImage buffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = buffImage.createGraphics();
        graphics2D.setColor(Color.white);
        graphics2D.fillRect(0, 0, width,height);
        try {
                
                dna.drawAll(graphics2D, true);
        } finally {
            graphics2D.dispose();
        }
        return buffImage;
    }

    @Override
    protected Map<String,Collection<String>> referenceData(HttpServletRequest req) throws Exception {
        Map<String, Collection<String>> ret = new HashMap<String, Collection<String>>();
        Map<String, String> orgMappings = new LinkedHashMap<String, String>(orgs.size());
        orgMappings.put("User uploaded file", "user-file");
        orgMappings.putAll(orgs);
        ret.put("digestNames", digestNames);
        ret.put("organisms", orgMappings.keySet());
        return ret;
    }

    public void setCachedFileFactory(CachedFileFactory cachedFileFactory) {
        this.cachedFileFactory = cachedFileFactory;
    }


    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(String.class, new StringMultipartFileEditor());
    }


    public String getEmbossDirectoryName() {
        return embossDirectoryName;
    }


    public void setEmbossDirectoryName(String embossDirectoryName) {
        this.embossDirectoryName = embossDirectoryName;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        digestNames = embossDigestParser.getDigests();
    }


    public EmbossDigestParser getEmbossDigestParser() {
        return embossDigestParser;
    }


    public void setEmbossDigestParser(EmbossDigestParser embossDigestParser) {
        this.embossDigestParser = embossDigestParser;
    }

};