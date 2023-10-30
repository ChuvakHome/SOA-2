package ru.itmo.se.soa.lab2.parser;

import java.util.Map;

public class BinaryComparisonOperatorASTNode extends BinaryASTNode {
	private static Map<String, ASTNodeSubtype> comparisonSubtypeMap = Map.of(
			">", ASTNodeSubtype.NODE_GREATER_COMPARISON_OPERATOR,
			">=", ASTNodeSubtype.NODE_GREATER_OR_EQUAL_COMPARISON_OPERATOR,
			
			"=", ASTNodeSubtype.NODE_EQUAL_COMPARISON_OPERATOR,
			"!=", ASTNodeSubtype.NODE_NOT_EQUAL_COMPARISON_OPERATOR,
			
			"<", ASTNodeSubtype.NODE_LESS_COMPARISON_OPERATOR,
			"<=", ASTNodeSubtype.NODE_LESS_OR_EQUAL_COMPARISON_OPERATOR
	);
	
	public BinaryComparisonOperatorASTNode(String nodeString, ASTNode leftNode, ASTNode rightNode) {
		super(nodeString, leftNode, rightNode, ASTNodeType.EXPRESSION, comparisonSubtypeMap.get(nodeString));
	}
}
