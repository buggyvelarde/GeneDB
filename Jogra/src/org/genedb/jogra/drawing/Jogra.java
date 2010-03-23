/*
 * Copyright (c) 2009 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB. If not, write to the Free
 * Software Foundation Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307
 * USA
 */


package org.genedb.jogra.drawing;

import org.genedb.jogra.domain.GeneDBMessage;
import org.genedb.jogra.drawing.JograProgressBar.Position;
import org.genedb.jogra.services.DatabaseLogin;
import org.genedb.jogra.services.MessageService;
import org.genedb.jogra.services.DatabaseLogin.AbortException;

import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.bushe.swing.event.EventTopicSubscriber;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.util.StringUtils;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

/*
 * JOGRA is an overarching application that has several plugins 'attached' to it. Jogra initialises the application context, 
 * and obtains and publishes the username and password to other plugins that may need it.
 * 14.5.2009
 */

public class Jogra implements SingleInstanceListener, PropertyChangeListener, EventSubscriber<GeneDBMessage> {

    public static int TIMER_DELAY = 10*1000;
    private static final Logger logger = Logger.getLogger(Jogra.class);
    private Map<String, JograPlugin> pluginMap;
    private SingleInstanceService sis;
    private final JFrame mainFrame = new JFrame();
    private JMenu windowMenu;
    private JograBusiness jograBusiness;
    private List<String> selectedOrganismNames; /* Picked up via Organism-Tree  */
    private static String username;
    private static String password;
 
    public Jogra() {
        EventBus.subscribe(ApplicationClosingEvent.class, new EventSubscriber<GeneDBMessage>() {
            public void onEvent(final GeneDBMessage ese) {
                shutdown();
            }
        });

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"); //Not available on macs for now
           // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception exp) {
            logger.debug("Unable to set Nimbus L&F", exp);
        }

        // EventBus.subscribe(ApplicationClosingEvent.class, new
        // VetoEventListener {});

        EventBus.subscribe(OpenWindowEvent.class, this);

