package org.caesarj.compiler.ast.phylum.declaration;

import org.caesarj.compiler.ast.JavaStyleComment;
import org.caesarj.compiler.ast.JavadocComment;
import org.caesarj.compiler.ast.phylum.statement.JBlock;
import org.caesarj.compiler.ast.phylum.variable.JFormalParameter;
import org.caesarj.compiler.types.CReferenceType;
import org.caesarj.compiler.types.CType;
import org.caesarj.compiler.types.CTypeVariable;
import org.caesarj.util.TokenReference;

/**
 * @author andreas
 */
public class FjImplementationMethodDeclaration extends FjMethodDeclaration {

	/**
	 * Constructor for FjImplementationMethodDeclaration.
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
	public FjImplementationMethodDeclaration(
		TokenReference where,
		int modifiers,
		CTypeVariable[] typeVariables,
		CType returnType,
		String ident,
		JFormalParameter[] parameters,
		CReferenceType[] exceptions,
		JBlock body,
		JavadocComment javadoc,
		JavaStyleComment[] comments) {
		super(
			where,
			modifiers,
			typeVariables,
			returnType,
			ident,
			parameters,
			exceptions,
			body,
			javadoc,
			comments);
	}

}