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
 * $Id: LineNumberInfo.java,v 1.3 2005-01-24 16:52:57 aracic Exp $
 */

package org.caesarj.classfile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * VMS 4.7.8: Line Number Table Attribute.
 *
 * This attribute represents a mapping between he Java Virtual Machine code
 * array and the line number in the original Java source file
 */
public class LineNumberInfo implements AccessorContainer {

  // --------------------------------------------------------------------
  // CONSTRUCTORS
  // --------------------------------------------------------------------

  /**
   * Create an entry in the line number table
   *
   * @param	line		the line number in the source code
   * @param	inst		the instruction where the line begins
   */
  public LineNumberInfo(short line, InstructionAccessor inst) {
    this.line = line;
    this.inst = inst;
  }

  /**
   * Create an entry in the line number table from a class file stream
   *
   * @param	in		the stream to read from
   * @param	cp		the constant pool
   * @param	insns		(sparse) array of instructions
   *
   * @exception	java.io.IOException	an io problem has occured
   */
  public LineNumberInfo(DataInput in, ConstantPool cp, Instruction[] insns)
    throws IOException
  {
    int	pos = in.readUnsignedShort();
    if (pos < insns.length) {
      this.inst = insns[pos];
    }
    this.line = in.readUnsignedShort();
  }

  // --------------------------------------------------------------------
  // ACCESSORS
  // --------------------------------------------------------------------

  /**
   * Transforms targets (deferences to actual instructions).
   */
  public void transformAccessors(AccessorTransformer transformer) throws BadAccessorException {
    this.inst = this.inst.transform(transformer, this);
  }

  /**
   * Returns the line number in the source code
   */
  public int getLine() {
    return line;
  }

  /**
   * Returns the instruction where the line begins
   */
  public InstructionAccessor getInstruction() {
    return inst;
  }

  /**
   * Sets the instruction where the line begins
   */
  public void setInstruction(InstructionAccessor inst) {
    this.inst = inst;
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
  /*package*/ void write(DataOutput out) throws IOException {
    Instruction		inst = (Instruction)this.inst;

    out.writeShort(inst.getAddress());
    out.writeShort((short)line);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private int				line;
  private InstructionAccessor		inst;
}
