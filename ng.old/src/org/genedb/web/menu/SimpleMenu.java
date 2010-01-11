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
    @Override
    public Collection<Menu> listChildMenus() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("This mehtod Cannot be invoked for simple menu item");
    }

    /**
     * Renders the function menus for the immediate section
     * @return String containing the function menus.
    */
    @Override
    public String render(int j)
    {
        StringBuffer sb = new StringBuffer();
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
        String menuid = "menu_" + getMenuId();
        String checkid = "check_" + getMenuId();
        
        sb.append("<a name=\"" + path.toString() + "\" id=\"" + id + "\" onmouseover=\"mouseover(" + getMenuId() + ");\" onmouseout=\"mouseout();\" style=\"text-decoration:none; border:1px solid black; background: rgb(153, 153, 255) none repeat scroll 0%; position: absolute; top: " + top + "px; left: " + left + "px; width: 154px; height: 25px; display: none; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial; color: white; z-index: " + zindex + ";\" >");

        sb.append("<div id=\"" + menuid + "\" onclick=\"mouseclick(" + getMenuId() + ");\" style=\"font-family:Tahoma,Verdana,Arial;font-size:12px;padding:4px;\"><input value=\"" + getMenuName() + "\" style=\"z-index: " + zindex + "\" name=\"" + path.toString() + "\" type=\"checkbox\" id=\"" + checkid + "\" onclick=\"boxclicked(" + getMenuId() + ")\">" + getMenuName() + "</input></div>");
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
    @Override
    public  boolean add(Menu menu) throws NoSuchElementException, UnsupportedOperationException
    {
        // list.add(menu);
        throw new UnsupportedOperationException("This method Cannot be invoked for simple menu item");
    }

    /**
     * Removes the function menu from the list
     * @param Menu object
     * @exception NoSuchElementException
    */
    @Override
    public void remove(Menu menu) throws NoSuchElementException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("This method Cannot be invoked for simple menu item");
    }
}//~public class SimpleMenu ...

