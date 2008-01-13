package org.genedb.jogra.plugins;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

public class ConflictComponentFactory {
	
	private static final Icon conflictIcon = new ConflictIcon(true);
	private static final Icon noConflictIcon = new ConflictIcon(false);
	
	public static Icon getConflictIcon(boolean conflict) {
		if (conflict) {
			return conflictIcon;
		}
		return noConflictIcon;
	}
	
	public static String getConflictString(boolean conflict) {
		if (conflict) {
			return "Already being edited";
		}
		return "";
	}
	
}
	
class ConflictIcon implements Icon {
	private int width = 32;
	private int height = 32;
	private boolean conflict;
	
	public ConflictIcon(boolean conflict) {
		this.conflict = conflict;
	}
	    
	private BasicStroke stroke = new BasicStroke(4);
	
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setColor(Color.WHITE);
		g2d.fillRect(x +1 ,y + 1,width -2 ,height -2);
		
		g2d.setColor(Color.BLACK);
		g2d.drawRect(x +1 ,y + 1,width -2 ,height -2);

		g2d.setStroke(stroke);
		
		if (conflict) {
			g2d.setColor(Color.RED);
			g2d.drawLine(x +10, y + 10, x + width -10, y + height -10);
			g2d.drawLine(x +10, y + height -10, x + width -10, y + 10);
		} else {
			g2d.setColor(Color.GREEN);
			g2d.drawLine(x +10, y + 10+ (height-20)/2, x + (width+20)/4, y + height -10);
			g2d.drawLine(x + (width+20)/4, y + height -10, x + width -10, y + 10);			
		}
		
		g2d.dispose();
	}
	    
	public int getIconWidth() {
		return width;
	}
	    
	public int getIconHeight() {
		return height;
	}
	    
}
