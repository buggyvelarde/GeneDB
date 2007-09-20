package org.genedb.jogra.drawing;

import org.genedb.jogra.domain.ExpressionZone;
import org.genedb.jogra.domain.ExpressionZoneDao;

import org.springframework.beans.factory.InitializingBean;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ExpressionZoneSelector implements InitializingBean {

    ButtonGroup expressionZoneGroup = new ButtonGroup();
    JComponent container = new JPanel(new BorderLayout());
    ExpressionZone expressionZone;
    ExpressionZoneDao expressionZoneDao;
    
    public void afterPropertiesSet() {
        Collection<ExpressionZone> zones = expressionZoneDao.retrieveAllAnatomy();
        ButtonModel model = null;
        ActionListener selectionChange = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String name = expressionZoneGroup.getSelection().getActionCommand();
                setAnatomy(expressionZoneDao.retrieveAnatomyByName(name));
            }
        };
        
        Box box1 = Box.createHorizontalBox();
        Box box2 = Box.createHorizontalBox();
        Box box = box1;
        int count = 0;
        for (ExpressionZone zone : zones) {
            JRadioButton jb = new JRadioButton(zone.getLabel());
            jb.setActionCommand(zone.getLabel());
            jb.addActionListener(selectionChange);
            if (model == null) {
                model = jb.getModel();
                expressionZone = zone;
            }
            expressionZoneGroup.add(jb);
            box.add(jb);
            count++;
            if (count == 6) {
                box = box2;
            }
        }
        expressionZoneGroup.setSelected(model, true);
        container.add("North", box1);
        container.add("South", box2);
    }

    public void setAnatomyDao(ExpressionZoneDao expressionZoneDao) {
        this.expressionZoneDao = expressionZoneDao;
    }

    public JComponent getJComponent() {
        return this.container;
    }

    public void setAnatomy(ExpressionZone expressionZone) {
        this.expressionZone = expressionZone;
    }

    public ExpressionZone getAnatomy() {
        return this.expressionZone;
    }
    
}

    
