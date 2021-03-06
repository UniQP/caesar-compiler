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
 * $Id: NameAndTypeConstant.java,v 1.2 2005-01-24 16:52:57 aracic Exp $
 */

package org.caesarj.classfile;

import java.io.DataOutput;
import java.io.IOException;

import org.caesarj.util.InconsistencyException;

/**
 * This constant represent a name and a type in the contant pool
 */
public class NameAndTypeConstant extends PooledConstant {

  // --------------------------------------------------------------------
  // CONSTRUCTORS
  // --------------------------------------------------------------------

  /**
   * Constructs a name/type constant pool entry.
   *
   * @param	name		the name of the symbol
   * @param	type		the signature of the symbol
   */
  public NameAndTypeConstant(String name, String type) {
    this(new AsciiConstant(name),
	 new AsciiConstant(type));
  }

  /**
   * Constructs a name/type constant pool entry.
   *
   * @param	name		the name of the symbol
   * @param	type		the signature of the symbol
   */
  public NameAndTypeConstant(AsciiConstant name, AsciiConstant type) {
    this.name = name;
    this.type = type;
  }

  // --------------------------------------------------------------------
  // ACCESSORS
  // --------------------------------------------------------------------

  /**
   * Returns the name of this constant
   */
  public String getName() {
    return name.getValue();
  }

  /**
   * Sets the name of this constant
   */
  public void setName(String name) {
    this.name.setValue(name);
  }

  /**
   * Returns the the type of this constant
   */
  public String getType() {
    return type.getValue();
  }

  /**
   * Returns the associated literal: this constant type has none
   */
  /*package*/ Object getLiteral() {
    throw new InconsistencyException("UNEXPECTED ACCESS TO LITERAL IN NON LITERAL CONSTANT");
  }

  // --------------------------------------------------------------------
  // POOLING
  // --------------------------------------------------------------------

  /**
   * hashCode (a fast comparison)
   * CONVENTION: return XXXXXXXXXXXX << 4 + Y
   * with Y = ident of the type of the pooled constant
   */
  public final int hashCode() {
    // ADD is not perfect, but...
    // we know already that name is an ascii: &
    return ((name.hashCode() + type.hashCode()) & 0xFFFFFFF0) + POO_NAT_CONSTANT;
  }

  /**
   * equals (an exact comparison)
   */
  public final boolean equals(Object o) {
    return (o instanceof NameAndTypeConstant) &&
      ((NameAndTypeConstant)o).name.equals(name) &&
      ((NameAndTypeConstant)o).type.equals(type);
  }

  // --------------------------------------------------------------------
  // WRITE
  // --------------------------------------------------------------------

  /**
   * Check location of constant value on constant pool
   *
   * @param	pc		the already in pooled constant
   */
  /*package*/ final void resolveConstants(PooledConstant pc) {
    setIndex(pc.getIndex());
    name.setIndex(((NameAndTypeConstant)pc).name.getIndex());
    type.setIndex(((NameAndTypeConstant)pc).type.getIndex());
  }

  /**
   * Insert or check location of constant value on constant pool
   *
   * @param	cp		the constant pool for this class
   */
  /*package*/ void resolveConstants(ConstantPool cp) throws ClassFileFormatException  {
    cp.addItem(name);
    cp.addItem(type);
  }

  /**
   * Write this class into the the file (out) getting data position from
   * the constant pool
   *
   * @param	cp		the constant pool that contain all data
   * @param	out		the file where to write this object info
   *
   * @exception	java.io.IOException	an io problem has occured
   */
  /*package*/ void write(ConstantPool cp, DataOutput out) throws IOException {
    out.writeByte(CST_NAMEANDTYPE);
    out.writeShort(name.getIndex());
    out.writeShort(type.getIndex());
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private AsciiConstant		name;
  private AsciiConstant		type;
}
