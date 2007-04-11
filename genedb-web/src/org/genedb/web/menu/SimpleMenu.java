package org.genedb.web.menu;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Class that extends menu and implements its abstract method for menu functions.
 *
*/
public class SimpleMenu extends Menu
{
    public SimpleMenu( String menuId, String menuName,String url)
    {
        super(menuId, menuName,url);
        isLeaf=true;
    }

    /**
     * Returns the list of child menus
     * @return collection of child menus
    */
    public Collection listChildMenus() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("This mehtod Cannot be invoked for simple menu item");
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
     * Renders the function menus for the immediate section
     * @return String containing the function menus.
    */
    public String render(int j)
    {
        StringBuffer sb = new StringBuffer();

     //   System.out.println("simple " + getMenuName() + " " + getUrl());
        /*old code....
         * 
         
        sb.append("addmenuitem(");
        sb.append("\"" + getLevelCoord() + "\",");
        sb.append("\"" + getMenuName() + "\",");
        sb.append("\"" + getUrl() + "\",");
        sb.append("\"black\",\"FAEBD7\",\"white\",\"3366CC\",\"white\",\"3366CC\",\"font-family:Tahoma, Verdana, Arial; font-size:12px;font-weight:normal,text-decoration:none;padding: 4px\");");

        sb.append("\n");
        String sections[] = getLevelCoord().split(",");
        int left = sections.length * 154;
        int top = 0;
        for (int i = 0; i < sections.length; i++) {
        	if (i == 0) {
        		top = Integer.parseInt(sections[i]) * 20 + 4;
        	} else {
        		top = top + (Integer.parseInt(sections[i]) - 1) * 20 + 4;
        	}
        }
        top = top + 200;
        String id = "mi_0_" + getMenuId();
        
        sb.append("<a id=\"" + id + "\" onmouseover=\"mouseover('"+ getMenuId() + "');\" onmouseout=\"mouseout('" + getMenuId() + "');\" onclick=\"return mouseclick();\" style=\"text-decoration:none; border:1px solid black; background: rgb(153, 153, 255) none repeat scroll 0%; position: absolute; top: " + top + "px; left: " + left + "px; width: 154px; height: 20px; visibility: hidden; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial; color: white; z-index: 0;\" href=\"null\">");
        System.out.println(sb.toString());
        sb.append("<div id=\"menudivs\">" + getMenuName() + "</div>");
        sb.append("</a>");
        sb.append("\n");
        return sb.toString(); */
        String sections[] = getLevelCoord().split(",");
        int left;
        if (sections.length > 1) {
        	left = (sections.length * 154) + 1 - (154 - Menu.left) ;
        } else {
        	left = sections.length * Menu.left;
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
        top = top + Menu.top + (4 * j);
               
        String id = "mi_0_" + getMenuId();
        
       	sb.append("<a name=\"" + path.toString() + "\" id=\"" + id + "\" onmouseover=\"mouseover(" + getMenuId() + ");\" onmouseout=\"mouseout();\" onclick=\"return mouseclick(" + getUrl() + ");\" style=\"text-decoration:none; border:1px solid black; background: rgb(153, 153, 255) none repeat scroll 0%; position: absolute; top: " + top + "px; left: " + left + "px; width: 154px; height: 20px; visibility: hidden; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial; color: white; z-index: " + zindex + ";\" href=\"null\">");

        sb.append("<div id=\"menudivs\" style=\"font-family:Tahoma,Verdana,Arial;font-size:12px;padding:4px;\">" + getMenuName() + "</div>");
        sb.append("</a>");
        sb.append("\n");
              

        return sb.toString();
        
    }//~public String render()...

  /**
     * Adds the function menu to the list
     * @param Menu object
     * @return boolean value for success or failure.
     * @exception NoSuchElementException
    */
    public  boolean add(Menu menu) throws NoSuchElementException, UnsupportedOperationException
    {
        // list.add(menu);
        throw new UnsupportedOperationException("This mehtod Cannot be invoked for simple menu item");
    }

    /**
     * Removes the function menu from the list
     * @param Menu object
     * @exception NoSuchElementException
    */
    public void remove(Menu menu) throws NoSuchElementException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("This mehtod Cannot be invoked for simple menu item");
    }
}//~public class SimpleMenu ...

