package org.caesarj.compiler.typesys.graph;

/**
 * Representes an outer <-> inner class relation
 * 
 * @author Ivica Aracic
 */
public class OuterInnerRelation extends BidirectionalRelation {
	public OuterInnerRelation(CaesarTypeNode outerNode, CaesarTypeNode innerNode) {
		this(false, outerNode, innerNode);
	}

	public OuterInnerRelation(boolean implicit, CaesarTypeNode outerNode, CaesarTypeNode innerNode) {
		super(implicit, outerNode, innerNode);
		outerNode.addEnclosingFor(this);
		innerNode.addEnclosedBy(this);
	}
		
	public CaesarTypeNode getOuterNode() {
		return getFirst();
	}

	public CaesarTypeNode getInnerNode() {
		return getSecond();
	}
}