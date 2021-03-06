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
 * $Id: MethodInfo.java,v 1.5 2005-01-24 16:52:57 aracic Exp $
 */

package org.caesarj.classfile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.caesarj.util.InconsistencyException;

/**
 * VMS 4.6: Methods.
 *
 * Each method, and each (class or instance) initialization method is
 * described by this structure.
 */
public class MethodInfo extends Member {

  // --------------------------------------------------------------------
  // CONSTRUCTORS
  // --------------------------------------------------------------------

  /**
   * Constructs a method entry
   *
   * @param	modifiers	access permission to and properties of the method
   * @param	name		the simple name of the method (or <init> or <clinit>)
   * @param	type		the method signature
   * @param	exceptions	the checked exceptions the method may throw
   * @param	code		the virtual machine instructions and auxiliary infos
   * @param	deprecated	is this method deprecated ?
   * @param	synthetic	is this method synthesized by the compiler ?
   *
   * NOTE:
   * VMS 4.7.4: There must be exactly one Exceptions attribute in each method_info structure.
   */
  public MethodInfo(short modifiers,
		    String name,
		    String type,
                    String genericSignature,
		    String[] exceptions,
		    CodeInfo code,
		    boolean deprecated,
		    boolean synthetic) {
    super(modifiers);
    this.name = new AsciiConstant(name);
    this.type = new AsciiConstant(type);
    if (code != null && code.getParameterCount() == -1) {
      code.setParameterCount(getParameterCount());
    }

    this.attributes = new AttributeList(code,
					exceptions != null && exceptions.length != 0 ? new ExceptionsAttribute(exceptions) : null,
					null,
					synthetic ? new SyntheticAttribute() : null);
    if (deprecated) {
      attributes.add( new DeprecatedAttribute());
    }
  }

  /**
   * Constructs a method entry from a class file stream.
   *
   * @param	in		the stream to read from
   * @param	cp		the constant pool
   * @param	interfaceOnly	load only the interface, not the source code
   *
   * @exception	IOException	an io problem has occured
   * @exception	ClassFileFormatException	attempt to read a bad classfile
   */
  public MethodInfo(DataInput in, ConstantPool cp, boolean interfaceOnly)
    throws IOException, ClassFileFormatException
  {
    setModifiers((short)in.readUnsignedShort());
    this.name = (AsciiConstant)cp.getEntryAt(in.readUnsignedShort());
    this.type = (AsciiConstant)cp.getEntryAt(in.readUnsignedShort());
    this.attributes = new AttributeList(in, cp, interfaceOnly);

    CodeInfo	code = getCodeInfo();
    if (code != null && code.getParameterCount() == -1) {
      code.setParameterCount(getParameterCount());
    }
  }

  // --------------------------------------------------------------------
  // ACCESSORS
  // --------------------------------------------------------------------

  /**
   * Returns the name of this method
   */
  public String getName() {
    return name.getValue();
  }

  /**
   * Sets the name of the this method
   */
  public void setName(String name) {
    this.name = new AsciiConstant(name);
  }

  /**
   * Returns the type of the this method
   */
  public String getSignature() {
    return type.getValue();
  }

  /**
   * Returns the type of the this method
   */
  public void setSignature(String type) {
    this.type = new AsciiConstant(type);
  }

  /**
   * Returns the exceptions of this method
   */
  public String[] getExceptions() {
    Attribute		attr = attributes.get(ClassfileConstants2.ATT_EXCEPTIONS);

    return attr == null ? null : ((ExceptionsAttribute)attr).getExceptions();
  }

  /**
   * Sets the exceptions
   */
  public void setExceptions(String[] exceptions) {
    if (exceptions != null) {
      attributes.add(new ExceptionsAttribute(exceptions));
    } else {
      attributes.remove(ClassfileConstants2.ATT_EXCEPTIONS);
    }
  }

  /**
   * Returns true if the field is deprecated
   */
  public boolean isDeprecated() {
    return attributes.get(ClassfileConstants2.ATT_DEPRECATED) != null;
  }

  /**
   * Sets the deprecated attribute of this field
   */
  public void setDeprecated(boolean deprecated) {
    if (deprecated) {
      attributes.add(new DeprecatedAttribute());
    } else {
      attributes.remove(ClassfileConstants2.ATT_DEPRECATED);
    }
  }

