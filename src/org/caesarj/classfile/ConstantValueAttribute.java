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
 * $Id: ConstantValueAttribute.java,v 1.5 2005-01-24 16:52:57 aracic Exp $
 */

package org.caesarj.classfile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.caesarj.util.InconsistencyException;

/**
 * VMS 4.7.2: Constant Value Attribute.
 *
 * A ConstantValue attribute represents the value of a constant field that
 * must be (explicitly or implicitly) static
 */
public class ConstantValueAttribute extends Attribute {

  // --------------------------------------------------------------------
  // CONSTRUCTORS
  // --------------------------------------------------------------------

  /**
   * Create a new constant attribute whose constant value
   * is picked up from constant pool with the given entry.
   *
   * @param	value		the value
   */
  public ConstantValueAttribute(Object value) {
    if (value instanceof Integer) {
      this.value = new IntegerConstant(((Integer)value).intValue());
    } else if (value instanceof Float) {
      this.value = new FloatConstant(((Float)value).floatValue());
    } else if (value instanceof Double) {
      this.value = new DoubleConstant(((Double)value).doubleValue());
    } else if (value instanceof Long) {
      this.value = new LongConstant(((Long)value).longValue());
    } else if (value instanceof String) {
      this.value = new StringConstant((String)value);
    } else {
      throw new InconsistencyException("bad object type " + value.getClass());
    }
  }

  /**
   * Constructs a constant value attribute from a class file stream.
   *
   * @param	in		the stream to read from
   * @param	cp		the constant pool
   *
   * @exception	java.io.IOException	an io problem has occured
   * @exception	ClassFileFormatException	attempt to
   *					write a bad classfile info
   */
  public ConstantValueAttribute(DataInput in, ConstantPool cp)
    throws IOException, ClassFileFormatException
  {
    if (in.readInt() != 2) {
      throw new ClassFileFormatException("bad attribute length");
    }
    this.value = cp.getEntryAt(in.readUnsignedShort());
  }

  // --------------------------------------------------------------------
  // ACCESSORS
  // --------------------------------------------------------------------

  /**
   * Returns the attribute's tag
   */
  /*package*/ public int getTag() {
    return ClassfileConstants2.ATT_CONSTANTVALUE;
  }

  /**
   * Returns the space in bytes used by this attribute in the classfile
   */
  /*package*/ protected int getSize() {
    return 2 + 4 + 2;
  }

  /**
   * Returns the value of the constant value attribute
   */
  /*package*/ Object getLiteral() {
    return value.getLiteral();
  }

  // --------------------------------------------------------------------
  // WRITE
  // --------------------------------------------------------------------

  /**
   * Insert or check location of constant value on constant pool
   *
   * @param	cp		the constant pool for this class
   */
  /*package*/ protected void resolveConstants(ConstantPool cp)  throws ClassFileFormatException {
    cp.addItem(attr);
    cp.addItem(value);
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
  /*package*/ protected void write(ConstantPool cp, DataOutput out) throws IOException {
    out.writeShort(attr.getIndex());
    out.writeInt(2);
    out.writeShort(value.getIndex());
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private static AsciiConstant		attr = new AsciiConstant("ConstantValue");
  private PooledConstant		value;
}
