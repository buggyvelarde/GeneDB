/*
 * Copyright (c) 2007 Genome Research Limited.
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

package org.genedb.jogra.plugins;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.bushe.swing.event.EventBus;
import org.genedb.jogra.domain.GeneDBMessage;
import org.genedb.jogra.domain.Product;
import org.genedb.jogra.drawing.Jogra;
import org.genedb.jogra.drawing.JograPlugin;
import org.genedb.jogra.drawing.OpenWindowEvent;
import org.genedb.jogra.services.MethodResult;
import org.genedb.jogra.services.ProductService;

import skt.swing.SwingUtil;
import skt.swing.search.IncrementalSearchKeyListener;
import skt.swing.search.ListFindAction;


public class ProductRationaliser implements JograPlugin {
	
	private static final String WINDOW_TITLE = "Product Rationaliser";
	private static final String A_LONG_STRING = "This is the maximum product width we show";
	private ProductService productService;
	JList fromList;
	JList toList; 
    JCheckBox filterBox;
    JLabel productCountLabel;
	
	private void initModels() {
		List<Product> products = productService.getProductList(filterBox.isSelected());
		Product[] productArray = new Product[products.size()];
		//System.err.println("The number of products is '"+products.size()+"'");
		int i=0;
		for (Product product : products) {
			productArray[i] = product;
			i++;
		}
		fromList.setListData(productArray);
		toList.setListData(productArray);
		productCountLabel.setText(products.size()+" Products");
	}
	
	
    public JFrame getMainPanel() {

        fromList = new JList();
        toList = new JList();
        filterBox = new JCheckBox("Only products annotated to genes", true);
        productCountLabel = new JLabel("No. of products");
        
        fromList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fromList.setPrototypeCellValue(A_LONG_STRING);
        ListFindAction findAction = new ListFindAction(true);
        SyncAction fromSync = new SyncAction(fromList, toList, KeyStroke.getKeyStroke("RIGHT"));
        SwingUtil.installActions(fromList, new Action[]{
            fromSync,
            findAction,
            new ListFindAction(false)
        });
        fromList.addKeyListener(new IncrementalSearchKeyListener(findAction));

        toList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toList.setPrototypeCellValue(A_LONG_STRING);
        SyncAction toSync = new SyncAction(toList, fromList, KeyStroke.getKeyStroke("LEFT"));
        SwingUtil.installActions(toList, new Action[]{
                toSync,
                findAction,
                new ListFindAction(false)
            });
        toList.addKeyListener(new IncrementalSearchKeyListener(findAction));
        //InputMap map = toList.getInputMap(JComponent.WHEN_FOCUSED);
        // List keystrokes in the component and in all parent input maps
        //list(map, map.allKeys());
    	initModels();
    	
        final JFrame ret = new JFrame();
        ret.setTitle(WINDOW_TITLE);
        ret.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        ret.setLayout(new BorderLayout());
        

        Box center = Box.createHorizontalBox();
        center.add(Box.createHorizontalStrut(5));
        Box leftPane = Box.createVerticalBox();
        leftPane.add(new JLabel("From"));
        leftPane.add(new JScrollPane(fromList));
        center.add(leftPane);
        center.add(Box.createHorizontalStrut(3));
        Box rightPane = Box.createVerticalBox();
        rightPane.add(new JLabel("To"));
        rightPane.add(new JScrollPane(toList));
        center.add(rightPane);
        center.add(Box.createHorizontalStrut(5));
        
        ret.add(center, BorderLayout.CENTER);
        
        
        Box buttons = Box.createVerticalBox();
        
        Box localButtons = Box.createHorizontalBox();
//        localButtons.add(Box.createHorizontalGlue());
        filterBox.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		initModels();	
        	}
        });
        localButtons.add(filterBox);
        localButtons.add(Box.createHorizontalStrut(10));
        localButtons.add(productCountLabel);
//        JButton syncLeftToRight = new JButton(">>");
//        localButtons.add(syncLeftToRight);
//        localButtons.add(Box.createHorizontalStrut(5));
//        JButton syncRightToLeft = new JButton("<<");
//        localButtons.add(syncRightToLeft);
        localButtons.add(Box.createHorizontalGlue());
        buttons.add(localButtons);
        
        Box actionButtons = Box.createHorizontalBox();
        actionButtons.add(Box.createHorizontalGlue());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		initModels(); // FIXME
        	}
        });
        actionButtons.add(refresh);
        actionButtons.add(Box.createHorizontalStrut(10));

        JButton speeling = new JButton(new FindClosestMatchAction());
        actionButtons.add(speeling);
        actionButtons.add(Box.createHorizontalStrut(10));
        
        RationaliserAction ra = new RationaliserAction();
        JButton go = new JButton(ra);

        actionButtons.add(go);
        actionButtons.add(Box.createHorizontalGlue());
        buttons.add(actionButtons);
        
        JLabel hints = new JLabel("<html><i><ol>"+
        		"<li>Use the left or right arrow, as appropriate, to sync the selection" +
        		"<li>Use CTRL+i to start an incremental search, then the UP/DOWN arrows" +
        		"</ol></i></html>");
        
        
        buttons.add(hints);
        
        ret.add(buttons, BorderLayout.SOUTH);
        
        ret.pack();
        
        return ret;
    }

    public JPanel getMainWindowPlugin() {
        final JPanel ret = new JPanel();
        final JButton temp = new JButton("Load Product Rationaliser");
        temp.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                System.err.println("Am I on EDT '" + EventQueue.isDispatchThread() + "' 1");
                new SwingWorker<JFrame, Void>() {

                    @Override
                    protected JFrame doInBackground() throws Exception {
                        return makeWindow();
                    }

                    @Override
                    public void done() {
                        try {
                            final GeneDBMessage e = new OpenWindowEvent(ProductRationaliser.this, get());
                            EventBus.publish(e);
                        } catch (final InterruptedException exp) {
                            exp.printStackTrace();
                        } catch (final ExecutionException exp) {
                            exp.printStackTrace();
                        }
                    }
                }.execute();
            }
        });
        ret.add(temp);
        return ret;
    }

    public String getName() {
        return WINDOW_TITLE;
    }

    public int getOrder() {
        return 7;
    }

    public boolean isSingletonByDefault() {
        return true;
    }

    public boolean isUnsaved() {
        // TODO Auto-generated method stub
        return false;
    }

    private JFrame makeWindow() {
        System.err.println("Am I on EDT '" + EventQueue.isDispatchThread() + "'  x");
        JFrame lookup = Jogra.findNamedWindow(WINDOW_TITLE);
        if (lookup == null) {
            lookup = getMainPanel();
        }
        return lookup;
    }

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	class FindClosestMatchAction extends AbstractAction implements ListSelectionListener {
		
		public FindClosestMatchAction() {
			putValue(AbstractAction.NAME, "Find possible fix");
			fromList.addListSelectionListener(this);
			enableBasedOnSelection();
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Product from = (Product) fromList.getSelectedValue();
			
			int match = findClosestMatch(from.toString(), fromList.getSelectedIndex(), toList.getModel());
			if (match != -1) {
				toList.setSelectedIndex(match);
				toList.ensureIndexIsVisible(match);
			}
		}
		
		int findClosestMatch(String in, int fromIndex, ListModel list) {
			//System.err.println("Looking for match for '"+in+"'");
			int current = -1;
			int distance = Integer.MAX_VALUE;
			for (int i = 0; i < list.getSize(); i++) {
				if (i == fromIndex) {
					continue;
				}
				String element = ((Product)list.getElementAt(i)).toString();
				if (in.equalsIgnoreCase(element)) {
					System.err.println("Found identical except case at '"+i+"'");
					return i;
				}
				int d = org.apache.commons.lang.StringUtils.getLevenshteinDistance(in, element);
				if (d==1) {
					//System.err.println("Found 1 away at '"+i+"'");
					return i;
				}
				if ( d < distance) {
					//System.err.println("Found distance '"+d+"' at '"+i+"'");
					distance = d;
					current = i;
				}
			}
			return current;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			enableBasedOnSelection();
		}

		private void enableBasedOnSelection() {
			boolean selection = (fromList.getMinSelectionIndex()!=-1);
			if (this.isEnabled() != selection) {
				this.setEnabled(selection);
			}
		}
		
	}


	class RationaliserAction extends AbstractAction implements ListSelectionListener {
		
		public RationaliserAction() {
			putValue(AbstractAction.NAME, "Rationalise Products");
			fromList.addListSelectionListener(this);
			toList.addListSelectionListener(this);
			enableBasedOnSelection();
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Product to = (Product) toList.getSelectedValue();
			Object[] from = fromList.getSelectedValues();
			List<Product> old = new ArrayList<Product>();
			for (Object o : from) {	
				old.add((Product)o);
			}
			MethodResult result = productService.rationaliseProduct(to, old);
			// TODO Check results
			initModels();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			enableBasedOnSelection();
		}

		private void enableBasedOnSelection() {
			boolean selection = (fromList.getMinSelectionIndex()!=-1) 
				&& (toList.getMinSelectionIndex()!=-1);
			if (this.isEnabled() != selection) {
				this.setEnabled(selection);
			}
		}
		
	}

    public class SyncAction extends AbstractAction {

		private JList sourceList;
		private JList targetList;
		
		public SyncAction(JList sourceList, JList targetList, KeyStroke keyStroke) {
			this.targetList = targetList;
			this.sourceList = sourceList;
			putValue(NAME, "syncList");
			putValue(ACCELERATOR_KEY, keyStroke);
		}
    	
		public void actionPerformed(ActionEvent evt) {
			System.err.println("key listener called");
			int index = sourceList.getSelectedIndex();
			targetList.setSelectedIndex(index);
			targetList.ensureIndexIsVisible(index);
		}
    	
    }
    
    public static void list(InputMap map, KeyStroke[] keys) {
        if (keys == null) {
            return;
        }
        for (int i=0; i<keys.length; i++) {
            // This method is defined in e859 Converting a KeyStroke to a String
            String keystrokeStr =  keyStroke2String(keys[i]);

            // Get the action name bound to this keystroke
            while (map.get(keys[i]) == null) {
                map = map.getParent();
            }
            if (map.get(keys[i]) instanceof String) {
                String actionName = (String)map.get(keys[i]);
                System.err.println(keystrokeStr+"        "+actionName);
            } else {
                Action action = (Action)map.get(keys[i]);
                System.err.println(keystrokeStr+"        "+action);
            }
        }
    }
    
    public static String keyStroke2String(KeyStroke key) {
        StringBuffer s = new StringBuffer(50);
        int m = key.getModifiers();
    
        if ((m & (InputEvent.SHIFT_DOWN_MASK|InputEvent.SHIFT_MASK)) != 0) {
            s.append("shift ");
        }
        if ((m & (InputEvent.CTRL_DOWN_MASK|InputEvent.CTRL_MASK)) != 0) {
            s.append("ctrl ");
        }
        if ((m & (InputEvent.META_DOWN_MASK|InputEvent.META_MASK)) != 0) {
            s.append("meta ");
        }
        if ((m & (InputEvent.ALT_DOWN_MASK|InputEvent.ALT_MASK)) != 0) {
            s.append("alt ");
        }
        if ((m & (InputEvent.BUTTON1_DOWN_MASK|InputEvent.BUTTON1_MASK)) != 0) {
            s.append("button1 ");
        }
        if ((m & (InputEvent.BUTTON2_DOWN_MASK|InputEvent.BUTTON2_MASK)) != 0) {
            s.append("button2 ");
        }
        if ((m & (InputEvent.BUTTON3_DOWN_MASK|InputEvent.BUTTON3_MASK)) != 0) {
            s.append("button3 ");
        }
    
        switch (key.getKeyEventType()) {
        case KeyEvent.KEY_TYPED:
            s.append("typed ");
            s.append(key.getKeyChar() + " ");
            break;
        case KeyEvent.KEY_PRESSED:
            s.append("pressed ");
            s.append(getKeyText(key.getKeyCode()) + " ");
            break;
        case KeyEvent.KEY_RELEASED:
            s.append("released ");
            s.append(getKeyText(key.getKeyCode()) + " ");
            break;
        default:
            s.append("unknown-event-type ");
            break;
        }
    
        return s.toString();
    }
    
    public static String getKeyText(int keyCode) {
        if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9 ||
            keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            return String.valueOf((char)keyCode);
        }
    
        switch(keyCode) {
          case KeyEvent.VK_COMMA: return "COMMA";
          case KeyEvent.VK_PERIOD: return "PERIOD";
          case KeyEvent.VK_SLASH: return "SLASH";
          case KeyEvent.VK_SEMICOLON: return "SEMICOLON";
          case KeyEvent.VK_EQUALS: return "EQUALS";
          case KeyEvent.VK_OPEN_BRACKET: return "OPEN_BRACKET";
          case KeyEvent.VK_BACK_SLASH: return "BACK_SLASH";
          case KeyEvent.VK_CLOSE_BRACKET: return "CLOSE_BRACKET";
    
          case KeyEvent.VK_ENTER: return "ENTER";
          case KeyEvent.VK_BACK_SPACE: return "BACK_SPACE";
          case KeyEvent.VK_TAB: return "TAB";
          case KeyEvent.VK_CANCEL: return "CANCEL";
          case KeyEvent.VK_CLEAR: return "CLEAR";
          case KeyEvent.VK_SHIFT: return "SHIFT";
          case KeyEvent.VK_CONTROL: return "CONTROL";
          case KeyEvent.VK_ALT: return "ALT";
          case KeyEvent.VK_PAUSE: return "PAUSE";
          case KeyEvent.VK_CAPS_LOCK: return "CAPS_LOCK";
          case KeyEvent.VK_ESCAPE: return "ESCAPE";
          case KeyEvent.VK_SPACE: return "SPACE";
          case KeyEvent.VK_PAGE_UP: return "PAGE_UP";
          case KeyEvent.VK_PAGE_DOWN: return "PAGE_DOWN";
          case KeyEvent.VK_END: return "END";
          case KeyEvent.VK_HOME: return "HOME";
          case KeyEvent.VK_LEFT: return "LEFT";
          case KeyEvent.VK_UP: return "UP";
          case KeyEvent.VK_RIGHT: return "RIGHT";
          case KeyEvent.VK_DOWN: return "DOWN";
    
          // numpad numeric keys handled below
          case KeyEvent.VK_MULTIPLY: return "MULTIPLY";
          case KeyEvent.VK_ADD: return "ADD";
          case KeyEvent.VK_SEPARATOR: return "SEPARATOR";
          case KeyEvent.VK_SUBTRACT: return "SUBTRACT";
          case KeyEvent.VK_DECIMAL: return "DECIMAL";
          case KeyEvent.VK_DIVIDE: return "DIVIDE";
          case KeyEvent.VK_DELETE: return "DELETE";
          case KeyEvent.VK_NUM_LOCK: return "NUM_LOCK";
          case KeyEvent.VK_SCROLL_LOCK: return "SCROLL_LOCK";
    
          case KeyEvent.VK_F1: return "F1";
          case KeyEvent.VK_F2: return "F2";
          case KeyEvent.VK_F3: return "F3";
          case KeyEvent.VK_F4: return "F4";
          case KeyEvent.VK_F5: return "F5";
          case KeyEvent.VK_F6: return "F6";
          case KeyEvent.VK_F7: return "F7";
          case KeyEvent.VK_F8: return "F8";
          case KeyEvent.VK_F9: return "F9";
          case KeyEvent.VK_F10: return "F10";
          case KeyEvent.VK_F11: return "F11";
          case KeyEvent.VK_F12: return "F12";
          case KeyEvent.VK_F13: return "F13";
          case KeyEvent.VK_F14: return "F14";
          case KeyEvent.VK_F15: return "F15";
          case KeyEvent.VK_F16: return "F16";
          case KeyEvent.VK_F17: return "F17";
          case KeyEvent.VK_F18: return "F18";
          case KeyEvent.VK_F19: return "F19";
          case KeyEvent.VK_F20: return "F20";
          case KeyEvent.VK_F21: return "F21";
          case KeyEvent.VK_F22: return "F22";
          case KeyEvent.VK_F23: return "F23";
          case KeyEvent.VK_F24: return "F24";
    
          case KeyEvent.VK_PRINTSCREEN: return "PRINTSCREEN";
          case KeyEvent.VK_INSERT: return "INSERT";
          case KeyEvent.VK_HELP: return "HELP";
          case KeyEvent.VK_META: return "META";
          case KeyEvent.VK_BACK_QUOTE: return "BACK_QUOTE";
          case KeyEvent.VK_QUOTE: return "QUOTE";
    
          case KeyEvent.VK_KP_UP: return "KP_UP";
          case KeyEvent.VK_KP_DOWN: return "KP_DOWN";
          case KeyEvent.VK_KP_LEFT: return "KP_LEFT";
          case KeyEvent.VK_KP_RIGHT: return "KP_RIGHT";
    
          case KeyEvent.VK_DEAD_GRAVE: return "DEAD_GRAVE";
          case KeyEvent.VK_DEAD_ACUTE: return "DEAD_ACUTE";
          case KeyEvent.VK_DEAD_CIRCUMFLEX: return "DEAD_CIRCUMFLEX";
          case KeyEvent.VK_DEAD_TILDE: return "DEAD_TILDE";
          case KeyEvent.VK_DEAD_MACRON: return "DEAD_MACRON";
          case KeyEvent.VK_DEAD_BREVE: return "DEAD_BREVE";
          case KeyEvent.VK_DEAD_ABOVEDOT: return "DEAD_ABOVEDOT";
          case KeyEvent.VK_DEAD_DIAERESIS: return "DEAD_DIAERESIS";
          case KeyEvent.VK_DEAD_ABOVERING: return "DEAD_ABOVERING";
          case KeyEvent.VK_DEAD_DOUBLEACUTE: return "DEAD_DOUBLEACUTE";
          case KeyEvent.VK_DEAD_CARON: return "DEAD_CARON";
          case KeyEvent.VK_DEAD_CEDILLA: return "DEAD_CEDILLA";
          case KeyEvent.VK_DEAD_OGONEK: return "DEAD_OGONEK";
          case KeyEvent.VK_DEAD_IOTA: return "DEAD_IOTA";
          case KeyEvent.VK_DEAD_VOICED_SOUND: return "DEAD_VOICED_SOUND";
          case KeyEvent.VK_DEAD_SEMIVOICED_SOUND: return "DEAD_SEMIVOICED_SOUND";
    
          case KeyEvent.VK_AMPERSAND: return "AMPERSAND";
          case KeyEvent.VK_ASTERISK: return "ASTERISK";
          case KeyEvent.VK_QUOTEDBL: return "QUOTEDBL";
          case KeyEvent.VK_LESS: return "LESS";
          case KeyEvent.VK_GREATER: return "GREATER";
          case KeyEvent.VK_BRACELEFT: return "BRACELEFT";
          case KeyEvent.VK_BRACERIGHT: return "BRACERIGHT";
          case KeyEvent.VK_AT: return "AT";
          case KeyEvent.VK_COLON: return "COLON";
          case KeyEvent.VK_CIRCUMFLEX: return "CIRCUMFLEX";
          case KeyEvent.VK_DOLLAR: return "DOLLAR";
          case KeyEvent.VK_EURO_SIGN: return "EURO_SIGN";
          case KeyEvent.VK_EXCLAMATION_MARK: return "EXCLAMATION_MARK";
          case KeyEvent.VK_INVERTED_EXCLAMATION_MARK:
                   return "INVERTED_EXCLAMATION_MARK";
          case KeyEvent.VK_LEFT_PARENTHESIS: return "LEFT_PARENTHESIS";
          case KeyEvent.VK_NUMBER_SIGN: return "NUMBER_SIGN";
          case KeyEvent.VK_MINUS: return "MINUS";
          case KeyEvent.VK_PLUS: return "PLUS";
          case KeyEvent.VK_RIGHT_PARENTHESIS: return "RIGHT_PARENTHESIS";
          case KeyEvent.VK_UNDERSCORE: return "UNDERSCORE";
    
          case KeyEvent.VK_FINAL: return "FINAL";
          case KeyEvent.VK_CONVERT: return "CONVERT";
          case KeyEvent.VK_NONCONVERT: return "NONCONVERT";
          case KeyEvent.VK_ACCEPT: return "ACCEPT";
          case KeyEvent.VK_MODECHANGE: return "MODECHANGE";
          case KeyEvent.VK_KANA: return "KANA";
          case KeyEvent.VK_KANJI: return "KANJI";
          case KeyEvent.VK_ALPHANUMERIC: return "ALPHANUMERIC";
          case KeyEvent.VK_KATAKANA: return "KATAKANA";
          case KeyEvent.VK_HIRAGANA: return "HIRAGANA";
          case KeyEvent.VK_FULL_WIDTH: return "FULL_WIDTH";
          case KeyEvent.VK_HALF_WIDTH: return "HALF_WIDTH";
          case KeyEvent.VK_ROMAN_CHARACTERS: return "ROMAN_CHARACTERS";
          case KeyEvent.VK_ALL_CANDIDATES: return "ALL_CANDIDATES";
          case KeyEvent.VK_PREVIOUS_CANDIDATE: return "PREVIOUS_CANDIDATE";
          case KeyEvent.VK_CODE_INPUT: return "CODE_INPUT";
          case KeyEvent.VK_JAPANESE_KATAKANA: return "JAPANESE_KATAKANA";
          case KeyEvent.VK_JAPANESE_HIRAGANA: return "JAPANESE_HIRAGANA";
          case KeyEvent.VK_JAPANESE_ROMAN: return "JAPANESE_ROMAN";
          case KeyEvent.VK_KANA_LOCK: return "KANA_LOCK";
          case KeyEvent.VK_INPUT_METHOD_ON_OFF: return "INPUT_METHOD_ON_OFF";
    
          case KeyEvent.VK_AGAIN: return "AGAIN";
          case KeyEvent.VK_UNDO: return "UNDO";
          case KeyEvent.VK_COPY: return "COPY";
          case KeyEvent.VK_PASTE: return "PASTE";
          case KeyEvent.VK_CUT: return "CUT";
          case KeyEvent.VK_FIND: return "FIND";
          case KeyEvent.VK_PROPS: return "PROPS";
          case KeyEvent.VK_STOP: return "STOP";
    
          case KeyEvent.VK_COMPOSE: return "COMPOSE";
          case KeyEvent.VK_ALT_GRAPH: return "ALT_GRAPH";
        }
    
        if (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9) {
            char c = (char)(keyCode - KeyEvent.VK_NUMPAD0 + '0');
            return "NUMPAD"+c;
        }
    
        return "unknown(0x" + Integer.toString(keyCode, 16) + ")";
    }


	public void process(List<String> newArgs) {
		// TODO Auto-generated method stub
		
	}
	

}