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
 * $Id: CjExternClassContext.java,v 1.4 2005-11-02 15:46:07 gasiunas Exp $
 */

package org.caesarj.compiler.context;

import java.lang.ref.WeakReference;

import org.caesarj.compiler.KjcEnvironment;
import org.caesarj.compiler.ast.phylum.declaration.JTypeDeclaration;
import org.caesarj.compiler.export.CClass;
import org.caesarj.compiler.export.CCompilationUnit;
import org.caesarj.compiler.export.CSourceClass;
import org.caesarj.util.UnpositionedError;

/**
 * @author vaidas
 *
 * TODO [documentation]
 */
public class CjExternClassContext extends FjClassContext {
	
	WeakReference<CCompilationUnit> origCu;

	public CjExternClassContext(
			CContext parent,
			KjcEnvironment environment,
			CSourceClass clazz,
			JTypeDeclaration decl,
			CCompilationUnit origCu) {
		super(parent, environment, clazz, decl);
		this.origCu = new WeakReference<CCompilationUnit>(origCu);
	}
	
	public CClass lookupClass(CClass caller, String name) throws UnpositionedError {
		try {
			return super.lookupClass(caller, name);			
		}
		catch (UnpositionedError e) {
			return origCu.get().lookupClass(caller, name);
		}		
	}
}
