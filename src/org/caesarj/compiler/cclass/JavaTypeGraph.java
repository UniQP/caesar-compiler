package org.caesarj.compiler.cclass;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * ...
 * 
 * @author Ivica Aracic
 */
public class JavaTypeGraph {
    
    private HashMap origMixinMap = new HashMap();
    private JavaTypeNode root;
    private HashMap caesar2javaMap = new HashMap();
    private HashMap nodes = new HashMap();
    
    public JavaTypeGraph() {
        CaesarTypeNode typeNode = new CaesarTypeNode(null, "java/lang/Object", false);
        root = new JavaTypeNode(this, typeNode);
        root.setType(typeNode);
    }
    
    public void generateFrom(CaesarTypeGraph completeGraph) {
        Map typeMap = completeGraph.getTypeMap();
        
        // generate graph
        for (Iterator it = typeMap.entrySet().iterator(); it.hasNext();) {
            CaesarTypeNode t = (CaesarTypeNode) ((Map.Entry)it.next()).getValue();
            
            List mixinList = t.getMixinList();
            
            // sort list into compilation graph
            CaesarTypeNode[] mixins = new CaesarTypeNode[mixinList.size()];
            mixins = (CaesarTypeNode[])mixinList.toArray(mixins);
            
            JavaTypeNode current = root;
            
            for (int i=mixins.length-1; i>=0; i--) {
                CaesarTypeNode mixin = mixins[i];
                JavaTypeNode next = current.getSubNode(mixin.getQualifiedName().toString());
                
                if(next == null) {
                    next = new JavaTypeNode(this, mixin);
                    current.addSubNode(next);
                }
                                
                current = next;
            }
            
            if(t.isImplicit()) {
                // append as leaf
                JavaTypeNode next = new JavaTypeNode(this, t);
                next.setType(t);
                current.addSubNode(next);
            }
            else {
                origMixinMap.put(t.getQualifiedName().toString(), current);
                current.setType(t);
            }
        }
                
        root.genMixinCopyDependencies();        
        root.genOuterAndQualifiedNames();
        root.genOuterAndQNForGeneratedTypes(new HashSet());
    }
    
    
    public JavaTypeNode getOriginalMixin(JavaQualifiedName qualifiedName) {
        return (JavaTypeNode)origMixinMap.get(qualifiedName.toString());
    }
        
    public void debug() {
        root.debug(0);
    }

    public void registerJavaType(CaesarTypeNode type, JavaTypeNode node) {
        caesar2javaMap.put(type, node);
    }    
    
    public JavaTypeNode getJavaTypeNode(CaesarTypeNode type) {
        return (JavaTypeNode)caesar2javaMap.get(type);
    }
    
    public JavaTypeNode getNode(JavaQualifiedName qn) {
        return (JavaTypeNode)nodes.get(qn);
    }
    
    protected void registerNode(JavaQualifiedName qn, JavaTypeNode node) {
        nodes.put(qn, node);
    }

    /** 
     * @return preorder sorted list of types which has to be generated 
     */
    public Collection getTypesToGenerate() {
        Collection res = new LinkedList();
        root.collectGeneratedTypes(res);
        return res;
    }

    /**
     * @return preorder sorted list of all types
     */
    public Collection getAllTypes() {
        Collection res = new LinkedList();
        root.collectAllTypes(res);
        return res;
    }
    
    /**
     * CTODO getAllSourceTypes
     */
    public Collection getAllSourceTypes() {
        return null;
    }
}