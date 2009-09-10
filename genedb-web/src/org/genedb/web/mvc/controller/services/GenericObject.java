package org.genedb.web.mvc.controller.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.lang.Iterable;

/**
 * A simple generic-object for representing nested data structures.
 * 
 * @author gv1
 *
 */
public class GenericObject implements Iterable<Object> 
{
	private String name = "";	
	private List<Object> objects = new ArrayList<Object>();
	
	/**
	 * Construct the object and add a child to it at the same time.
	 * @param name
	 * @param object
	 */
	public GenericObject(String name, Object object)
	{
		setName(name);
		add(object);
	}
	
	public GenericObject(String name)
	{
		setName(name);
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void add(Object obj)
	{
		objects.add(obj);
	}
	
	public Iterator<Object> iterator() {
		return objects.iterator();
	}
	
	public int size()
	{
		return objects.size();
	}
	
	public boolean isEmpty()
	{
		return (objects.size() == 0);
	}
	
	public Object get(int index)
	{
		return objects.get(index);
	}
	
}
