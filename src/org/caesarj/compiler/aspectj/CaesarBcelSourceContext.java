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
 * $Id: CaesarBcelSourceContext.java,v 1.2 2005-06-03 08:12:35 klose Exp $
 */

package org.caesarj.compiler.aspectj;

import java.io.File;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.bcel.BcelObjectType;
import org.aspectj.weaver.bcel.BcelSourceContext;

/**
 * @author vaidas
 *
 * TODO [documentation]
 */
public class CaesarBcelSourceContext extends BcelSourceContext {
	
	protected String sourceFileName;
	
	public CaesarBcelSourceContext(BcelObjectType inObject, String fileName) {
		super(inObject);
		sourceFileName = fileName;
	}
		
	public ISourceLocation makeSourceLocation(IHasPosition position) {
		if (position instanceof Advice) {
			return new SourceLocation(getSourceFile(), position.getEnd());
		}
		else {
			return super.makeSourceLocation(position);
		}
	}
	
	protected File getSourceFile() {
		return new File(sourceFileName);
	}
}