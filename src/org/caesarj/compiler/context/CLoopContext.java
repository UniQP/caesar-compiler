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
 * $Id: CLoopContext.java,v 1.3 2005-01-24 16:52:58 aracic Exp $
 */

package org.caesarj.compiler.context;

import org.caesarj.compiler.KjcEnvironment;
import org.caesarj.compiler.ast.phylum.statement.JLoopStatement;
import org.caesarj.compiler.ast.phylum.statement.JStatement;

/**
 * This class provides the contextual information for the semantic
 * analysis loop statements.
 */
public class CLoopContext extends CBodyContext {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Constructs the context to analyse a loop statement semantically.
   * @param	parent		the parent context
   * @param	stmt		the loop statement
   */
  public CLoopContext(CBodyContext parent, KjcEnvironment environment,JLoopStatement stmt) {
    super(parent, environment);

    this.stmt = stmt;

    setInLoop(true);
  }

  // ----------------------------------------------------------------------
  // ACCESSORS
  // ----------------------------------------------------------------------

  /**
   * Returns the innermost statement which can be target of a break
   * statement without label.
   */
  public JStatement getNearestBreakableStatement() {
    return stmt;
  }

  /**
   * Returns the innermost statement which can be target of a continue
   * statement without label.
   */
  public JStatement getNearestContinuableStatement() {
    return stmt;
  }

  /**
   *
   */
  protected void addBreak(JStatement target,
			  CBodyContext context)
  {
    if (stmt == target) {
      if (breakContextSummary == null) {
	breakContextSummary = context.cloneContext();
      } else {
	breakContextSummary.merge(context);
      }
      breakContextSummary.setReachable(true);
    } else {
      ((CBodyContext)getParentContext()).addBreak(target, context);
    }
  }

  /**
   *
   */
  protected void addContinue(JStatement target,
			     CBodyContext context)
  {
    if (stmt == target) {
      if (continueContextSummary == null) {
	continueContextSummary = context.cloneContext();
      } else {
	continueContextSummary.merge(context);
      }
    } else {
      ((CBodyContext)getParentContext()).addContinue(target, context);
    }
  }

  /**
   * Checks whether this statement is target of a break statement.
   *
   * @return	true iff this statement is target of a break statement.
   */
  public boolean isBreakTarget() {
    return breakContextSummary != null;
  }

  /**
   * Returns the context state after break statements.
   */
  public CBodyContext getBreakContextSummary() {
    return breakContextSummary;
  }

  /**
   * Checks whether this statement is target of a continue statement.
   *
   * @return	true iff this statement is target of a continue statement.
   */
  public boolean isContinueTarget() {
    return continueContextSummary != null;
  }

  /**
   * Returns the context state after continue statements.
   */
  public CBodyContext getContinueContextSummary() {
    return continueContextSummary;
  }



  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private final JLoopStatement		stmt;
  private CBodyContext			breakContextSummary;
  private CBodyContext			continueContextSummary;
}
