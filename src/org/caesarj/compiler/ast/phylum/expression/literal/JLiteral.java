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
 * $Id: JLiteral.java,v 1.2 2005-01-24 16:53:02 aracic Exp $
 */

package org.caesarj.compiler.ast.phylum.expression.literal;

import org.caesarj.compiler.ast.phylum.expression.*;
import org.caesarj.compiler.context.CExpressionContext;
import org.caesarj.util.PositionedError;
import org.caesarj.util.TokenReference;

/**
 * Root class for all literals expression
 */
public abstract class JLiteral extends JExpression {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Construct a node in the parsing tree
   * @param where the line of this node in the source code
   */
  public JLiteral(TokenReference where) {
    super(where);
  }

  /**
   * Constructs a real (float or double) literal
   * @param	where		the line of this node in the source code
   * @param	image		the textual representation of this literal
   */
  public static JLiteral parseReal(TokenReference where, String image)
    throws PositionedError
  {
    switch (image.charAt(image.length() - 1)) {
    case 'f':
    case 'F':
      return new JFloatLiteral(where, image.substring(0, image.length() - 1));

    case 'd':
    case 'D':
      return new JDoubleLiteral(where, image.substring(0, image.length() - 1));

    default:
      return new JDoubleLiteral(where, image);
    }
  }

  /**
   * Constructs an integer (int or long) literal
   * @param	where		the line of this node in the source code
   * @param	image		the textual representation of this literal
   */
  public static JLiteral parseInteger(TokenReference where, String image)
    throws PositionedError
  {
    switch (image.charAt(image.length() - 1)) {
    case 'l':
    case 'L':
      return new JLongLiteral(where, image.substring(0, image.length() - 1));

    default:
      return new JIntLiteral(where, image);
    }
  }

  // ----------------------------------------------------------------------
  // ACCESSORS
  // ----------------------------------------------------------------------

  /**
   * Tests whether this expression denotes a compile-time constant (JLS 15.28).
   *
   * @return	true iff this expression is constant
   */
  public boolean isConstant() {
    return true;
  }

  /**
   * Returns the literal value of an expression
   */
  public JLiteral getLiteral() {
    return this;
  }

  /**
   * Returns true iff the value of this literal is the
   * default value for this type (JLS 4.5.5).
   */
  public abstract boolean isDefault();

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
    return this;
  }
}
