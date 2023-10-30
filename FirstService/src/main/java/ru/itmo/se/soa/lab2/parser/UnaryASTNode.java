package ru.itmo.se.soa.lab2.parser;

public class UnaryASTNode extends ASTNode {
	public UnaryASTNode(String nodeString, ASTNode node, ASTNodeType nodeType, ASTNodeSubtype nodeSubtype) {
		super(nodeString, new ASTNode[]{ node }, nodeType, nodeSubtype);
	}
	
	public ASTNode getChild() {
		return children[0];
	}
	
	void setChild(ASTNode node) {
		children[0] = node;
	}
}
