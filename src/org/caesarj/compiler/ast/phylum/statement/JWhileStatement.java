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
 * $Id: JWhileStatement.java,v 1.5 2005-05-31 09:00:20 meffert Exp $
 */

package org.caesarj.compiler.ast.phylum.statement;

import org.caesarj.classfile.LocalVariableScope;
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
 * JLS 14.11: While Statement
 *
 * The while statement executes an expression and a statement repeatedly
 * until the value of the expression is false.
 */
public class JWhileStatement extends JLoopStatement {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Construct a node in the parsing tree
   * @param	where		the line of this node in the source code
   * @param	cond		the expression to evaluate.
   * @param	body		the loop body.
   */
  public JWhileStatement(TokenReference where,
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
      CLoopContext	condContext = new CLoopContext(context, context.getEnvironment(), this);
      TypeFactory       factory = context.getTypeFactory();

      cond = cond.analyse(new CExpressionContext(condContext,
                                                 context.getEnvironment()));
      condContext.close(getTokenReference());
      check(context,
	    cond.getType(factory) == factory.getPrimitiveType(TypeFactory.PRM_BOOLEAN),
	    KjcMessages.WHILE_COND_NOTBOOLEAN, cond.getType(factory));

      if (!cond.isConstant()) {
	CBodyContext	neverContext = context.cloneContext();
	CLoopContext	bodyContext = new CLoopContext(context, 
                                                       context.getEnvironment(), 
                                                       this);

	body.analyse(bodyContext);
	bodyContext.close(getTokenReference());

	context.merge(neverContext);
        context.setInLoop(false);
     } else {
	// JLS 14.20 Unreachable Statements :
	// The contained statement [of a while statement] is reachable iff the while
	// statement is reachable and the condition expression is not a constant
	// expression whose value is false.
	check(context, cond.booleanValue(), KjcMessages.STATEMENT_UNREACHABLE);

	if (cond instanceof JAssignmentExpression) {
	  context.reportTrouble(new CWarning(getTokenReference(),
					     KjcMessages.ASSIGNMENT_IN_CONDITION));
	}

	CLoopContext	bodyContext;

	bodyContext = new CLoopContext(context, context.getEnvironment(), this);
	body.analyse(bodyContext);
	if (bodyContext.isBreakTarget()) {
	  bodyContext.adopt(bodyContext.getBreakContextSummary());
	}
	bodyContext.close(getTokenReference());
	context.setReachable(bodyContext.isBreakTarget());
        context.setInLoop(false);
      }
    } catch (CBlockError e) {
      context.reportTrouble(e);
    }
  }

  // ----------------------------------------------------------------------
  // CODE GENERATION
  // ----------------------------------------------------------------------

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

    if (cond.isConstant() && cond.booleanValue()) {
      // while (true)
      code.plantLabel(getContinueLabel());			//	body:
      LocalVariableScope scope = new LocalVariableScope();
      code.pushLocalVariableScope(scope);
      body.genCode(context);					//		BODY CODE
      code.popLocalVariableScope(scope);
      code.plantJumpInstruction(opc_goto, getContinueLabel());		//		GOTO body
    } else {
      CodeLabel		bodyLabel = new CodeLabel();

      code.plantJumpInstruction(opc_goto, getContinueLabel());		//		GOTO cont
      code.plantLabel(bodyLabel);				//	body:
      LocalVariableScope scope = new LocalVariableScope();
      code.pushLocalVariableScope(scope);
      body.genCode(context);					//		BODY CODE
      code.popLocalVariableScope(scope);
      code.plantLabel(getContinueLabel());			//	cont:
      cond.genBranch(true, context, bodyLabel);			//		EXPR CODE; IFNE body
    }
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
