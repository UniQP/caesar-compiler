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
 * $Id: JDoStatement.java,v 1.3 2005-01-24 16:52:59 aracic Exp $
 */

package org.caesarj.compiler.ast.phylum.statement;

import org.caesarj.compiler.ast.CBlockError;
import org.caesarj.compiler.ast.JavaStyleComment;
import org.caesarj.compiler.ast.phylum.expression.JAssignmentExpression;
import org.caesarj.compiler.ast.phylum.expression.JExpression;
import org.caesarj.compiler.ast.visitor.IVisitor;
import org.caesarj.compiler.codegen.CodeLabel;
import org.caesarj.compiler.codegen.CodeSequence;
import org.caesarj.compiler.constants.KjcMessages;
import org.caesarj.compiler.context.CBodyContext;
import org.caesarj.compiler.context.CExpressionContext;
import org.caesarj.compiler.context.CLoopContext;
import org.caesarj.compiler.context.GenerationContext;
import org.caesarj.compiler.types.TypeFactory;
import org.caesarj.util.CWarning;
import org.caesarj.util.PositionedError;
import org.caesarj.util.TokenReference;

/**
 * JLS 14.12: Do Statement
 *
 * The do statement executes an expression and a statement repeatedly
 * until the value of the expression is false.
 */
public class JDoStatement extends JLoopStatement {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Construct a node in the parsing tree
   * @param	where		the line of this node in the source code
   * @param	cond		the expression to evaluate
   * @param	body		the loop body
   * @param	comments	comments in the source text
   */
  public JDoStatement(TokenReference where,
		      JExpression cond,
		      JStatement body,
		      JavaStyleComment[] comments)
  {
    super(where, comments);

    this.cond = cond;
    this.body = body;
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
    try {
      CLoopContext	loopContext;
      TypeFactory       factory = context.getTypeFactory();

      loopContext = new CLoopContext(context, context.getEnvironment(), this);

      body.analyse(loopContext);

      if (loopContext.isContinueTarget()) {
	if (loopContext.isReachable()) {
	  loopContext.merge(loopContext.getContinueContextSummary());
	} else {
	  loopContext.adopt(loopContext.getContinueContextSummary());
	  loopContext.setReachable(true);
	}
      }

      cond = cond.analyse(new CExpressionContext(loopContext, context.getEnvironment()));
      check(loopContext,
	    cond.getType(factory) == factory.getPrimitiveType(TypeFactory.PRM_BOOLEAN),
	    KjcMessages.DO_COND_NOTBOOLEAN, cond.getType(factory));
      if (cond instanceof JAssignmentExpression) {
	loopContext.reportTrouble(new CWarning(getTokenReference(),
					       KjcMessages.ASSIGNMENT_IN_CONDITION));
      }

      loopContext.close(getTokenReference());

      if (cond.isConstant() && cond.booleanValue()) {
	context.setReachable(false);
      }

      if (loopContext.isBreakTarget()) {
	if (context.isReachable()) {
	  context.merge(loopContext.getBreakContextSummary());
	} else {
	  context.adopt(loopContext.getBreakContextSummary());
	}
      }
    } catch (CBlockError e) {
      context.reportTrouble(e);
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
    cond.accept(s);
    body.accept(s);
  }

  /**
   * Generates a sequence of bytescodes
   * @param	code		the code list
   */
  public void genCode(GenerationContext context) {
    CodeSequence code = context.getCodeSequence();

    setLineNumber(code);

    code.pushContext(this);

    CodeLabel		bodyLabel = new CodeLabel();

    code.plantLabel(bodyLabel);				//	body:
    body.genCode(context);					//		BODY CODE
    code.plantLabel(getContinueLabel());			//	cont:
    cond.genBranch(true, context, bodyLabel);		//		EXPR CODE; IFNE body
    code.plantLabel(getBreakLabel());				//	end:	...

    code.popContext(this);
  }

  public JExpression getCondition() {return cond;}
  public JStatement getBody() {return body;}
  
  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private JExpression		cond;
  private JStatement		body;
}
