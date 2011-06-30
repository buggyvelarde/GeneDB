//package org.genedb.web.gui;
//
//import org.genedb.web.mvc.model.BerkeleyMapFactory;
//
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Required;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//
//import java.awt.Font;
//import java.util.Map;
//
//public class RenderedDiagramFactory implements ApplicationContextAware {
//
//    private Font labelFont;
//    private BerkeleyMapFactory bmf;
//    private static RenderedDiagramFactory instance;
//
//    public RenderedDiagram getRenderedDiagram(TrackedDiagram diagram) {
//        RenderedDiagram ret = null;
//        if (diagram instanceof ContextMapDiagram) {
//            ret = new RenderedContextMap((ContextMapDiagram)diagram);
//            ret.setLabelFont(labelFont);
//            ret.setBerkelyMapFactory(bmf);
//            return ret;
//        }
//
//        if (diagram instanceof ProteinMapDiagram) {
//            ret = new RenderedProteinMap((ProteinMapDiagram)diagram);
//            ret.setLabelFont(labelFont);
//            ret.setBerkelyMapFactory(bmf);
//            return ret;
//        }
//
//        throw new RuntimeException("Unable to find a renderer for diagram '"+diagram+"'");
//    }
//
//    public static RenderedDiagramFactory getInstance() {
//        return instance;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public void setApplicationContext(ApplicationContext appContext)
//            throws BeansException {
//        Map map = appContext.getBeansOfType(RenderedDiagramFactory.class);
//        if (map.isEmpty()) {
//            throw new RuntimeException("Unable to set singleton instance of RenderedDiagramFactory - see Javadocs");
//        }
//        RenderedDiagramFactory.instance = (RenderedDiagramFactory) map.values().iterator().next();
//    }
//
//    @Required
//    public void setLabelFont(Font labelFont) {
//        this.labelFont = labelFont;
//    }
//
//    public void setBerkeleyMapFactory(BerkeleyMapFactory bmf) {
//        this.bmf = bmf;
//    }
//
//
//
//}
