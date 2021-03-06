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
 * $Id: Optimizer.java,v 1.2 2005-01-24 16:52:57 aracic Exp $
 */

package org.caesarj.compiler.ssa;

import org.caesarj.classfile.ClassInfo;
import org.caesarj.classfile.CodeInfo;
import org.caesarj.classfile.MethodInfo;

/**
 * This class is the entry point for the optimizer.
 */
public class Optimizer {

  /**
   * Reads, optimizes and writes a class file
   * @exception	UnpositionedError	an error occurred
   */
  public static void optimizeClass(ClassInfo info)  {
    MethodInfo[]	methods;
    
    methods = info.getMethods();
    for (int i = 0; i < methods.length; i++) {
      CodeInfo		code;

      code = methods[i].getCodeInfo();
      if (code != null) {
	MethodOptimizer methOptim = new MethodOptimizer(methods[i], code);
	code = methOptim.generateCode();
        methods[i].setCodeInfo(code);
      }
    }
  }
  /**
   * Reads, optimizes and writes a class file
   * @exception	UnpositionedError	an error occurred
   */
  public static MethodInfo optimize(MethodInfo info)  {
    CodeInfo		code;

    code = info.getCodeInfo();
    if (code != null) {
      MethodOptimizer methOptim = new MethodOptimizer(info, code);

      code = methOptim.generateCode();
      info.setCodeInfo(code);
    }
    return info;
  }
}
