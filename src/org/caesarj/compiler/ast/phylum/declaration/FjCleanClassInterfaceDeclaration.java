package org.caesarj.compiler.ast.phylum.declaration;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Vector;

import org.caesarj.compiler.ast.JavaStyleComment;
import org.caesarj.compiler.ast.JavadocComment;
import org.caesarj.compiler.ast.phylum.JPhylum;
import org.caesarj.compiler.constants.CaesarMessages;
import org.caesarj.compiler.constants.CciConstants;
import org.caesarj.compiler.constants.FjConstants;
import org.caesarj.compiler.context.CContext;
import org.caesarj.compiler.export.CClass;
import org.caesarj.compiler.export.CModifier;
import org.caesarj.compiler.types.CClassNameType;
import org.caesarj.compiler.types.CReferenceType;
import org.caesarj.compiler.types.CStdType;
import org.caesarj.compiler.types.CTypeVariable;
import org.caesarj.util.PositionedError;
import org.caesarj.util.TokenReference;
import org.caesarj.util.Utils;

public class FjCleanClassInterfaceDeclaration extends FjInterfaceDeclaration
{

	FjCleanClassDeclaration baseDecl;

	public FjCleanClassInterfaceDeclaration(
		TokenReference tokenReference,
		String ident,
		int modifiers,
		CReferenceType[] interfaces,
		FjCleanMethodDeclaration[] methods,
		FjCleanClassDeclaration baseDecl)
	{
		super(
			tokenReference,
			ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT | FJC_CLEAN | modifiers,
			ident,
			CTypeVariable.EMPTY,
			interfaces,
			new JFieldDeclaration[0],
		// clean classes - no fields
		importMethods(methods), new JTypeDeclaration[0], // inners are possible
		new JPhylum[0],
			new JavadocComment(
				"Automatically generated interface.",
				false,
				false),
			new JavaStyleComment[0]);
		addInterface(new CClassNameType(
			FjConstants.CHILD_TYPE_NAME), 
			CciConstants.CHILD_TYPE_INDEX);
		this.baseDecl = baseDecl;
	}

	public FjMethodDeclaration[] getMethods()
	{
		return (FjMethodDeclaration[]) Utils.toArray(
			new Vector(Arrays.asList(methods)),
			FjMethodDeclaration.class);
	}

	public static FjMethodDeclaration[] importMethods(FjCleanMethodDeclaration[] cleanMethods)
	{

		FjMethodDeclaration[] abstractMethods =
			new FjMethodDeclaration[cleanMethods.length * 2];
		for (int i = 0; i < cleanMethods.length; i++)
		{
			abstractMethods[2 * i] =
				cleanMethods[i].getAbstractMethodDeclaration();
			abstractMethods[2 * i + 1] =
				cleanMethods[i]
					.getForwardSelfToImplementationMethod(CStdType.Object)
					.getAbstractMethodDeclaration();
		}
		return abstractMethods;
	}

	/**
	 * Adds a clean method, importing it to this context.
	 * 
	 * @param methodToAdd
	 */
	public void addMethod(FjCleanMethodDeclaration methodToAdd)
	{
		addMethods(new FjCleanMethodDeclaration[]{methodToAdd});
	}

	/**
	 * Adds clean methods, importing it to this context.
	 * 
	 * @param methodToAdd
	 */
	public void addMethods(FjCleanMethodDeclaration[] methodsToAdd)
	{
		FjMethodDeclaration[] importedMethods = 
			importMethods(methodsToAdd);

		JMethodDeclaration[] newMethods =
			new JMethodDeclaration[methods.length + importedMethods.length];

		System.arraycopy(methods, 0, newMethods, 0, methods.length);
		System.arraycopy(
			importedMethods,
			0,
			newMethods,
			methods.length,
			importedMethods.length);

		methods = newMethods;
	}	

	/**
	 * Adds a non clean method.
	 * 
	 * @param methodToAdd
	 */
	public void addMethod(JMethodDeclaration methodToAdd)
	{
		JMethodDeclaration[] newMethods =
			new JMethodDeclaration[methods.length + 1];

		System.arraycopy(methods, 0, newMethods, 1, methods.length);
		
		newMethods[0] = methodToAdd;

		methods = newMethods;
	}
	
	public FjCleanClassDeclaration getBaseClass()
	{
		return baseDecl;
	}

	public void append(JTypeDeclaration type)
	{
		JTypeDeclaration[] newInners =
			(JTypeDeclaration[]) Array.newInstance(
				JTypeDeclaration.class,
				inners.length + 1);
		for (int i = 0; i < inners.length; i++)
		{
			newInners[i] = inners[i];
		}
		newInners[inners.length] = type;
		inners = newInners;
	}
	public void addInterface(CReferenceType newInterface)
	{
		addInterface(newInterface, interfaces.length);
	}
	
	public void addInterface(CReferenceType newInterface, int index)
	{
		CReferenceType[] newInterfaces =
			new CReferenceType[interfaces.length + 1];

		System.arraycopy(interfaces, 0, newInterfaces, 0, index);
		newInterfaces[index] = newInterface;
		System.arraycopy(
			interfaces, 
			index, 
			newInterfaces, 
			index + 1, 
			interfaces.length - index);

		interfaces = newInterfaces;
		
	}


	public String getIdent()
	{
		return ident;
	}

	public void join(CContext context) throws PositionedError
	{
		try
		{
			super.join(context);
		}
		catch (PositionedError e)
		{
			// an error occuring here will occur in the
			// base class this class is derived from, too
		}
	}
	
	public void checkCollaborationInterface(CContext context, 
		String collaborationName)
		throws PositionedError
	{
		boolean found = false;
		for (int i = 0; i < interfaces.length; i++)
		{
			CClass interfaceClass = interfaces[i].getCClass();
			if (CModifier.contains(interfaceClass.getModifiers(),
					CCI_COLLABORATION)
				&& interfaceClass.getQualifiedName().equals(collaborationName))
			{
				found = true;
				break;
			}		
		}
		check(
			context,
			found,
			CaesarMessages.NON_CI,
			ident);		
	}		

	/**
	 * 
	 */
	protected int getAllowedModifiers()
	{
		return super.getAllowedModifiers() | FJC_CLEAN;
	}

}