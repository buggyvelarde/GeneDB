package org.genedb.web.menu;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Parent class for all types of menus.
 * 
 */
public abstract class Menu {
    private String menuName;
    private String menuId;
    private String url;
    private String parentId;
    protected boolean isLeaf = false;
    private String levelCoord;
    public static int counter = 0;
    private boolean isTop = false;
    public static int left = 154;
    public static int top = 200;

    public int getCounter() {
        return counter;
    }

    public Menu(String menuId, String menuName) {
        this.menuName = menuName;
        this.menuId = menuId;

    }

    public Menu(String menuId, String menuName, String url) {
        this.menuName = menuName;
        this.menuId = menuId;
        this.url = url;
    }

    public Menu(String menuId, String menuName, String url, boolean isTop) {
        this.menuName = menuName;
        this.menuId = menuId;
        this.url = url;
        this.isTop = isTop;
    }

    /**
     * Returns the menu name
     * 
     * @return Menu name(section or function)
     */
    public String getMenuName() {
        return this.menuName;
    }

    /**
     * Returns the menuID
     * 
     * @return Menu ID
     */
    public String getMenuId() {
        return this.menuId;
    }

    /**
     * Returns the parentID
     * 
     * @return parent ID
     */
    public String getParent() {
        return this.parentId;
    }

    /**
     * Returns the function url
     * 
     * @return function url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the parentID.
     * 
     * @param parentID
     */
    public void setParent(String parentId) {
        this.parentId = parentId;
    }

    /**
     * Returns the LevelCoordinate
     * 
     * @return LevelCoordinate
     */
    public String getLevelCoord() {
        return levelCoord;
    }

    /**
     * Set the LevelCoordinate
     * 
     * @param LevelCoordinate
     */
    public void setLevelCoord(String levelCoord) {
        this.levelCoord = levelCoord;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    /**
     * Abstract method that returns the list of child menus
     * 
     * @return collection of child menus
     */
    public abstract Collection<Menu> listChildMenus();

    /**
     * Abstract method that renders the whole menu Hierarchy
     * 
     * @return String containing the whole menu Hierarchy
     */
    public abstract String render(int i);

    /**
     * Abstract method that adds the menu to the list
     * 
     * @param Menu object
     * @return boolean value for success or failure.
     * @exception NoSuchElementException
     */
    public abstract boolean add(Menu menu) throws NoSuchElementException;

    /**
     * Abstract method that removes the menu from the list
     * 
     * @param menu object
     * @return boolean value for success or failure.
     * @exception NoSuchElementException
     */
    public abstract void remove(Menu menu) throws NoSuchElementException;

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean isTop) {
        this.isTop = isTop;
    }

}// ~public abstract class Me...

