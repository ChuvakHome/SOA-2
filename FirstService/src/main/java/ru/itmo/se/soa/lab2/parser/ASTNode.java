package ru.itmo.se.soa.lab2.parser;

import java.util.Arrays;

public class ASTNode {
	protected String nodeString;
	protected ASTNode parent;
	protected ASTNode[] children;
	private final ASTNodeType nodeType;
	private final ASTNodeSubtype nodeSubtype;
	
	ASTNode(String nodeString, ASTNode[] children, ASTNodeType nodeType, ASTNodeSubtype nodeSubtype) {
		this.nodeString = nodeString;
		this.children = children;
		
		if (children != null)
			Arrays.stream(this.children).forEachOrdered(node -> {
				if (node != null)
					node.setParent(this);
			});
		
		this.nodeType = nodeType;
		this.nodeSubtype = nodeSubtype;
	}
	
	public String getNodeString() {
		return nodeString;
	}
	
	void setParent(ASTNode parent) {
		this.parent = parent;
	}
	
	public ASTNode getParent() {
		return parent;
	}

	public ASTNodeType getNodeType() {
		return nodeType;
	}

	public ASTNodeSubtype getNodeSubtype() {
		return nodeSubtype;
	}
	
	public static enum ASTNodeType {
		LOGICAL_EXPRESSION,
		EXPRESSION,
		EMPTY_NODE,
	}
	
	public static enum ASTNodeSubtype {
		NODE_FIELD,
		
		NODE_NUMBER,
		NODE_DATE,
		NODE_ENUM,
		NODE_STRING,
		
		NODE_NOT_LOGICAL_OPERATOR,
		
		NODE_AND_LOGICAL_OPERATOR,
		NODE_OR_LOGICAL_OPERATOR,
		
		NODE_LESS_COMPARISON_OPERATOR,
		NODE_LESS_OR_EQUAL_COMPARISON_OPERATOR,
		
		NODE_EQUAL_COMPARISON_OPERATOR,
		NODE_NOT_EQUAL_COMPARISON_OPERATOR,
		
		NODE_GREATER_COMPARISON_OPERATOR,
		NODE_GREATER_OR_EQUAL_COMPARISON_OPERATOR,
	}
}
