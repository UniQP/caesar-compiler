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
 * $Id: JLocalVariable.java,v 1.6 2005-01-27 15:20:11 aracic Exp $
 */

package org.caesarj.compiler.ast.phylum.variable;

import org.caesarj.compiler.ast.phylum.JPhylum;
import org.caesarj.compiler.ast.phylum.expression.JExpression;
import org.caesarj.compiler.ast.visitor.IVisitor;
import org.caesarj.compiler.codegen.CodeSequence;
import org.caesarj.compiler.constants.KjcMessages;
import org.caesarj.compiler.context.CBodyContext;
import org.caesarj.compiler.context.GenerationContext;
import org.caesarj.compiler.types.CType;
import org.caesarj.util.CWarning;
import org.caesarj.util.TokenReference;

/**
 * This class represents a local variable declaration
 */
public abstract class JLocalVariable extends JPhylum {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Constructs a local variable definition
   * @param	where		the line of this node in the source code
   * @param	modifiers	the modifiers of this variable
   * @param	kind		the kind of this variable
   * @param	type		the type of this variable
   * @param	name		the name of this variable
   * @param	expr		the initial value
   */
  public JLocalVariable(TokenReference where,
			int modifiers,
			int kind,
			CType type,
			String name,
			JExpression expr)
  {
    super(where);

    this.modifiers = modifiers;
    this.kind = kind;
    this.type = type;
    this.name = name;
    this.expr = expr;
  }

  // ----------------------------------------------------------------------
  // ACCESSORS
  // ----------------------------------------------------------------------

  /**
   * Returns if this variable is final
   */
  public boolean isFinal() {
    return (getModifiers() & ACC_FINAL) != 0;
  }

  /**
   * Returns if this variable is static
   */
  public boolean isStatic() {
    return (getModifiers() & ACC_STATIC) != 0;
  }

  /**
   * Returns the variable definied by this formal parameter
   */
  public boolean isGenerated() {
    return (getDescription() & DES_GENERATED) != 0;
  }

  /**
   * @return the modifiers of this variable
   */
  public int getModifiers() {
    return modifiers;
  }

  /**
   * @return the name of this variable
   */
  public String getIdent() {
    return name;
  }

  /**
   * @return the type of this variable
   */
  public CType getType() {
    return type;
  }

  /**
   * @return	the value of this local variable at initialization
   * $$$ why not dynamically...
   */
  public JExpression getValue() {
    return expr;
  }

  /**
   * Tests whether this expression denotes a compile-time constant (JLS 15.28).
   *
   * @return	true iff this expression is constant
   */
  public boolean isConstant() {
    return isFinal() && expr != null && expr.isConstant();
  }

  /**
   * @return the local index in context variable table
   */
  public int getIndex() {
    return index;
  }

  /**
   * @return the local index in context variable table
   */
  public void setIndex(int index) {
    this.index = index;
  }

  public int getDescription() {
    return kind;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public void setUsed() {
    used = true;
  }

  public boolean isUsed() {
    return used;
  }

  public void setAssigned(TokenReference ref, CBodyContext context) {
    assigned = true;
    if (isLoopVariable()) {
      context.reportTrouble(new CWarning(ref,
					 KjcMessages.ASSIGNS_LOOP_VARIABLE,
					 getIdent()));
    }
  }

  public boolean isAssigned() {
    return assigned;
  }

  public void setIsLoopVariable() {
    loopVariable = true;
  }

  public void unsetIsLoopVariable() {
    loopVariable = false;
  }

  public boolean isLoopVariable() {
    return loopVariable;
  }

  public boolean equals(Object o) {
    return o instanceof JLocalVariable &&
      index == ((JLocalVariable)o).index;
  }

  // ----------------------------------------------------------------------
  // CODE GENERATION
  // ----------------------------------------------------------------------

  /**
   * Generates a sequence of bytecode to load
   * the value of the variable on the stack
   * @param	code		the code sequence
   */
  public void genLoad(GenerationContext context) {
    CodeSequence code = context.getCodeSequence();

    code.plantLocalVar(type.getLoadOpcode(), this);
  }

  /**
   * Generates a sequence of bytescodes
   * @param	code		the code sequence
   */
  public void genStore(GenerationContext context) {
    CodeSequence code = context.getCodeSequence();

    code.plantLocalVar(type.getStoreOpcode(), this);
  }

  public void setType(CType type) {
      this.type = type;
  }
  
  public void recurse(IVisitor s) {
    if(expr != null) expr.accept(s);
  }
  
  // ----------------------------------------------------------------------
  // PUBLIC CONSTANTS
  // ----------------------------------------------------------------------

  public static final int	DES_LOCAL_VAR		= 1 << 0;
  public static final int	DES_PARAMETER		= 1 << 1;
  public static final int	DES_CATCH_PARAMETER	= 1 << 2;
  public static final int	DES_GENERATED		= 1 << 3;

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private int			kind;
  private boolean		used;
  private boolean		assigned;
  private boolean		loopVariable;
  protected int			modifiers;
  protected String		name;
  protected CType		type;
  protected JExpression		expr;

  // index in block local vars array
  private int			index;

  // position in method local vars
  private int			position;
  
  
  public String toString() {
    return "JLocalVariable["+name+":"+type+"]";
  }
}