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
 * $Id: CTryFinallyContext.java,v 1.3 2005-01-24 16:52:58 aracic Exp $
 */

package org.caesarj.compiler.context;

import java.util.ArrayList;

import org.caesarj.compiler.KjcEnvironment;
import org.caesarj.compiler.ast.phylum.statement.JStatement;
import org.caesarj.util.TokenReference;

/**
 * This class represents a local context during checkBody
 * It follows the control flow and maintain informations about
 * variable (initialised, used, allocated), exceptions (thrown, catched)
 * It also verify that context is still reachable
 *
 * There is a set of utilities method to access fields, methods and class
 * with the name by clamping the parsing tree
 * @see CCompilationUnitContext
 * @see CClassContext
 * @see CMethodContext
 * @see CContext
 */
public class CTryFinallyContext extends CBlockContext {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Construct a block context, it supports local variable allocation
   * throw statement and return statement
   * @param	parent		the parent context, it must be different
   *				than null except if called by the top level
   */
  public CTryFinallyContext(CBodyContext parent, KjcEnvironment environment) {
    super(parent, environment);
    clearThrowables();
  }

  public void close(TokenReference ref) {
  }

  /**
   *
   */
  public void forwardBreaksAndContinues() {
    CBodyContext	parent = (CBodyContext)getParentContext();

    if (breaks != null) {
      for (int i = 0; i < breaks.size(); i++) {
	Object[]	elem = (Object[])breaks.get(i);

	parent.addBreak((JStatement)elem[0], (CBodyContext)elem[1]);
      }
    }
    if (continues != null) {
      for (int i = 0; i < continues.size(); i++) {
	Object[]	elem = (Object[])continues.get(i);

	parent.addContinue((JStatement)elem[0], (CBodyContext)elem[1]);
      }
    }
  }

  /**
   *
   * @param	target		the target of the break statement
   * @param	context		the context at the break statement
   */
  protected void addBreak(JStatement target,
			  CBodyContext context)
  {
    if (breaks == null) {
      breaks = new ArrayList();
    }
    //!!! FIXME clone context ???
    breaks.add(new Object[]{ target, context });
  }

  /**
   *
   * @param	target		the target of the continue statement
   * @param	context		the context at the continue statement
   */
  protected void addContinue(JStatement target,
			     CBodyContext context)
  {
    if (continues == null) {
      continues = new ArrayList();
    }
    //!!! FIXME clone context ???
    continues.add(new Object[]{ target, context });
  }

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private ArrayList	breaks;
  private ArrayList	continues;
}
