/*
 * Copyright (C) 1990-2001 DMS Decision Management Systems Ges.m.b.H.
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
 * $Id: JCastExpression.java,v 1.2 2004-02-08 20:27:58 ostermann Exp $
 */

package org.caesarj.compiler.ast;

import org.caesarj.compiler.codegen.CodeSequence;
import org.caesarj.compiler.constants.KjcMessages;
import org.caesarj.compiler.context.CExpressionContext;
import org.caesarj.compiler.context.GenerationContext;
import org.caesarj.compiler.types.CNumericType;
import org.caesarj.compiler.types.CReferenceType;
import org.caesarj.compiler.types.CType;
import org.caesarj.compiler.types.TypeFactory;
import org.caesarj.util.CWarning;
import org.caesarj.util.PositionedError;
import org.caesarj.util.TokenReference;
import org.caesarj.util.UnpositionedError;

/**
 * This class represents a cast expression '((byte)2)'
 */
public class JCastExpression extends JExpression {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Constructs a node in the parsing tree.
   * This method is directly called by the parser.
   * @param	where	the line of this node in the source code
   * @param	expr	the expression to be casted
   * @param	dest	the type of this expression after cast
   */
  public JCastExpression(TokenReference where, JExpression expr, CType dest) {
    super(where);
    this.expr = expr;
    this.dest = dest;
  }

  // ----------------------------------------------------------------------
  // ACCESSORS
  // ----------------------------------------------------------------------

  /**
   * Compute the type of this expression.
   *
   * @return the type of this expression
   */
  public CType getType(TypeFactory factory) {
    return dest;
  }
  
  /**
   * WAlTER NEW
   *
   * @return the type of this expression
   */
  public void setType(CType type) {
	dest = type;
  }

  // ----------------------------------------------------------------------
  // SEMANTIC ANALYSIS
  // ----------------------------------------------------------------------

  /**
   * Analyses the expression (semantically).
   * @param	context		the analysis context
   * @return	an equivalent, analysed expression
   * @exception	PositionedError	the analysis detected an error
   */
  public JExpression analyse(CExpressionContext context) throws PositionedError {
    TypeFactory         factory = context.getTypeFactory();

    expr = expr.analyse(context);

    try {
      dest = dest.checkType(context);
    } catch (UnpositionedError e) {
      throw e.addPosition(getTokenReference());
    }

    check(context, expr.getType(factory).isCastableTo(dest), KjcMessages.CAST_CANT, expr.getType(factory), dest);

    if (!expr.getType(factory).isPrimitive() 
        && expr.getType(factory).isAssignableTo(context, dest) 
        && expr.getType(factory) != context.getTypeFactory().getNullType()) {
      context.reportTrouble(new CWarning(getTokenReference(), KjcMessages.UNNECESSARY_CAST, expr.getType(factory), dest));
    }

    if (expr.isConstant() /*&& expr.getType(factory).isPrimitive() */) {
      return expr.convertType(context, dest);
    }

    if (!dest.isAssignableTo(context, expr.getType(factory))) {
      return expr.convertType(context, dest);
    }

    return this;
  }

  // ----------------------------------------------------------------------
  // CODE GENERATION
  // ----------------------------------------------------------------------

  /**
   * Accepts the specified visitor
   * @param	p		the visitor
   */
  public void accept(KjcVisitor p) {
    p.visitCastExpression(this, expr, dest);
  }

  /**
   * Generates JVM bytecode to evaluate this expression.
   *
   * @param	code		the bytecode sequence
   * @param	discardValue	discard the result of the evaluation ?
   */
  public void genCode(GenerationContext context, boolean discardValue) {
    CodeSequence        code = context.getCodeSequence();
    TypeFactory         factory = context.getTypeFactory();

    setLineNumber(code);

    expr.genCode(context, false);

    if (dest.isNumeric()) {
      ((CNumericType)expr.getType(factory)).genCastTo((CNumericType)dest, context);
    } else if (dest instanceof CReferenceType) {
      code.plantClassRefInstruction(opc_checkcast, ((CReferenceType)dest).getQualifiedName());
    }

    if (discardValue) {
      code.plantPopInstruction(dest);
    }
  }

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  protected JExpression		expr;
  protected CType		dest;
}