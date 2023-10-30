package ru.itmo.se.soa.lab2.parser;

import java.util.Map;

public class BinaryLogicalOperatorASTNode extends BinaryASTNode {
	private static Map<String, ASTNodeSubtype> binlogopSubtypeMap = Map.of(
		"or", ASTNodeSubtype.NODE_OR_LOGICAL_OPERATOR,
		"and", ASTNodeSubtype.NODE_AND_LOGICAL_OPERATOR
	);
	
	private static Map<ASTNodeSubtype, Integer> binlogopPriorityMap = Map.of(
		ASTNodeSubtype.NODE_OR_LOGICAL_OPERATOR, 1,
		ASTNodeSubtype.NODE_AND_LOGICAL_OPERATOR, 2
	);
	
	private final int priority;
	
	public BinaryLogicalOperatorASTNode(String nodeString, ASTNode leftNode, ASTNode rightNode) {
		super(nodeString, leftNode, rightNode, ASTNodeType.LOGICAL_EXPRESSION, binlogopSubtypeMap.get(nodeString));
		
		priority = binlogopPriorityMap.getOrDefault(binlogopSubtypeMap.get(nodeString), -1);
	}
	
	public int getPriority() {
		return priority;
	}
}
