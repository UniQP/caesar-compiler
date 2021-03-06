/*
 * This source file is part of CaesarJ 
 * For the latest info, see http://caesarj.org/
 * 
 * Copyright � 2003-2005 
 * Darmstadt University of Technology, Software Technology Group
 * Also see acknowledgements in readme.txt
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * $Id: AspectThisObjectMapper.java,v 1.1 2006-01-13 12:06:06 gasiunas Exp $
 */

package org.caesarj.runtime.perobject;

import java.util.WeakHashMap;

import org.caesarj.runtime.aspects.AbstractAspectRegistry;
import org.caesarj.runtime.aspects.AspectContainerIfc;
import org.caesarj.runtime.aspects.DynArray;

/**
 * @author Vaidas Gasiunas
 *
 * Maintaints aspects deployed on objects
 */
public class AspectThisObjectMapper implements AspectContainerIfc {
	protected int _id;
	protected AbstractAspectRegistry _registry;
	
	public AspectThisObjectMapper(int id, AbstractAspectRegistry registry) {
		_id = id;
		_registry = registry;
	}
	
	private WeakHashMap _objectAspects = new WeakHashMap();
	
	/**
	 * Get list of deployed aspect objects for which the advice has to be called
	 * 
	 * @return 			Iterator of aspect objects
	 */ 
	public Object[] $getInstances() {		
		DynArray lst = (DynArray)_objectAspects.get(_registry.$cj$joinpoint$this);
		if (lst == null)
			return null;
		else {
			return lst.toArray();
		}
	}
	
	/**
	 * Get container type
	 * 
	 * @return  Constant denoting container type
	 */
	public int $getContainerType() {
		return _id;
	}
	
	/**
	 * Check if there are no deployed objects
	 * 
	 * @return			If collection is empty
	 */
	public boolean isEmpty() {
		return _objectAspects.isEmpty();
	}
	
	/**
	 * Deploy the object on the key object. 
	 * Assumes correct usage for the sake of efficiency.
	 * 
	 * @param obj		Aspect object
	 * @param key		Key object
	 */
	public void deployObject(Object obj, Object key) {
		
		/* include the object to the list of deployed objects of the thread */
		DynArray lst = (DynArray)_objectAspects.get(key);
		
		if (lst == null)
		{
			lst = new DynArray();
			_objectAspects.put(key, lst);
		}
		
		lst.add(obj);
	}
	
	/**
	 * Undeploy the object from the key object 
	 * 
	 * @param obj		Aspect object
	 * @param key		Key object
	 */
	public void undeployObject(Object obj, Object key) {
		
		/* remove the object from the DynArray of deployed objects of the thread */
		DynArray lst = (DynArray)_objectAspects.get(key);
		
		if (lst != null) {
			lst.remove(obj);
			
			if (lst.isEmpty())	{
				_objectAspects.remove(key);
			}
		}
	}
	
	/**
	 * If there is fixed single instance, return it. 
	 * Return null otherwise
	 * 
	 * @return  Single deployed instance
	 */
	public Object getSingleInstance() {
		return null;
	}
}
