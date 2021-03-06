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
 * $Id: CjPointcutDeclaration.java,v 1.13 2006-05-05 14:00:42 gasiunas Exp $
 */

package org.caesarj.compiler.ast.phylum.declaration;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.weaver.ResolvedTypeX;
import org.caesarj.compiler.aspectj.CaesarBcelWorld;
import org.caesarj.compiler.aspectj.CaesarFormalBinding;
import org.caesarj.compiler.aspectj.CaesarMember;
import org.caesarj.compiler.aspectj.CaesarPointcut;
import org.caesarj.compiler.aspectj.CaesarPointcutScope;
import org.caesarj.compiler.ast.JavadocComment;
import org.caesarj.compiler.ast.phylum.statement.JBlock;
import org.caesarj.compiler.ast.phylum.statement.JStatement;
import org.caesarj.compiler.ast.phylum.variable.JFormalParameter;
import org.caesarj.compiler.context.CClassContext;
import org.caesarj.compiler.context.CjDoubleClassContext;
import org.caesarj.compiler.context.FjClassContext;
import org.caesarj.compiler.export.CCjSourceClass;
import org.caesarj.compiler.export.CClass;
import org.caesarj.compiler.export.CSourceClass;
import org.caesarj.compiler.export.CSourceMethod;
import org.caesarj.compiler.types.CReferenceType;
import org.caesarj.compiler.types.CType;
import org.caesarj.util.PositionedError;
import org.caesarj.util.TokenReference;

/**
 * A Pointcut declaration.
 * 
 * @author J�rgen Hallpap
 */
public class CjPointcutDeclaration extends CjMethodDeclaration {

	public static final CjPointcutDeclaration[] EMPTY =
		new CjPointcutDeclaration[0];

	/**The corresponding Pointcut.*/
	private CaesarPointcut pointcut;

	private boolean checked=false;

	/** The virtual class where this pointcut was declared */
    private CjVirtualClassDeclaration originalClass;
    
	/**
	 * Constructor for PointcutDeclaration.
	 * @param where
	 * @param modifiers
	 * @param typeVariables
	 * @param returnType
	 * @param ident
	 * @param parameters
	 * @param exceptions
	 * @param body
	 * @param javadoc
	 * @param comments
	 */
	public CjPointcutDeclaration(
		TokenReference where,
		int modifiers,
		CType returnType,
		String ident,
		JFormalParameter[] parameters,
		JavadocComment javadoc,
		CaesarPointcut pointcut) {
		super(
			where,
			modifiers,
			returnType,
			ident,
			parameters,
			CReferenceType.EMPTY,
			(modifiers & ACC_ABSTRACT) == 0
				? new JBlock(where, new JStatement[0], null)
				: null,
			javadoc,
			null);

		this.pointcut = pointcut;
	}

	/**
	 * @see org.caesarj.kjc.JMethodDeclaration#checkInterface(CClassContext)
	 */
	public CSourceMethod checkInterface(CClassContext context)
		throws PositionedError {
		
		try {
			context.setAllowsDependentTypes(false);
			
			// If there's an original class, use it to resolve the parameter types
			CClassContext pointcutContext = getPointcutContext(context);
			
			CType[] parameterTypes = new CType[parameters.length];
			
			for (int i = 0; i < parameterTypes.length; i++) {
				parameterTypes[i] = parameters[i].checkInterface(pointcutContext);
			}

			CCjSourceClass crosscuttingClass = (CCjSourceClass) context.getCClass();

			CaesarMember rpd =
				resolve(
					context,
					context.getCClass(),
					parameters,
					getTokenReference());

			crosscuttingClass.addResolvedPointcut(rpd);
			
			checked=true;
			return null;
		}
		finally {
			context.setAllowsDependentTypes(true);
		}
	}

	public CaesarMember resolve(
		CClassContext context,
		CClass caller,
		JFormalParameter[] formalParameters,
		TokenReference tokenReference) {

		List parameterSignatures = new ArrayList();
		List formalBindings = new ArrayList();

		for (int i = 0; i < parameters.length; i++) {

			if (!formalParameters[i].isGenerated()) {

				ResolvedTypeX typeX = CaesarBcelWorld.getInstance().resolve(parameters[i].getType());
				parameterSignatures.add(typeX);

				formalBindings.add(
					new CaesarFormalBinding(
						typeX,
						formalParameters[i].getIdent(),
						i,
						tokenReference.getLine(),
						tokenReference.getLine(),
						tokenReference.getFile()));
			}
		}

        // If there's an original class, use its context to resolve the pointcut
		FjClassContext classContext = (FjClassContext)getPointcutContext(context);
        
        classContext.setBindings(
			(CaesarFormalBinding[]) formalBindings.toArray(new CaesarFormalBinding[0]));

		if (((modifiers & ACC_ABSTRACT) == 0)&&!checked) {
				pointcut.resolve(new CaesarPointcutScope((FjClassContext) classContext, caller, getTokenReference()));
		}

		CaesarMember rpd =
			CaesarMember.ResolvedPointcutDefinition(
				CaesarBcelWorld.getInstance().resolve(context.getCClass()),
				modifiers,
				getIdent(),
				(ResolvedTypeX[]) parameterSignatures.toArray(new ResolvedTypeX[0]),
				pointcut);

		return rpd;
	}
	
	public CaesarPointcut getPointcut() {
	    return pointcut;
	}
	
	public String toString(){
		return pointcut.toString();
	}

	/**
	 * @return Returns the checked.
	 */
	public boolean isChecked() {
		return checked;
	}

    /**
     * @return Returns the originalClass.
     */
    public CjVirtualClassDeclaration getOriginalClass() {
        return originalClass;
    }
    /**
     * @param originalClass The originalClass to set.
     */
    public void setOriginalClass(CjVirtualClassDeclaration originalClass) {
        this.originalClass = originalClass;
    }
    
    protected CClassContext getPointcutContext(CClassContext currentContext) {
    	if (originalClass == null) {
        	return currentContext;
        }
        else {
        	CClassContext 
        		ctx = new CjDoubleClassContext(currentContext.getParentContext(),
        			currentContext.getEnvironment(),
        			(CSourceClass)currentContext.getCClass(),
        			currentContext.getTypeDeclaration(),
        			originalClass.getContext());
        	ctx.setAllowsDependentTypes(false);
        	return ctx;
        }
    }
}
