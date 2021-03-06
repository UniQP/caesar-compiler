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
 * $Id: ClassReader.java,v 1.4 2005-04-20 19:34:21 gasiunas Exp $
 */

package org.caesarj.compiler;

import org.caesarj.compiler.export.CCjSourceClass;
import org.caesarj.compiler.export.CClass;
import org.caesarj.compiler.export.CSourceClass;
import org.caesarj.compiler.types.SignatureParser;
import org.caesarj.compiler.types.TypeFactory;

//import org.caesarj.at.dms.compiler.UnpositionedError;

/**
 * Interface for Java-Classfile readers.
 */
public interface ClassReader {

  /**
   * Loads class definition from .class file
   */
  CClass loadClass(TypeFactory typeFactory, String name);

  /**
   * @return  false if name exists for source class as source class
   *          in an other file
   * @param CClass a class to add (must be a CSourceClass)
   */
  boolean addSourceClass(CSourceClass cl);

  /**
   * @return a class file that contain the class named name
   * @param name the name of the class file
   */
  boolean hasClassFile(String name);

  SignatureParser getSignatureParser();
 
  /**
   * Returns ture iff the specified package exists in the classpath
   *
   * @param	name		the name of the package
   */
  boolean packageExists(String name);
  
  /**
   * Try to find source class for given fully qualified class name
   */
  public CCjSourceClass findSourceClass(String name);
}