        EventBus.subscribeStrongly("selection", new EventTopicSubscriber<List<String>>() {
            public void onEvent(String topic, List<String> selection) { 
                logger.info("What is Jogra getting? " + StringUtils.collectionToCommaDelimitedString(selection));
                setSelectedOrganismNames(selection);
                logger.info("JOGRA: Picked up " + selection.toString());
            }


        });

    }

    protected void finishUp() {
        // TODO Auto-generated method stub
    }

    public void init() throws Exception {

        try {
            sis = (SingleInstanceService) ServiceManager.lookup("javax.jnlp.SingleInstanceService");
            sis.addSingleInstanceListener(this);
        }
        catch (UnavailableServiceException e) {
            sis=null; // Not running under JNLP
        }
    }

    /* Construct main frame with a list of possible plugins */
    public void makeMain() {

        mainFrame.setResizable(false);
        // slide.setDirty(false);
        //updateTitle(); // To make sure title bar is updated even if dirty
        // hasn't changed

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent arg0) {
                System.err.println("About to publish application closing event");
                EventBus.publish(new ApplicationClosingEvent());
                System.err.println("Just published application closing event");
                System.exit(0);  // FIXME - should we be catching the above event?
            }
        });
        mainFrame.setTitle("Jogra");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        final JMenuBar menu = new JMenuBar();

        final JMenu pluginMenu = new JMenu("Plugins");
        for (final String plugin : pluginMap.keySet()) {
            pluginMenu.add(new JMenuItem(plugin));
        }
        menu.add(pluginMenu);

        windowMenu = new JMenu("Window");
        menu.add(windowMenu);

        final JMenu helpMenu = new JMenu("Help");
        helpMenu.add(new JMenuItem("About"));
        menu.add(helpMenu);

        mainFrame.setJMenuBar(menu);

        final Container pane = mainFrame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        final Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2),
                BorderFactory.createEtchedBorder());

        for (final JograPlugin plugin : pluginMap.values()) {
            logger.warn("Trying to get main window plugin for '"+plugin.getClass()+"'");
            if (plugin.getMainWindowPlugin() != null) {
                final JComponent panel = plugin.getMainWindowPlugin();
                panel.setBorder(border);
                pane.add(panel);
            }
        }

    }

    public void onEvent(final GeneDBMessage event) {
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
                final JFrame frame = event.getJFrame();
                if (frame==null) {
                    return; // Assume plugin will report why
                }
                if (!frame.isVisible()) {
                    frame.setVisible(true);
                } else {
                    // frame.pack();
                    // windowList.add(frame);
                    frame.toFront();
                }
            }
        });
    }



    private void restart() {
        // TODO Auto-generated method stub
    }

    public void setJograBusiness(final JograBusiness jograBusiness) {
        this.jograBusiness = jograBusiness;
    }

    public void showMain() {
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private void shutdown() {
        System.err.println("Shutdown called");
        final int check = JOptionPane.showConfirmDialog(mainFrame, "Really finish this experiment", "Caution",
                JOptionPane.YES_NO_OPTION);
        if (check == JOptionPane.NO_OPTION) {
            return;
        }
       finalShutdown(); 
    }

    private void finalShutdown() {
        if (sis != null) {
            sis.removeSingleInstanceListener(this);
        }
        System.exit(0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.genedb.jogra.drawing.JograApplication#testTransactions()
     */
    public void testTransactions() {
        // TODO Auto-generated method stub
        // Feature f = fetchFeature("idXXX");
        jograBusiness.testTransactions();
        // testService.doSomething2();
    }

    public static JFrame findNamedWindow(final String name) {
        final Frame[] frames = Frame.getFrames();
        for (final Frame frame : frames) {
            if (!(frame instanceof JFrame)) {
                continue;
            }
            final String title = frame.getTitle();
            System.err.println("Comparing '" + name + "' and '" + title + "'");
            // Possible match
            if (title.equals(name)) {
                return (JFrame) frame;
            }
            if (title.equals(name + " *")) {
                return (JFrame) frame;
            }
        }
        return null;
    }



    /* Instantiating the application/Jogra bean */
    public static Jogra instantiate(String userName, char[] password, String url) throws IOException {
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        Properties properties = new Properties();
        properties.setProperty("Jogra.username", userName);
        properties.setProperty("Jogra.password", new String(password));
        properties.setProperty("Jogra.url", url);
        configurer.setProperties(properties);
        
        /** Check if the org....* files exist in the root directory and if they do, delete them **/
        // Store lucene indices somewhere harmless as they are not needed, but are created automatically
        //System.setProperty("hibernate.search.default.indexBase", System.getProperty("java.io.tmpdir"));

        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext();
        context.addBeanFactoryPostProcessor(configurer);
        

        context.setConfigLocation("file:/Users/nds/Documents/workspace/Jogra/conf/applicationContext.xml" );
        context.refresh();

        final Jogra application = (Jogra)context.getBean("application", Jogra.class);
        context.registerShutdownHook();
        return application;

    }

    // public void setTestService(TestService testService) {
    // this.testService = testService;
    // }


    public void setPluginList(List<JograPlugin> pluginList) {
        this.pluginMap = new LinkedHashMap<String, JograPlugin>(pluginList.size());
        for (JograPlugin plugin : pluginList) {
            logger.error("Registering plugin '"+plugin+"' under name '"+plugin.getName()+"' in map");
            pluginMap.put(plugin.getName(), plugin);
        }
    }



    public void newActivation(String[] args) {
        if (args.length==0) {
            mainFrame.toFront();
            return;
        }
        String target = args[0];
        if (!pluginMap.containsKey(target)) {
            logger.error("Unable to find a plugin to handle command '"+target+"'");
            return;
        }
        JograPlugin jp = pluginMap.get(target);
        List<String> newArgs = new ArrayList<String>();
        for (int i = 1; i < args.length; i++) {
            newArgs.add(args[i]);
        }
        jp.process(newArgs);
    }
  
    public List<String> getSelectedOrganismNames() {
        return selectedOrganismNames;
    }

    public void setSelectedOrganismNames(List<String> user_selection) {
       /* if(this.selectedOrganismNames!=null && this.selectedOrganismNames.size()>0){
            this.selectedOrganismNames.clear();
        } */
        this.selectedOrganismNames = user_selection;
    }
    
    

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        // TODO Auto-generated method stub

    }

    /* Getter and setter methods for username and password. */

    public static String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /* Main method that calls the DBLogin application and, if valid user, calls methods to display main Jogra frame */
    public static void main(final String[] args) throws Exception {
   
        DatabaseLogin dblogin = new DatabaseLogin();
//        
//        dblogin.addInstance("Pathogens", "jdbc:postgresql://pgsrv1.internal.sanger.ac.uk:5432/pathogens");
//        dblogin.addInstance("Pathogens-test (Pathdev)", "jdbc:postgresql://pgsrv2.internal.sanger.ac.uk:5432/pathdev");
//        dblogin.addInstance("Port forwarding pathogens (localhost:5432)", "jdbc:postgresql://localhost:5432/pathogens");
//        dblogin.addInstance("Port forwarding pathogens (localhost:54321)", "jdbc:postgresql://localhost:54321/pathogens"); 
//        dblogin.addInstance("Nishadi test database", "jdbc:postgresql://localhost:54321/nds"); 
        
        try {
            dblogin.validateUser();
        } catch (SQLException exp) {
            exp.printStackTrace();
            System.exit(65);
        } catch (AbortException exp) {
            System.exit(65);
        }
        JograProgressBar jpb = new JograProgressBar("Loading Jogra...", Position.CENTRE); //Progress bar added for better user information
        Jogra application = Jogra.instantiate(dblogin.getUsername(), dblogin.getPassword(), dblogin.getDBUrl());
        application.testTransactions();
        application.init();
        application.makeMain();
        jpb.stop();
        application.showMain();
        if (args.length > 0) {
            application.newActivation(args);
        }

    }

}
