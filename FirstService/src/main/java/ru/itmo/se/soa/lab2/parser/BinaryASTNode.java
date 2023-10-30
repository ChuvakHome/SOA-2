package ru.itmo.se.soa.lab2.parser;

public class BinaryASTNode extends ASTNode {
	public BinaryASTNode(String nodeString, ASTNode leftNode, ASTNode rightNode, ASTNodeType nodeType, ASTNodeSubtype nodeSubtype) {
		super(nodeString, new ASTNode[]{ leftNode, rightNode }, nodeType, nodeSubtype);
	}
	
	void setLeftChild(ASTNode leftNode) {
		children[0] = leftNode;
	}
	
	public ASTNode getLeftChild() {
		return children[0];
	}
	
	void setRightChild(ASTNode rightNode) {
		children[1] = rightNode;
	}
	
	public ASTNode getRightChild() {
		return children[1];
	}
}
