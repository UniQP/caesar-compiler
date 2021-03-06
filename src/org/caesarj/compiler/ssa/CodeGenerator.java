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
 * $Id: CodeGenerator.java,v 1.2 2005-01-24 16:52:57 aracic Exp $
 */

package org.caesarj.compiler.ssa;

import org.caesarj.classfile.Instruction;

/**
 * To generate classfile instructions
 */
public abstract class CodeGenerator {
    // -------------------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------------------
    /**
     * Construct the code generator
     */
    public CodeGenerator() {
    }

    // -------------------------------------------------------------------
    // ACCESSOR
    // -------------------------------------------------------------------
    /**
     * Add an new instruction
     *
     * @param inst the instruction to add.
     */
    public abstract void addInstruction(Instruction inst);

    // -------------------------------------------------------------------
    // ACCESSORS
    // -------------------------------------------------------------------
    /**
     * Get the current basic block
     */
    public BasicBlock getCurrentBasicBlock() {
	return current;
    }

    /**
     * Set the current basic block
     */
    public void setCurrentBasicBlock(BasicBlock bb) {
        current = bb;
    }

    // -------------------------------------------------------------------
    // ATTRIBUTS
    // -------------------------------------------------------------------
    protected BasicBlock current;
}

