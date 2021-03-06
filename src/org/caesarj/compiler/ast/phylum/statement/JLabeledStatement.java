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
 * $Id: JLabeledStatement.java,v 1.3 2005-01-24 16:52:59 aracic Exp $
 */

package org.caesarj.compiler.ast.phylum.statement;

import org.caesarj.compiler.ast.JavaStyleComment;
import org.caesarj.compiler.ast.visitor.IVisitor;
import org.caesarj.compiler.codegen.CodeLabel;
import org.caesarj.compiler.codegen.CodeSequence;
import org.caesarj.compiler.constants.KjcMessages;
import org.caesarj.compiler.context.CBodyContext;
import org.caesarj.compiler.context.CLabeledContext;
import org.caesarj.compiler.context.GenerationContext;
import org.caesarj.util.PositionedError;
import org.caesarj.util.TokenReference;

/**
 * JLS 14.7: Labeled Statement
 *
 * Statements may have label prefixes.
 */
public class JLabeledStatement extends JStatement {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Construct a node in the parsing tree
   * @param	where		the line of this node in the source code
   * @param	label		the label of the enclosing labeled statement
   * @param	body		the contained statement
   * @param	comments	comments in the source text
   */
  public JLabeledStatement(TokenReference where,
			   String label,
			   JStatement body,
			   JavaStyleComment[] comments)
  {
    super(where, comments);

    this.label = label;
    this.body = body;
  }

  // ----------------------------------------------------------------------
  // ACCESSORS
  // ----------------------------------------------------------------------

  /**
   * Returns the label of this statement.
   */
  public String getLabel() {
    return label;
  }

  // ----------------------------------------------------------------------
  // SEMANTIC ANALYSIS
  // ----------------------------------------------------------------------

  /**
   * Analyses the statement (semantically).
   * @param	context		the analysis context
   * @exception	PositionedError	the analysis detected an error
   */
  public void analyse(CBodyContext context) throws PositionedError {
    check(context,
	  context.getLabeledStatement(label) == null,
	  KjcMessages.LABEL_ALREADY_EXISTS, label);

    CLabeledContext	labeledContext;

    labeledContext = new CLabeledContext(context, context.getEnvironment(), this);
    body.analyse(labeledContext);
    labeledContext.close(getTokenReference());
  }

  // ----------------------------------------------------------------------
  // BREAK/CONTINUE HANDLING
  // ----------------------------------------------------------------------

  /**
   * Returns the actual target statement of a break or continue whose
   * label is the label of this statement.
   *
   * If the statement referencing this labeled statement is either a break
   * or a continue statement :
   * - if it is a continue statement, the target is the contained statement
   *   which must be a loop statement.
   * - if it is a break statement, the target is the labeled statement
   *   itself ; however, if the contained statement is a loop statement,
   *   the target address for a break of the contained statement is the same
   *   as the target address of this labeled statement.
   * Thus, if the contained statement is a loop statement, the target
   * for a break or continue to this labeled statement is the same as the
   * target address of this labeled statement.
   */
  public JStatement getTargetStatement() {
    if (body instanceof JLoopStatement) {
      // JLS 14.15: do, while or for statement
      return body;
    } else {
      return this;
    }
  }

  // ----------------------------------------------------------------------
  // CODE GENERATION
  // ----------------------------------------------------------------------

  /**
   * Accepts the specified visitor
   * @param	p		the visitor
   */
  public void recurse(IVisitor s) {
    body.accept(s);
  }

  /**
   * Generates a sequence of bytescodes
   * @param	code		the code list
   */
  public void genCode(GenerationContext context) {
    CodeSequence code = context.getCodeSequence();

    setLineNumber(code);

    endLabel = new CodeLabel();
    body.genCode(context);
    code.plantLabel(endLabel);
    endLabel = null;
  }

  /**
   * Returns the end of this block (for break statement).
   */
  public CodeLabel getBreakLabel() {
    return endLabel;
  }

  public JStatement getBody() {return body;}
  
  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private String		label;
  private JStatement		body;
  private CodeLabel		endLabel;
}
