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
 * $Id: CaesarSourceType.java,v 1.13 2005-11-03 11:40:10 gasiunas Exp $
 */

package org.caesarj.compiler.aspectj;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.ResolvedTypeX.ConcreteName;
import org.aspectj.weaver.ResolvedTypeX.Name;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.PerClause;
import org.caesarj.compiler.constants.Constants;
import org.caesarj.compiler.export.CCjSourceClass;
import org.caesarj.compiler.export.CClass;
import org.caesarj.compiler.export.CMethod;
import org.caesarj.compiler.types.CReferenceType;

/**
 * @author J�rgen Hallpap
 *
 */
public class CaesarSourceType extends ConcreteName implements Constants {

	private ResolvedTypeX[] declaredInterfaces;

	private ResolvedTypeX superClass;

	private ResolvedMember[] declaredPointcuts;

	private ResolvedMember[] declaredMethods;

	private Collection declares;

	private WeakReference<CClass> cclass;

	private WeakReference<CaesarBcelWorld> world = new WeakReference<CaesarBcelWorld>(CaesarBcelWorld.getInstance());

	/**
	 * Constructor for CaesarSourceType.
	 * @param resolvedTypeX
	 * @param exposedToWeaver
	 */
	public CaesarSourceType(
		Name resolvedTypeX,
		boolean exposedToWeaver,
		CClass cclass) {
		super(resolvedTypeX, exposedToWeaver);

		this.cclass = new WeakReference<CClass>(cclass);
	}

	/**
	 * Return null. Only to be compatible with the ConcreteName.
	 * thiago
	 */
	public WeaverStateInfo getWeaverState() {
	    return null;
	}
	
	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#isAspect()
	 */
	public boolean isAspect() {
		return true;
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#isInterface()
	 */
	public boolean isInterface() {
		return cclass.get().isInterface();
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#getDeclaredFields()
	 */
	public ResolvedMember[] getDeclaredFields() {
		throw new RuntimeException("unimplemented");
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#getDeclaredInterfaces()
	 */
	public ResolvedTypeX[] getDeclaredInterfaces() {
		if (declaredInterfaces == null) {
			fillDeclaredMembers();
		}

		return declaredInterfaces;
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#getDeclaredMethods()
	 */
	public ResolvedMember[] getDeclaredMethods() {
		if (declaredMethods == null) {
			fillDeclaredMembers();
		}

		return declaredMethods;
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#getDeclaredPointcuts()
	 */
	public ResolvedMember[] getDeclaredPointcuts() {
		if (declaredPointcuts == null) {
			fillDeclaredMembers();
		}
		return declaredPointcuts;
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#getPerClause()
	 */
	public PerClause getPerClause() {
		throw new RuntimeException("unimplemented");
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#getDeclares()
	 */
	protected Collection getDeclares() {
		if (declares == null) {
			fillDeclaredMembers();
		}

		return declares;
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#getTypeMungers()
	 */
	protected Collection getTypeMungers() {
		throw new RuntimeException("unimplemented");
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#getPrivilegedAccesses()
	 */
	protected Collection getPrivilegedAccesses() {
		throw new RuntimeException("unimplemented");
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#getModifiers()
	 */
	public int getModifiers() {
		return cclass.get().getModifiers();
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#getSuperclass()
	 */
	public ResolvedTypeX getSuperclass() {
		if (superClass == null) {
			fillDeclaredMembers();
		}

		return superClass;
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#getSourceLocation()
	 */
	public ISourceLocation getSourceLocation() {
		throw new RuntimeException("unimplemented");
	}

	/**
	 * @see org.aspectj.weaver.ResolvedTypeX.ConcreteName#isWovenBy(ResolvedTypeX)
	 */
	public boolean isWovenBy(ResolvedTypeX aspectType) {
		return false;
	}

	protected void fillDeclaredMembers() {
		CReferenceType[] ifcs = cclass.get().getInterfaces();
		declaredInterfaces = new ResolvedTypeX[ifcs.length];
		for (int i = 0; i < ifcs.length; i++) {
			declaredInterfaces[i] = world.get().resolve(ifcs[i].getCClass());
		}

		if (cclass.get().getSuperClass() != null) {
			superClass = world.get().resolve(cclass.get().getSuperClass());
		}

		/*
		List pointcuts = new ArrayList();
		if (cclass instanceof CCjSourceClass) {
			CCjSourceClass caesarClass = (CCjSourceClass) cclass;
			pointcuts.addAll(caesarClass.getResolvedPointcuts());
		}
		declaredPointcuts =
			(ResolvedMember[]) pointcuts.toArray(
				new ResolvedMember[pointcuts.size()]);
		*/
	
		// Get the declared pointcuts
		List pointcuts = new ArrayList();
		if (cclass.get() instanceof CCjSourceClass) {
			CCjSourceClass caesarClass = (CCjSourceClass) cclass.get();
			pointcuts.addAll(caesarClass.getDeclaredPointcuts());
		}
		declaredPointcuts =
			(ResolvedMember[]) pointcuts.toArray(
				new ResolvedMember[pointcuts.size()]);

		CMethod[] methods = cclass.get().getMethods();
		if (methods != null) {
			declaredMethods = new ResolvedMember[methods.length];

			for (int i = 0; i < methods.length; i++) {
				//XXX resolved???
				ResolvedMember member =
					new ResolvedMember(
						Member.METHOD,
						TypeX.forName(methods[i].getOwner().getQualifiedName()),
						methods[i].getModifiers(),
						methods[i].getIdent(),
						methods[i].getSignature());

				declaredMethods[i] = member;
			}

		} else {
			declaredMethods = new ResolvedMember[0];
		}

		declares = new ArrayList();
		if (cclass.get() instanceof CCjSourceClass) {
			CCjSourceClass caesarClass = (CCjSourceClass) cclass.get();
			Declare[] decs = CaesarDeclare.wrappees(caesarClass.getDeclares());
			for (int i = 0; i < decs.length; i++) {
				declares.add(decs[i]);
			}
		}
	}
	
	public boolean isExposedToWeaver() { 
		return true; 
	}
}
