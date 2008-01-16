package org.genedb.jogra.drawing;

import org.genedb.db.domain.misc.GeneDBMessage;
import org.genedb.db.domain.misc.Message;
import org.genedb.db.domain.services.MessageService;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.springframework.context.support.AbstractApplicationContext;
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
import java.util.Deque;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class Jogra implements PropertyChangeListener, EventSubscriber<GeneDBMessage> {
	
	private static int TIMER_DELAY = 10*1000;

    private final List<JograPlugin> pluginList = new ArrayList<JograPlugin>();

    // private List<JFrame> windowList = new ArrayList<JFrame>();

    private final JFrame mainFrame = new JFrame();

    private AbstractApplicationContext ctx;

    private JMenu windowMenu;

    private JograBusiness jograBusiness;

    private Timer timer = new Timer();
    
    private MessageService messageService;
    
    // private TestService testService;

    // public void setDirty(boolean dirty) {
    // if (dirty == this.dirty) {
    // return;
    // }
    // this.dirty = dirty;
    // updateTitle();
    // }

    public Jogra() throws IOException {

        // ctx = new ClassPathXmlApplicationContext(
        // new String[] {"classpath:applicationContext.xml"});
        // ctx.registerShutdownHook();

        EventBus.subscribe(ApplicationClosingEvent.class, new EventSubscriber<GeneDBMessage>() {
            public void onEvent(final GeneDBMessage ese) {
                shutdown();
            }
        });

        try {
        	UIManager.setLookAndFeel("sun.swing.plaf.nimbus.NimbusLookAndFeel");
            //UIManager.setLookAndFeel("org.jdesktop.swingx.plaf.nimbus.NimbusLookAndFeel");
        } catch (final Exception exp) {
            exp.printStackTrace();
        }

        // EventBus.subscribe(ApplicationClosingEvent.class, new
        // VetoEventListener {});

        EventBus.subscribe(OpenWindowEvent.class, this);
        
        TimerTask fetchMessage = new TimerTask() {
        	@Override
        	public void run() {
        	
        		String clientName = "dummy"; // FIXME
				Deque<Message> messages = messageService.checkMessages(clientName);
				if (messages != null) {
					for (Message message : messages) {
						EventBus.publish(message);
					}
				}
        	}
        };
        timer.scheduleAtFixedRate(fetchMessage, TIMER_DELAY, TIMER_DELAY);

    }

    protected void finishUp() {
        // TODO Auto-generated method stub
    }

    public void init() throws Exception {
        final JograLogin loginWindow = new JograLogin();
        // loginWindow.setUser(user);
        loginWindow.pack();
        loginWindow.setVisible(true);
    }

    public void makeMain() throws IOException {

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
                // System.exit(0);
            }
        });
        mainFrame.setTitle("Jogra");

        final JMenuBar menu = new JMenuBar();

        final JMenu pluginMenu = new JMenu("Plugins");
        for (final JograPlugin plugin : pluginList) {
            pluginMenu.add(new JMenuItem(plugin.getName()));
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

        for (final JograPlugin plugin : pluginList) {
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

    public void propertyChange(final PropertyChangeEvent evt) {
        // updateTitle();
    }

    private void restart() {
        // TODO Auto-generated method stub
    }

    public void setJograBusiness(final JograBusiness jograBusiness) {
        this.jograBusiness = jograBusiness;
    }

    public void showMain() throws IOException {
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

    public static Jogra instantiate() throws IOException {
        //final AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(
          //      new String[] { "classpath:applicationContext.xml", "classpath:domain-client-applicationContext.xml" });
        final AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] { "classpath:domain-client-applicationContext.xml", "classpath:applicationContext.xml" });
        final Jogra application = (Jogra) ctx.getBean("application", Jogra.class);
        ctx.registerShutdownHook();
        return application;
    }

    // public void setTestService(TestService testService) {
    // this.testService = testService;
    // }

    public static void main(final String[] args) throws Exception {

        final Jogra application = Jogra.instantiate();

        application.testTransactions();

        application.init();
        // application.logon();
        application.makeMain();
        application.showMain();

        // ps.showSplash();

        // EventQueue.invokeLater(new Runnable() {
        // public void run() {
        // try {
        // ps.makeMain();
        // ps.showMain();
        // } catch (IOException exp) {
        // // TODO Auto-generated catch block
        // exp.printStackTrace();
        // }
        // }
        // });
    }

	public void setPluginList(List<JograPlugin> pluginList) {
		this.pluginList.addAll(pluginList);
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
}
