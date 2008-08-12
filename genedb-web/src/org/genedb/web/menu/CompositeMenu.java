package org.genedb.web.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

public class CompositeMenu extends Menu
{
    private List<Menu> list = new ArrayList<Menu>();

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
    @Override
    public Collection<Menu> listChildMenus()
    {
        return list;
    }

  /**
     * Renders the section menus
     * @return String containing the section menus.
    */
    @Override
    public String render(int j)
    {
        StringBuffer sb = new StringBuffer();
        String sections[] = getLevelCoord().split(",");

        int left;
        if (sections.length > 1) {
            left = (sections.length * 154) + 1 - (154 - Menu.left);
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
                top = Integer.parseInt(sections[i]) * 25;
            } else {
                top = top + (Integer.parseInt(sections[i]) - 1) * 25;
            }
        }
        path.deleteCharAt(path.length()-1);
        top = top + Menu.top + (4 * j);
        StringBuffer childs = new StringBuffer();
        
        for(Menu menu: list) {
            childs.append(menu.getMenuId());
            childs.append(",");
        }
        if (childs.length() > 0) {
            childs.deleteCharAt(childs.length()-1);
        } 
        
        String id = "mi_0_" + getMenuId();
        String menuid = "menu_" + getMenuId();
        String checkid = "check_" + getMenuId();
        
        if(isTop()) {
            sb.append("<a name=\"" + path.toString() + "\" id=\"" + id + "\" onmouseover=\"mouseover(" + getMenuId() + ");\" onmouseout=\"mouseout();\" style=\"text-decoration:none; border:1px solid black; background: rgb(153, 153, 255) none repeat scroll 0%; position: absolute; top: " + top + "px; left: " + left + "px; width: 154px; height: 25px; display: none; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial; color: white; z-index: 0;\" >");
        } else {
            sb.append("<a name=\"" + path.toString() + "\" id=\"" + id + "\" onmouseover=\"mouseover(" + getMenuId() + ");\" onmouseout=\"mouseout();\" style=\"text-decoration:none; border:1px solid black; background: rgb(153, 153, 255) none repeat scroll 0%; position: absolute; top: " + top + "px; left: " + left + "px; width: 154px; height: 25px; display: none; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial; color: white; z-index: " + zindex + ";\" >");
        }
        sb.append("<div id=\"" + menuid + "\" onclick=\"mouseclick(" + getMenuId() + ");\" style=\"font-family:Tahoma,Verdana,Arial;font-size:12px;padding:4px;\"><input value=\"" + getMenuName() + "\" style=\"z-index: " + zindex + "\" name=\"" + path.toString() + "\" type=\"checkbox\" id=\"" + checkid + "\" onclick=\"boxclicked(" + getMenuId() + ")\">" + getMenuName() + "</input></div>");
        sb.append("</a>");
        sb.append("\n");

        int i=1;
        for(Menu menu: list)
        {
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
    @Override
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
    @Override
    public void remove(Menu menu) throws NoSuchElementException
    {
        list.remove(menu);
    }


}//~public class CompositeMe...

