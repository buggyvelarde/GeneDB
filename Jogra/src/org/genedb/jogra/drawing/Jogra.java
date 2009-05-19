/*
 * JOGRA is an overarching application that has several plugins 'attached' to it. 
 * Jogra initialises the application context, and obtains and publishes the username and password to other plugins that may need it. 
 * 14.5.2009
 */


package org.genedb.jogra.drawing;



import org.genedb.jogra.domain.GeneDBMessage;
import org.genedb.jogra.services.DatabaseLogin;
import org.genedb.jogra.services.MessageService;

import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.bushe.swing.event.EventTopicSubscriber;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;

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

public class Jogra implements SingleInstanceListener, PropertyChangeListener, EventSubscriber<GeneDBMessage> {

    private static int TIMER_DELAY = 10*1000;
    private static final Logger logger = Logger.getLogger(Jogra.class);
    private Map<String, JograPlugin> pluginMap;
    private SingleInstanceService sis;
    private final JFrame mainFrame = new JFrame();
    private JMenu windowMenu;
    private JograBusiness jograBusiness;
    private Timer timer = new Timer();
    private MessageService messageService;
    private List<String> chosenOrganism; /* Used by other plugins as a way of storing the user's organism selection. Will be changed to Eventbus.publish etc. */
    private static String username;
    private static String password;

   
    public Jogra() {
        EventBus.subscribe(ApplicationClosingEvent.class, new EventSubscriber<GeneDBMessage>() {
            public void onEvent(final GeneDBMessage ese) {
                shutdown();
            }
        });

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (final Exception exp) {
            exp.printStackTrace();
        }

        // EventBus.subscribe(ApplicationClosingEvent.class, new
        // VetoEventListener {});

        EventBus.subscribe(OpenWindowEvent.class, this);

//        TimerTask fetchMessage = new TimerTask() {
//        	@Override
//        	public void run() {
//
//        		String clientName = "dummy"; // FIXME
//				Collection<Message> messages = messageService.checkMessages(clientName);
//				if (messages != null) {
//					for (Message message : messages) {
//						EventBus.publish(message);
//					}
//				}
//        	}
//        };
//        timer.scheduleAtFixedRate(fetchMessage, TIMER_DELAY, TIMER_DELAY);
        
        EventBus.subscribeStrongly("selection", new EventTopicSubscriber<List<String>>() {
            public void onEvent(String topic, List<String> selection) {
              System.out.println("Selection is " + selection.toString() );
//                System.out.println("Topic is " + topic );
                setChosenOrganism(selection);
                System.out.println("JOGRA: Picked up " + selection.toString());
            }

            
        }); 

    }

    protected void finishUp() {
        // TODO Auto-generated method stub
    }

    public void init() throws Exception {

      /*  LoginService ls = new JograLoginService();
        JXLoginPane loginPane = new JXLoginPane(ls);
       loginPane.setBannerText("Jogra Login");
       final BufferedImage bi = ImageUtils.makeBackgroundFromClasspath("jogra.jpg");
       loginPane.setBanner(bi);
       Status status = JXLoginPane.showLoginDialog(null, loginPane);
       if (status != Status.SUCCEEDED) {
       	finalShutdown();
        } 
        */

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
        // updateTitle(); // To make sure title bar is updated even if dirty
        // hasn't changed

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent arg0) {
                System.err.println("About to publish ace");
                EventBus.publish(new ApplicationClosingEvent());
                System.err.println("Just published ace");
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
        // pane.setOpaque(true);

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
    public static Jogra instantiate() throws IOException {     
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        Properties properties = new Properties();
        properties.setProperty("Jogra.username", getUsername());
        properties.setProperty("Jogra.password", getPassword());
        configurer.setProperties(properties);

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
        context.addBeanFactoryPostProcessor(configurer);

        context.setConfigLocation("classpath:/applicationContext.xml" );
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

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
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
    
    public List<String> getChosenOrganism() {
        return chosenOrganism;
    }

    public void setChosenOrganism(List<String> user_selection) {
        this.chosenOrganism = user_selection;
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
       /* final DatabaseLogin dblogin = new DatabaseLogin("jdbc:postgresql://localhost:5432/nds");
        
        try{
            new SwingWorker<String, String>() {
         
                protected String doInBackground() throws Exception {
                  
                  System.out.println("Inside doInBackground method");
                  dblogin.validateUser();
                
                  return new String("");
                }
                
         
                protected void done(){
                    System.out.println("Inside done method");
                    try{
                    Jogra application = new Jogra();
                    if(dblogin.isValid()){
                        application.setUsername(dblogin.getUsername());
                        application.setPassword(dblogin.getPassword());
                        application = Jogra.instantiate();
                        application.testTransactions();
                        application.init();
                        application.makeMain();
                        application.showMain();
                        if (args.length > 0) {
                            application.newActivation(args);
                        }
                    }
                    }catch(Exception e){
                        //Handle exceptions better
                        logger.debug(e);
                    }
                }
                
            }.execute();     
        }catch(Exception e){ //handle exceptions better later
            System.out.println(e);
        } */
        
        
        
        
       DatabaseLogin dblogin = new DatabaseLogin("jdbc:postgresql://localhost:5432/nds");
        boolean validUser = dblogin.validateUser();
        Jogra application = new Jogra();
        if(validUser){
            application.setUsername(dblogin.getUsername());
            application.setPassword(dblogin.getPassword());
            application = Jogra.instantiate();
            application.testTransactions();
            application.init();
            application.makeMain();
            application.showMain();
            if (args.length > 0) {
                application.newActivation(args);
            }
        } 
       
    }

}
