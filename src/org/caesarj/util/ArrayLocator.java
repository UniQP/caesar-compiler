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
 * $Id: ArrayLocator.java,v 1.6 2005-01-24 16:52:58 aracic Exp $
 */

package org.caesarj.util;

import java.util.Hashtable;

/**
 * This class allows to find the position of an object in an array.
 */
public class ArrayLocator {

  /**
   * Constructs a new ArrayLocator object.
   */
  public ArrayLocator(Object[] array) {
    if (array.length < MIN_HASH) {
      this.array = array;
    } else {
      System.err.println("WARNING HASH DOES NOT WORK");
      this.hashed = new Hashtable(array.length + 1, 1.0f);
      for (int i = 0; i < array.length; i++) {
	this.hashed.put(array[i], new Integer(i));
      }
    }
  }

  /**
   * Returns the index of the specified object in the array, or -1
   * if the object cannot be found.
   * !!! TEST: linear search
   */
  public int getIndex(Object object) {
    if (this.array != null) {
      for (int i = 0; i < array.length; i++) {
	if (object == array[i]) {
	  return i;
	}
      }
      return -1;
    } else {
      Object	index = hashed.get(object);

      if (index == null) {
	return -1;
      } else {
	return ((Integer)index).intValue();
      }
    }
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private static final int	MIN_HASH = Integer.MAX_VALUE; // Hashtable does not work !!!
  // The equals method differs from == !!!
  private Object[]		array;
  private Hashtable		hashed;
}
