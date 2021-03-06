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
 * $Id: JLoopStatement.java,v 1.3 2005-01-24 16:52:59 aracic Exp $
 */

package org.caesarj.compiler.ast.phylum.statement;

import org.caesarj.compiler.ast.JavaStyleComment;
import org.caesarj.compiler.codegen.CodeLabel;
import org.caesarj.util.TokenReference;

/**
 * Loop Statement
 *
 * Root class for loop statement
 */
public abstract class JLoopStatement extends JStatement {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Construct a node in the parsing tree
   * @param	where		the line of this node in the source code
   * @param	cond		the expression to evaluate.
   * @param	body		the loop body.
   */
  public JLoopStatement(TokenReference where, JavaStyleComment[] comments) {
    super(where, comments);
  }

  // ----------------------------------------------------------------------
  // ACCESSORS
  // ----------------------------------------------------------------------

  /**
   * Return the end of this block (for break statement)
   */
  public CodeLabel getBreakLabel() {
    return endLabel;
  }

  /**
   * Return the beginning of this block (for continue statement)
   */
  public CodeLabel getContinueLabel() {
    return contLabel;
  }
  
  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private CodeLabel		contLabel = new CodeLabel();
  private CodeLabel		endLabel = new CodeLabel();
}
