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
 * $Id: JShiftExpression.java,v 1.3 2005-01-24 16:52:58 aracic Exp $
 */

package org.caesarj.compiler.ast.phylum.expression;

import org.caesarj.compiler.ast.phylum.expression.literal.JIntLiteral;
import org.caesarj.compiler.ast.phylum.expression.literal.JLongLiteral;
import org.caesarj.compiler.codegen.CodeSequence;
import org.caesarj.compiler.constants.KjcMessages;
import org.caesarj.compiler.context.CExpressionContext;
import org.caesarj.compiler.context.GenerationContext;
import org.caesarj.compiler.types.CNumericType;
import org.caesarj.compiler.types.CType;
import org.caesarj.compiler.types.TypeFactory;
import org.caesarj.util.InconsistencyException;
import org.caesarj.util.PositionedError;
import org.caesarj.util.TokenReference;
import org.caesarj.util.UnpositionedError;

/**
 * This class implements '+ - * /' specific operations
 * Plus operand may be String, numbers
 */
public class JShiftExpression extends JBinaryArithmeticExpression {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Construct a node in the parsing tree
   * This method is directly called by the parser
   * @param	where		the line of this node in the source code
   * @param	oper		the operator
   * @param	left		the left operand
   * @param	right		the right operand
   */
  public JShiftExpression(TokenReference where,
			  int oper,
			  JExpression left,
			  JExpression right)
  {
    super(where, left, right);
    this.oper = oper;
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

    left = left.analyse(context);
    right = right.analyse(context);

    try {
      type = computeType(context, left.getType(factory), right.getType(factory));
    } catch (UnpositionedError e) {
      throw e.addPosition(getTokenReference());
    }

    left = left.convertType(context, type);
    right = right.convertType(context, context.getTypeFactory().getPrimitiveType(TypeFactory.PRM_INT));

    if (left.isConstant() && right.isConstant()) {
      return constantFolding(factory);
    } else {
      return this;
    }
  }

  /**
   * compute the type of this expression according to operands
   * @param	leftType		the type of left operand
   * @param	rightType		the type of right operand
   * @return	the type computed for this binary operation
   * @exception	UnpositionedError	this error will be positioned soon
   */
  public static CType computeType(CExpressionContext context, 
                                  CType	leftType, 
                                  CType rightType) throws UnpositionedError {
    //if (rightType == factory.getPrimitiveType(TypeFactory.PRM_LONG))
    // check the spec  throw new UnpositionedError(KjcMessages.shift-badtype, leftType, rightType);

    if (leftType.isOrdinal() && rightType.isOrdinal()) {
      return leftType = CNumericType.unaryPromote(context, leftType);
    }

    throw new UnpositionedError(KjcMessages.SHIFT_BADTYPE, leftType, rightType);
  }

  /**
   * @param	left		the left literal
   * @param	right		the right literal
   * @return	a literal resulting of an operation over two literals
   */
  public JExpression constantFolding(TypeFactory factory) {
    if (left.getType(factory) == factory.getPrimitiveType(TypeFactory.PRM_LONG)) {
      long	result;

      if (right.getType(factory) == factory.getPrimitiveType(TypeFactory.PRM_LONG)) {
	result = compute(left.longValue(), (int)right.longValue());
      } else {
	result = compute(left.longValue(), right.intValue());
      }
      return new JLongLiteral(getTokenReference(), result);
    } else {
      int	result;

      if (right.getType(factory) == factory.getPrimitiveType(TypeFactory.PRM_LONG)) {
	result = compute(left.intValue(), (int)right.longValue());
      } else {
	result = compute(left.intValue(), right.intValue());
      }
      return new JIntLiteral(getTokenReference(), result);
    }
  }

  /**
   * Computes the result of the operation at compile-time (JLS 15.28).
   * @param	left		the first operand
   * @param	right		the seconds operand
   * @return	the result of the operation
   */
  public int compute(int left, int right) {
    switch (oper) {
    case OPE_SL:
      return left << right;
    case OPE_SR:
      return left >> right;
    case OPE_BSR:
      return left >>> right;
    default:
      throw new InconsistencyException();
    }
  }

  /**
   * Computes the result of the operation at compile-time (JLS 15.28).
   * @param	left		the first operand
   * @param	right		the seconds operand
   * @return	the result of the operation
   */
  public long compute(long left, int right) {
    switch (oper) {
    case OPE_SL:
      return left << right;
    case OPE_SR:
      return left >> right;
    case OPE_BSR:
      return left >>> right;
    default:
      throw new InconsistencyException();
    }
  }

  // ----------------------------------------------------------------------
  // CODE GENERATION
  // ----------------------------------------------------------------------

  /**
   * @param	type		the type of result
   * @return	the type of opcode for this operation
   */
  public static int getOpcode(int oper, CType type) {
    if (type.getTypeID() == TID_LONG) {
      switch (oper) {
      case OPE_SL:
	return opc_lshl;
      case OPE_SR:
	return opc_lshr;
      case OPE_BSR:
	return opc_lushr;
      }
    } else {
      switch (oper) {
      case OPE_SL:
	return opc_ishl;
      case OPE_SR:
	return opc_ishr;
      case OPE_BSR:
	return opc_iushr;
      }
    }
    return -1;
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

    left.genCode(context, false);
    right.genCode(context, false);
    code.plantNoArgInstruction(getOpcode(oper, getType(factory)));

    if (discardValue) {
      code.plantPopInstruction(getType(factory));
    }
  }

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  protected	int			oper;
}