  /**
   * Returns true if the field is synthetic
   */
  public boolean isSynthetic() {
    return attributes.get(ClassfileConstants2.ATT_SYNTHETIC) != null;
  }

  /**
   * Returns true if the field is synthetic
   */
  public void setSynthetic(boolean synthetic) {
    if (synthetic) {
      attributes.add(new SyntheticAttribute());
    } else {
      attributes.remove(ClassfileConstants2.ATT_SYNTHETIC);
    }
  }

  /**
   * Returns the code attribute associated with this method
   */
  public CodeInfo getCodeInfo() {
    Attribute		attr = attributes.get(ClassfileConstants2.ATT_CODE);

    return attr == null ? null : (CodeInfo)attr;
  }

  /**
   * Sets the code attribute associated with this method
   */
  public void setCodeInfo(CodeInfo info) {
    if (info != null) {
      info.setParameterCount(getParameterCount());
      attributes.add(info);
    } else {
      attributes.remove(ClassfileConstants2.ATT_CODE);
    }
  }

  // --------------------------------------------------------------------
  // WRITE
  // --------------------------------------------------------------------

  /**
   * Insert or check location of constant value on constant pool
   *
   * @param	cp		the constant pool for this class
   */
  public void resolveConstants(ConstantPool cp) throws ClassFileFormatException {
    try {
      cp.addItem(name);
      cp.addItem(type);
      attributes.resolveConstants(cp);
    } catch (ClassFileFormatException e) {
      throw e;
    }
  }

  /**
   * Write this class into the the file (out) getting data position from
   * the constant pool
   *
   * @param	cp		the constant pool that contain all data
   * @param	out		the file where to write this object info
   *
   * @exception	java.io.IOException	an io problem has occured
   * @exception	ClassFileFormatException	attempt to
   *					write a bad classfile info
   */
  public void write(ConstantPool cp, DataOutput out)
    throws IOException, ClassFileFormatException
  { 
    try {
      out.writeShort(getModifiers() & MODIFIER_MASK);
      out.writeShort(name.getIndex());
      out.writeShort(type.getIndex());
      attributes.write(cp, out);
    } catch (InstructionOverflowException e ) {
      e.setMethod(name.getValue()+getSignature());
      throw e;
    } catch (LocalVariableOverflowException e ) {
      e.setMethod(name.getValue()+getSignature());
      throw e;
    }
  }

  // --------------------------------------------------------------------
  // PRIVATE METHODS
  // --------------------------------------------------------------------

  /**
   * Computes the number of parameters.
   */
  protected int getParameterCount() {
    String	signature = getSignature();
    int		paramCnt = 0;

    if ((getModifiers() & ACC_STATIC) == 0) {
      // an instance method always passes "this" as first, hidden parameter
      paramCnt += 1;
    }

    if (signature.charAt(0) != '(') {
      throw new InconsistencyException("invalid signature " + signature);
    }

    int		pos = 1;

  _method_parameters_:
    for (;;) {
      switch (signature.charAt(pos++)) {
      case ')':
	break _method_parameters_;

      case '[':
	while (signature.charAt(pos) == '[') {
	  pos += 1;
	}
	if (signature.charAt(pos) == 'L') {
	  while (signature.charAt(pos) != ';') {
	    pos += 1;
	  }
	}
	pos += 1;

	paramCnt += 1;
	break;

      case 'L':
	while (signature.charAt(pos) != ';') {
	  pos += 1;
	}
	pos += 1;

	paramCnt += 1;
	break;

      case 'Z':
      case 'B':
      case 'C':
      case 'S':
      case 'F':
      case 'I':
	paramCnt += 1;
	break;

      case 'D':
      case 'J':
	paramCnt += 2;
	break;

      default:
	throw new InconsistencyException("invalid signature " + signature);
      }
    }

    return paramCnt;
  }
  
/**
 * Returns the attributes.
 * @return AttributeList
 */
public AttributeList getAttributes() {
	return attributes;
}  

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  /**
   * Valid modifiers for methods.
   *
   * VMS 4.6 : All bits of the access_flags item not assigned in
   * Table 4.5 are reserved for future use. They should be set to zero
   * in generated class files.
   */
  private static final int		MODIFIER_MASK =
    ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC
    | ACC_FINAL | ACC_SYNCHRONIZED | ACC_NATIVE
    | ACC_ABSTRACT | ACC_STRICT;

  private AsciiConstant			name;
  private AsciiConstant			type;
  private AttributeList			attributes;


}
