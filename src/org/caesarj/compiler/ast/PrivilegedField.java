package org.caesarj.compiler.ast;

import org.caesarj.kjc.CField;
import org.caesarj.kjc.CMethod;
import org.caesarj.kjc.CReferenceType;
import org.caesarj.kjc.CType;
import org.caesarj.kjc.CTypeVariable;
import org.caesarj.kjc.CVoidType;
import org.caesarj.compiler.aspectj.CaesarBcelWorld;
import org.caesarj.compiler.aspectj.CaesarMember;

/**
 * A privileged field encapsulates a non-visible field and 
 * provides the corresponding access-methods.
 * 
 * @author J�rgen Hallpap
 */
public class PrivilegedField extends FjSourceField {

	/** the encapsulated non-visible baseField.*/
	private CField baseField;

	/** the reader access-method.*/
	private CMethod reader;

	/** the writer access-method.*/
	private CMethod writer;

	/** the resolved-member for attribute creation*/
	private CaesarMember	resolvedMember;
	
	/** the declaring type*/
	private String declaringType;

	/**
	 * Constructor for PrivilegedField.
	 * @param baseField
	 * @param aspect
	 */
	public PrivilegedField(
		CField baseField,
		FjSourceClass aspect,
		FjFamily family) {
		super(
			baseField.getOwner(),
			baseField.getModifiers(),
			baseField.getIdent(),
			baseField.getType(),
			baseField.isDeprecated(),
			baseField.isSynthetic(),
			family);

		this.baseField = baseField;

		this.declaringType = CaesarBcelWorld.getInstance().resolve(owner).getSignature();

 		String aspectType = CaesarBcelWorld.getInstance().resolve(aspect).getSignature();

		CaesarMember field =
			CaesarMember.Member(
				CaesarMember.FIELD,
				declaringType,
				getModifiers(),
				getIdent(),
				getType().getSignature());

		CaesarMember	readerMember =
			CaesarMember.privilegedAccessMethodForFieldGet(aspectType,field);

		CType[] readerParameterTypes = { getOwnerType()};
		FjFamily[] parameterFamilies = { getFamily()};

		reader =
			new FjSourceMethod(
				owner,
				readerMember.getModifiers(),
				readerMember.getName(),
				getType(),
				readerParameterTypes,
				CReferenceType.EMPTY,
				CTypeVariable.EMPTY,
				false,
				true,
				null,
				parameterFamilies);


		CaesarMember	writerMember = 
			CaesarMember.privilegedAccessMethodForFieldSet(aspectType, field);

		CType[] writerParameterTypes = { getOwnerType(), getType()};
		writer =
			new FjSourceMethod(
				owner,
				writerMember.getModifiers(),
				writerMember.getName(),
				new CVoidType(),
				writerParameterTypes,
				CReferenceType.EMPTY,
				CTypeVariable.EMPTY,
				false,
				true,
				null,
				new FjFamily[0]);

	}

	/**
	 * Gets the appropriate accessor.
	 * 
	 * @param isReadAccess
	 * @return CMethod
	 */
	public CMethod getAccessMethod(boolean isReadAccess) {
		if (isReadAccess)
			return reader;
		else
			return writer;
	}

	/**
	 * Gets the resolvedMember.
	 * 
	 * @return ResolvedMember
	 */
//	public ResolvedMember getResolvedMember() {
	public CaesarMember getResolvedMember() {
		if (resolvedMember == null) {
			resolvedMember =
				CaesarMember.ResolvedMember(
					CaesarMember.FIELD,
					declaringType,
					getModifiers(),
					getIdent(),
					getType().getSignature());
		}

		return resolvedMember;
	}

}
