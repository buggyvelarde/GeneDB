package org.genedb.web.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class CompositeMenu extends Menu
{
    private Vector list = new Vector();

    /** @link aggregationByValue */
    /*# Menu lnkMenu; */

    public CompositeMenu(String menuId, String menuName)
    {
        super(menuId, menuName);
    }


    public CompositeMenu(String menuId, String menuName,String url,boolean isTop)
    {
    	super(menuId, menuName,url,isTop);
    }

    /**
     * Returns the list of child menus
     * @return collection of child menus
    */
    public Collection listChildMenus()
    {
        return list;
    }

    /**
     * Returns the child
     * @param functionid as string
     * @return the child
    */
    public Menu getChild(String s)
    {
        return null;
    }

  /**
     * Renders the section menus
     * @return String containing the section menus.
    */
    public String render(int j)
    {
        StringBuffer sb = new StringBuffer();

        /*
        sb.append("addmenuitem(");
        sb.append("\"" + getLevelCoord() + "\",");
        sb.append("\"" + getMenuName() + "\",");
     //   System.out.println(getUrl() + " "  + getMenuName());
        
        if (null == getUrl())
            sb.append("null" + ",");
         else
                sb.append("\"" + getUrl() + "\",");


        sb.append("\"black\",\"FAEBD7\",\"white\",\"3366CC\",\"white\",\"3366CC\",\"font-family:Tahoma, Verdana, Arial; font-size:12px;font-weight:normal,text-decoration:none;padding: 4px\");");
        sb.append("\n"); */

        String sections[] = getLevelCoord().split(",");
        int left;
        if (sections.length > 1) {
        	left = (sections.length * 154) + 1;
        } else {
        	left = sections.length * 154;
        }
        int zindex = sections.length - 1;
        int top = 0;
        StringBuffer path = new StringBuffer();
        for (int i = 0; i < sections.length; i++) {
        	path.append(Integer.parseInt(sections[i]) - 1);
        	path.append("_");
        	if (i == 0) {
        		top = Integer.parseInt(sections[i]) * 20;
        	} else {
        		top = top + (Integer.parseInt(sections[i]) - 1) * 20;
        	}
        }
        path.deleteCharAt(path.length()-1);
        top = top + 200 + (4 * j);
        Iterator it = list.iterator();
        StringBuffer childs = new StringBuffer();
        
        while(it.hasNext()){
        	Menu menu = (Menu)it.next();
        	childs.append(menu.getMenuId());
        	childs.append(",");
        }
        if (childs.length() > 0) {
        	childs.deleteCharAt(childs.length()-1);
        } 
        
        String id = "mi_0_" + getMenuId();
        
        if(isTop()) {
        	sb.append("<a name=\"" + path.toString() + "\" id=\"" + id + "\" onmouseover=\"mouseover(" + getMenuId() + ");\" onmouseout=\"mouseout();\" onclick=\"return mouseclick(" + getUrl() + ");\" style=\"text-decoration:none; border:1px solid black; background: rgb(153, 153, 255) none repeat scroll 0%; position: absolute; top: " + top + "px; left: " + left + "px; width: 154px; height: 20px; visibility: visible; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial; color: white; z-index: 0;\" href=\"null\">");
        } else {
        	sb.append("<a name=\"" + path.toString() + "\" id=\"" + id + "\" onmouseover=\"mouseover(" + getMenuId() + ");\" onmouseout=\"mouseout();\" onclick=\"return mouseclick(" + getUrl() + ");\" style=\"text-decoration:none; border:1px solid black; background: rgb(153, 153, 255) none repeat scroll 0%; position: absolute; top: " + top + "px; left: " + left + "px; width: 154px; height: 20px; visibility: hidden; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial; color: white; z-index: " + zindex + ";\" href=\"null\">");
        }
        sb.append("<div id=\"menudivs\" style=\"font-family:Tahoma,Verdana,Arial;font-size:12px;padding:4px;\">" + getMenuName() + "</div>");
        sb.append("</a>");
        sb.append("\n");
        it = list.iterator();
        int i=1;
        while(it.hasNext())
        {
            Menu menu = (Menu)it.next();

            //menu.setLevelCoord(getLevelCoord()+ i);
            menu.setLevelCoord(getLevelCoord() + "," + i);
            sb.append(menu.render(i+j-1));
            i++;
        }

        return sb.toString();
    }//~public String render()...

    /**
     * Adds the menu to the list
     * @param Menu object
     * @return boolean value for success or failure.
     * @exception NoSuchElementException
    */
    public  boolean add(Menu menu) throws NoSuchElementException
    {
        list.add(menu);
        return true;
    }

  /**
     * Removes the menu from the list
     * @param menu object
     * @exception NoSuchElementException
    */
    public void remove(Menu menu) throws NoSuchElementException
    {
        list.remove(menu);
    }


}//~public class CompositeMe...

