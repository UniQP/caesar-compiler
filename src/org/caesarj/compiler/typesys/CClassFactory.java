package org.caesarj.compiler.typesys;

import java.util.ArrayList;

import org.caesarj.compiler.KjcEnvironment;
import org.caesarj.compiler.ast.phylum.JPhylum;
import org.caesarj.compiler.ast.phylum.declaration.CjInterfaceDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.CjMethodDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.CjMixinInterfaceDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.CjVirtualClassDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.JConstructorDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.JFieldDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.JMemberDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.JMethodDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.JTypeDeclaration;
import org.caesarj.compiler.constants.CaesarConstants;
import org.caesarj.compiler.export.CCjSourceClass;
import org.caesarj.compiler.export.CClass;
import org.caesarj.compiler.types.CClassNameType;
import org.caesarj.compiler.types.CCompositeNameType;
import org.caesarj.compiler.types.CReferenceType;
import org.caesarj.compiler.types.CTypeVariable;
import org.caesarj.compiler.types.TypeFactory;
import org.caesarj.util.TokenReference;

/**
 * ...
 * 
 * @author Ivica Aracic
 */
public class CClassFactory implements CaesarConstants {

	private CjVirtualClassDeclaration caesarClass;
	
	private String interfaceName;
	private String prefix;
    CClass interfaceOwner;

	private KjcEnvironment environment;
	private TypeFactory typeFactory;

	private TokenReference where;

	/**
	 * Constructor for CaesarDeploymentUtils.
	 */
	public CClassFactory(
		CjVirtualClassDeclaration caesarClass,
		KjcEnvironment environment
    ) {
		this.caesarClass = caesarClass;
		this.where = caesarClass.getTokenReference();
		this.typeFactory = environment.getTypeFactory();
		this.environment = environment;

		initState();
	}

	private void initState() {              
        CCjSourceClass caesarClassOwner = (CCjSourceClass)caesarClass.getOwner();

        if(caesarClassOwner != null) {
            CjVirtualClassDeclaration ownerClassDeclaration = 
                (CjVirtualClassDeclaration)caesarClassOwner.getTypeDeclaration();
            interfaceOwner = 
                ownerClassDeclaration.getMixinIfcDeclaration().getCClass();
        }

        if(interfaceOwner != null) {
            prefix = interfaceOwner.getQualifiedName()+'$';
        }
        else {
            prefix = caesarClass.getCClass().getPackage();
            if(prefix.length() > 0) {
                prefix += '/';
            }
        }

		//Intialize some class and interface identifiers
		interfaceName = caesarClass.getOriginalIdent();
	}

	/**
	 * Creates the cclass Interface.
	 */
	public CjInterfaceDeclaration createCaesarClassInterface() {

		JMethodDeclaration[] cclassMethods = caesarClass.getMethods();

        ArrayList interfaceMethods = new ArrayList(cclassMethods.length);

        // copy all public, non-static class methods to interface
		for (int i = 0; i < cclassMethods.length; i++) {      
            if(
                !(cclassMethods[i] instanceof JConstructorDeclaration)
                && ((cclassMethods[i].getModifiers() & JMemberDeclaration.ACC_PUBLIC) != 0)
                && ((cclassMethods[i].getModifiers() & JMemberDeclaration.ACC_STATIC) == 0)
            )      
                interfaceMethods.add(createInterfaceMethod(cclassMethods[i]));
		}

        // default is our interface has no superinterface
        CReferenceType[] superInterfaces = new CReferenceType[]{};
        
        CReferenceType superType = caesarClass.getSuperClass();
        
        // CTODO think about it
        if(superType instanceof CCompositeNameType) {
            // if we have a composite type our superinterface list consists
            // of composite type's typeList 
            CCompositeNameType compositType = (CCompositeNameType)superType;
            CClassNameType typeList[] = compositType.getTypeList();
            superInterfaces = new CReferenceType[typeList.length];            
            for(int i=0; i<typeList.length; i++) {
                superInterfaces[i] = typeList[i];
            }
        }
        else if(superType instanceof CClassNameType) {
            // if we have a super cclass, our superinterface list consist of
            // superTypes interface name
            superInterfaces = new CReferenceType[]{superType};
        }
        
        CReferenceType ifcs[] = caesarClass.getInterfaces();
        
        if(ifcs.length > 0) {
            CReferenceType tmp[] = new CReferenceType[superInterfaces.length+ifcs.length];
            System.arraycopy(superInterfaces, 0, tmp, 0, superInterfaces.length);
            System.arraycopy(ifcs, 0, tmp, superInterfaces.length, ifcs.length);
            superInterfaces = tmp;
        }
        

		CjMixinInterfaceDeclaration cclassInterface =
			new CjMixinInterfaceDeclaration(
				caesarClass.getTokenReference(),
				ACC_PUBLIC,
				interfaceName,
				superInterfaces,
				JFieldDeclaration.EMPTY,
				(JMethodDeclaration[])interfaceMethods.toArray(new JMethodDeclaration[]{}),
				new JTypeDeclaration[0],
				new JPhylum[0]);                  

        cclassInterface._generateInterface(
            environment.getClassReader(), interfaceOwner, prefix
        );

        // link this two AST elements
        caesarClass.setMixinIfcDeclaration(cclassInterface);
        cclassInterface.setCorrespondingClassDeclaration(caesarClass);
        
		return cclassInterface;
	}
        
    private String getPrefix(String qualifiedName) {
        String res = "";

        int i = qualifiedName.lastIndexOf('$');
        
        if(i < 0) {
            i = qualifiedName.lastIndexOf('/'); 
        }
        
        if(i >= 0) {
            res = qualifiedName.substring(0, i+1);
        }
        
		return res;
	}

	public void addCaesarClassInterfaceInners() {        
		caesarClass.getMixinIfcDeclaration().generateInterfaceInners(
            environment.getClassReader(),            
            prefix);            
    }

	private JMethodDeclaration createInterfaceMethod(JMethodDeclaration m) {
		return new CjMethodDeclaration(
			where,
			ACC_PUBLIC | ACC_ABSTRACT,
			CTypeVariable.EMPTY,
			m.getReturnType(),
			m.getIdent(),
			m.getParameters(),
			m.getExceptions(),
			null,
			null,
			null);

	}
    
	public void modifyCaesarClass() {    	
				
		caesarClass.setInterfaces(CReferenceType.EMPTY);
		caesarClass.setSuperClass(null);		
	}
}