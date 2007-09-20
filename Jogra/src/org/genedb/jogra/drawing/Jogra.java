package org.genedb.jogra.drawing;

import org.genedb.zfexpression.domain.Stage;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventServiceEvent;
import org.bushe.swing.event.EventSubscriber;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

public class Jogra implements PropertyChangeListener, EventSubscriber {
    
    private List<JograPlugin> pluginList = new ArrayList<JograPlugin>();
    
    private List<JFrame> windowList = new ArrayList<JFrame>();
    
    private JFrame frame = new JFrame();
    private AbstractApplicationContext ctx;
    //private boolean audio = true;
    private JButton edit;
    private JButton album;
    private String secondStep;
    private String defaultDir = "/Users/art/Desktop";
    
    private JMenu windowMenu;
    
//    public void setDirty(boolean dirty) {
//        if (dirty == this.dirty) {
//            return;
//        }
//        this.dirty = dirty;
//        updateTitle();
//    }


    public void setSecondStep(String secondStep) {
        this.secondStep = secondStep;
    }

    
    public void init() throws Exception {
        JograLogin loginWindow = new JograLogin();
        //loginWindow.setUser(user);
        loginWindow.pack();
        loginWindow.setVisible(true);
    }
    
    public static void main(String[] args) throws Exception {
        
        Jogra application  = new Jogra();
        
        application.testTransactions();
        
        application.init();
        //application.logon();
        application.makeMain();
        application.showMain();
        
        //ps.showSplash();

//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                try {
//                    ps.makeMain();
//                    ps.showMain();
//                } catch (IOException exp) {
//                    // TODO Auto-generated catch block
//                    exp.printStackTrace();
//                }
//            }
//        });
    }

    private void testTransactions() {
        // TODO Auto-generated method stub
        //Feature f = fetchFeature("idXXX");
        
    }


    public Jogra() throws IOException {
        
        ctx = new ClassPathXmlApplicationContext(
                new String[] {"classpath:applicationContext.xml"});
        ctx.registerShutdownHook();
        
        EventBus.subscribe(ApplicationClosingEvent.class, new EventSubscriber() {
            public void onEvent(EventServiceEvent ese) {
                shutdown();
            }
        });
        
        //EventBus.subscribe(ApplicationClosingEvent.class, new VetoEventListener {});
        pluginList.add(new GeneEditor());
        pluginList.add(new GeneList());
        
        EventBus.subscribe(OpenWindowEvent.class, this);
        
    }
    
    
    private void shutdown() {
        System.err.println("Shutdown called");
        int check = JOptionPane.showConfirmDialog(frame,
                "Really finish this experiment",
                "Caution",
                JOptionPane.YES_NO_OPTION);
        if (check == JOptionPane.NO_OPTION) {
            return;
        }
        System.exit(0);
    }
    
    
    public void makeMain() throws IOException {
        
        frame.setResizable(false);
        //slide.setDirty(false);
        //updateTitle(); // To make sure title bar is updated even if dirty hasn't changed
        
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                System.err.println("About to publish ace");
                EventBus.publish(new ApplicationClosingEvent());
                System.err.println("Just published ace");
                //System.exit(0);
            }
        });
        
        JMenuBar menu = new JMenuBar();
        
        JMenu pluginMenu = new JMenu("Plugins");
        for (JograPlugin plugin : pluginList) {
            pluginMenu.add(new JMenuItem(plugin.getName()));
        }
        menu.add(pluginMenu);
        
        windowMenu = new JMenu("Window");
        menu.add(windowMenu);
        
        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(new JMenuItem("About"));
        menu.add(helpMenu);
        
        frame.setJMenuBar(menu);
        
        Container pane = frame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        //pane.setOpaque(true);
        
        for (JograPlugin plugin : pluginList) {
            if (plugin.getMainWindowPlugin() != null) {
                pane.add(plugin.getMainWindowPlugin());
            }
        }
        
    }

   protected void finishUp() {
        // TODO Auto-generated method stub
    }

   public void showMain() throws IOException {
        frame.pack();
        frame.setVisible(true);
    }

    
    private void restart() {
        // TODO Auto-generated method stub
    }    

    public void propertyChange(PropertyChangeEvent evt) {
        //updateTitle();
    }

    public void onEvent(EventServiceEvent event) {
        if (event.getClass().isAssignableFrom(OpenWindowEvent.class)) {
            this.onEvent((OpenWindowEvent) event);
            return;
        }
        System.err.println("Generic event received");
    }

    public void onEvent(final OpenWindowEvent event) {
        System.err.println("Open event received");
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // TODO Auto-generated method stub
                JFrame frame = new JFrame();
                frame.add(event.getPanel());
                frame.pack();
                windowList.add(frame);
                frame.setVisible(true);
            }
        });
    }
}

