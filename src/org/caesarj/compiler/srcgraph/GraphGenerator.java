/*
 * Created on 08.02.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.caesarj.compiler.srcgraph;

import org.caesarj.compiler.ast.phylum.JCompilationUnit;
import org.caesarj.compiler.ast.phylum.declaration.JClassDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.JInterfaceDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.JTypeDeclaration;
import org.caesarj.compiler.constants.CaesarConstants;
import org.caesarj.compiler.export.CBinaryClass;
import org.caesarj.compiler.export.CClass;
import org.caesarj.compiler.types.CCompositeType;
import org.caesarj.compiler.types.CReferenceType;
import org.caesarj.compiler.types.CType;

/**
 * ...
 * 
 * @author Ivica Aracic
 */
public class GraphGenerator implements CaesarConstants {

    private static GraphGenerator singleton = new GraphGenerator();
    
    public static GraphGenerator instance() {
        return singleton; 
    }
    
    private GraphGenerator() {
    }

    
    public void generateGraph(
        SourceDependencyGraph g,
        JCompilationUnit cu
    ) {
        JTypeDeclaration inners[] = cu.getInners();
        for(int i=0; i<inners.length; i++)
            if(inners[i] instanceof JClassDeclaration)
                generateGraph(g, (JClassDeclaration)inners[i]);
            else
                generateGraph(g, (JInterfaceDeclaration)inners[i]); 
    }

    private void generateGraph(SourceDependencyGraph g, JInterfaceDeclaration decl) {        
        CClass thisClass = decl.getCClass();
            
        RegularTypeNode thisNode = 
            (RegularTypeNode)g.getNode(thisClass.getQualifiedName());
                
        if(thisNode == null)
            thisNode = g.createNode(thisClass);
            
        thisNode.setTypeDeclaration(decl);
        
        CReferenceType interfaces[] = thisClass.getInterfaces();
        for(int i=0; i<interfaces.length; i++) {
            CClass superInterfaceClass = interfaces[i].getCClass();
            RegularTypeNode node = 
                (RegularTypeNode)g.getNode(superInterfaceClass.getQualifiedName());
                
            if(node == null)
                node = g.createNode(superInterfaceClass);
                                
            thisNode.addSuperTypeNode(node);
        }
        
        if(interfaces.length == 0) {
            thisNode.addSuperTypeNode(g.getRoot());
        }
    }

    private void generateGraph(
        SourceDependencyGraph g,
		JClassDeclaration decl
    ) {
        CClass thisClass  = decl.getCClass();
        CType superType   = decl.getSuperClass();
        CClass ownerClass = thisClass.getOwner();
        
        // create node for this type
        RegularTypeNode thisNode = (RegularTypeNode)g.getNode(thisClass.getQualifiedName());
        if(thisNode == null)
            thisNode = g.createNode(thisClass);
            
        thisNode.setTypeDeclaration(decl);
        
        
        if(superType instanceof CCompositeType) {
            CCompositeType compositeType = (CCompositeType)superType;            
            if(g.getNode(compositeType.getQualifiedName()) == null) {
                CReferenceType[] classList = compositeType.getClassList();
                
                TypeNode compositeTypeNode = g.createNode(compositeType);
                thisNode.addSuperTypeNode(compositeTypeNode);
                
                for(int i=0; i<classList.length; i++) {
                    TypeNode node = g.getNode(classList[i].getQualifiedName());
                    if(node == null)
                        node = g.createNode(classList[i].getCClass());                        

                    compositeTypeNode.addSuperTypeNode(node);
                }
            }
        }
        else {
            CClass clazz = superType.getCClass();
            if(clazz instanceof CBinaryClass) {
                thisNode.addSuperTypeNode(g.getRoot());
            }
            else {
                TypeNode superTypeNode = g.getNode(superType.getCClass().getQualifiedName());
                if(superTypeNode == null)
                    superTypeNode = g.createNode(superType.getCClass());
                    
                thisNode.addSuperTypeNode(superTypeNode);
            }
        }
        
        // add interface dependencies
        CReferenceType interfaces[] = thisClass.getInterfaces();
        for(int i=0; i<interfaces.length; i++) {
            CClass interfaceClass = interfaces[i].getCClass();
            TypeNode node = 
                g.getNode(interfaceClass.getQualifiedName());
                
            if(node == null) {
                node = g.createNode(interfaceClass);
                thisNode.addSuperTypeNode(node);
            } 
        }
        
        // recurse into inners
		JTypeDeclaration inners[] = decl.getInners();        
        for(int i=0; i<inners.length; i++)
            if(inners[i] instanceof JClassDeclaration)
                generateGraph(g, (JClassDeclaration)inners[i]);
	}

}