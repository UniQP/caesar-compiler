package org.caesarj.compiler.delegation;

import org.caesarj.compiler.KjcEnvironment;
import org.caesarj.compiler.ast.FjClassDeclaration;
import org.caesarj.compiler.ast.FjCleanClassDeclaration;
import org.caesarj.compiler.ast.FjCleanClassIfcImplDeclaration;
import org.caesarj.compiler.ast.FjCleanClassInterfaceDeclaration;
import org.caesarj.compiler.ast.FjCompilationUnit;
import org.caesarj.compiler.ast.FjVirtualClassDeclaration;
import org.caesarj.compiler.ast.FjVisitor;
import org.caesarj.compiler.ast.JClassImport;
import org.caesarj.compiler.ast.JCompilationUnit;
import org.caesarj.compiler.ast.JMethodDeclaration;
import org.caesarj.compiler.ast.JPackageImport;
import org.caesarj.compiler.ast.JPackageName;
import org.caesarj.compiler.ast.JPhylum;
import org.caesarj.compiler.ast.JTypeDeclaration;
import org.caesarj.compiler.constants.FjConstants;
import org.caesarj.compiler.types.CClassNameType;
import org.caesarj.compiler.types.CReferenceType;
import org.caesarj.compiler.types.CTypeVariable;


public class ClassTransformationFjVisitor extends FjVisitor
{

	protected KjcEnvironment environment;
	
	/**
	 * sets the environmment.
	 * 
	 * @param environment
	 */
	public ClassTransformationFjVisitor(KjcEnvironment environment)
	{
		this.environment = environment;
	}

	/**
	 * sets the classReader and packagePrefix on the inner classes.
	 */
	public void visitCompilationUnit(
		JCompilationUnit self,
		JPackageName packageName,
		JPackageImport[] importedPackages,
		JClassImport[] importedClasses,
		JTypeDeclaration[] typeDeclarations)
	{

		String packagePrefix = null;
		if (packageName != null)
			packagePrefix = packageName.getName();
		if (packagePrefix == null || packagePrefix.trim().equals(""))
			packagePrefix = "";
		else
			packagePrefix = packagePrefix + "/";

		super.visitCompilationUnit(
			self,
			packageName,
			importedPackages,
			importedClasses,
			typeDeclarations);

		JTypeDeclaration[] inners = ((FjCompilationUnit) self).getInners();
		for (int i = 0; i < inners.length; i++)
		{
			inners[i].generateInterface(
				environment.getClassReader(),
				null,
				packagePrefix);
		}
	}

	/**
	 * sets the owner on visited classes.
	 */
	public void visitFjClassDeclaration(
		FjClassDeclaration self,
		int modifiers,
		String ident,
		CTypeVariable[] typeVariables,
		String superClass,
		CReferenceType[] interfaces,
		JPhylum[] body,
		JMethodDeclaration[] methods,
		JTypeDeclaration[] decls)
	{

		// classes need to know their
		// owners in order to be able to access
		// fields when inheriting
		Object myOwner = owner.get();
		if (myOwner instanceof FjClassDeclaration)
			self.setOwnerDeclaration(myOwner);

		super.visitFjClassDeclaration(
			self,
			modifiers,
			ident,
			typeVariables,
			superClass,
			interfaces,
			body,
			methods,
			decls);
	}

	/**
	 * puts the clean interface in the containing class, implements 
	 * it and remains the implementing class accordingly.
	 */
	public void visitFjCleanClassDeclaration(
		FjCleanClassDeclaration self,
		int modifiers,
		String ident,
		CTypeVariable[] typeVariables,
		String superClass,
		CReferenceType[] interfaces,
		JPhylum[] body,
		JMethodDeclaration[] methods,
		JTypeDeclaration[] decls)
	{

		self.setTypeFactory(environment.getTypeFactory());
		

		
		// Put self's clean interface into the
		// containing class or compilationunit
		FjCleanClassInterfaceDeclaration ifcDecl =
			self.createCleanInterface(owner.get());
		FjCleanClassIfcImplDeclaration implDecl =
			self.createCleanInterfaceImplementation(owner.get());

		owner.append(ifcDecl);
		owner.append(implDecl);

		// Let self implement its own clean interface and inherit
		// its superclass clean-interface-implementation
		CReferenceType ifcType =
			new CClassNameType(owner.getQualifiedName() + ifcDecl.getIdent());
		// Important: add interface at canonical position 0!
		self.addInterface(ifcType,0);
		self.setSuperClass();
		implDecl.setSuperClass(self.getSuperClass());

		// Resets the class' name because the clean interface
		// gets the class' old name so that we do not have to
		// change any type declarations from class to clean interface.
		self.setIdent(FjConstants.baseName(self.getIdent()));

		// Our inner declarations may have further
		// inners, so descend here too.
		super.visitFjCleanClassDeclaration(
			self,
			modifiers,
			ident,
			typeVariables,
			superClass,
			interfaces,
			body,
			methods,
			decls);
		
	}

	/**
	 * sets the owner of visited classes.
	 */
	public void visitFjVirtualClassDeclaration(
		FjVirtualClassDeclaration self,
		int modifiers,
		String ident,
		CTypeVariable[] typeVariables,
		String superClass,
		CReferenceType[] interfaces,
		JPhylum[] body,
		JMethodDeclaration[] methods,
		JTypeDeclaration[] decls)
	{

		// virtual classes need to now in order
		// to perform the proper tranformations
		self.setOwnerDeclaration(owner.get());
		//Insert adapt method if the owner is clean
		if (owner.get() instanceof FjCleanClassDeclaration)
			self.addAdaptMethod();

		super.visitFjVirtualClassDeclaration(
			self,
			modifiers,
			ident,
			typeVariables,
			superClass,
			interfaces,
			body,
			methods,
			decls);
	}
}