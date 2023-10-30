package ru.itmo.se.soa.lab2.parser;

import java.util.Map;

public class UnaryLogicalOperatorASTNode extends UnaryASTNode {
	private static Map<String, ASTNodeSubtype> unarySubtypeMap = Map.of(
		"not", ASTNodeSubtype.NODE_NOT_LOGICAL_OPERATOR
	);
	
	public UnaryLogicalOperatorASTNode(String nodeString, ASTNode node) {
		super(nodeString, node, ASTNodeType.LOGICAL_EXPRESSION, unarySubtypeMap.get(nodeString));
	}
}
