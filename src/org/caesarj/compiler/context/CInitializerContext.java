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
 * $Id: CInitializerContext.java,v 1.5 2005-02-11 18:45:22 aracic Exp $
 */

package org.caesarj.compiler.context;

import org.caesarj.compiler.KjcEnvironment;
import org.caesarj.compiler.ast.phylum.declaration.JMethodDeclaration;
import org.caesarj.util.PositionedError;
import org.caesarj.util.TokenReference;
import org.caesarj.util.UnpositionedError;

/**
 * This class represents a method context during check
 * @see CCompilationUnitContext
 * @see CClassContext
 * @see CMethodContext
 * @see CContext
 */
public class CInitializerContext extends CMethodContext {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * CInitializerContext
   * @param	parent		the parent context
   * @param	self		the corresponding method interface
   */
  public CInitializerContext(CClassContext parent, KjcEnvironment environment, JMethodDeclaration decl) {
    super(parent, environment, decl);
  }

  /**
   * Verify that all checked exceptions are defined in the throw list
   * @exception	UnpositionedError	this error will be positioned soon
   */
  public void close(TokenReference ref) throws PositionedError {
    if (! getCMethod().isStatic()) {
      ((CClassContext)parent).setInitializerInfo(fieldInfo);
      // 25.09.01 throws in initializer
      //      if (getCMethod().getOwner().isAnonymous()) {
        getCMethod().setThrowables(getThrowables());
        //      }
      adoptFieldInfos((CClassContext)parent);
    } else {
      adoptFieldInfos((CClassContext)parent);
    }

    super.close(ref);
  }

  public void adoptFieldInfos(CClassContext target) {
    int		parentPosition = target.getCClass().getFieldCount();

    if (fieldInfo != null) {
      for (int i = 0; i < parentPosition; i++) {
	int	info = getFieldInfo(i);
	
	if (info != 0) {
	  target.setFieldInfo(i, info);
	}
      }
    }
  }

  // ----------------------------------------------------------------------
  // FIELD STATE
  // ----------------------------------------------------------------------

  /**
   * @param	var		the definition of a field
   * @return	all informations we have about this field
   */
  public int getFieldInfo(int index) {
    if (fieldInfo == null) {
      return parent.getFieldInfo(index);
    } else {
      return fieldInfo.getInfo(index);
    }
  }

  /**
   * @param	index		The field position in method array of local vars
   * @param	info		The information to add
   *
   * We make it a local copy of this information and at the end of this context
   * we will transfert it to the parent context according to controlFlow
   */
  public void setFieldInfo(int index, int info) {
    if (fieldInfo == null) {
      fieldInfo = (CVariableInfo)getFieldInfo().clone();
    }
    fieldInfo.setInfo(index, info);
  }

  
public CInitializerContext getInitializerContext() {
    return this;
}
  
  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private CVariableInfo		fieldInfo;
}
