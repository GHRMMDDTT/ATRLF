package ATRLFC.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class ATRLFAlternativesStatementTree extends ATRLFStatementTree {
	public final ArrayList<ATRLFStatementTree> expressionTrees;

	public ATRLFAlternativesStatementTree(ArrayList<ATRLFStatementTree> expressionTrees) {
		this.expressionTrees = expressionTrees;
	}

	@Override
	public String onVisitor() {
		StringBuilder sb = new StringBuilder();
		if (expressionTrees.size() == 1) {
			sb.append(expressionTrees.getFirst().onVisitor());
		} else {
			for (ATRLFStatementTree expressionTree : this.expressionTrees) {
				ATRLFSequenceStatementTree sequenceExpressionTree = (ATRLFSequenceStatementTree) expressionTree;
				sb.append("if (has(").append(Arrays.stream(getCharacterExpressionTree(sequenceExpressionTree).toArray(new ATRLFToken[0])).map(ATRLFToken::value).collect(Collectors.joining(") || has("))).append(")) {\n").append(sequenceExpressionTree.onVisitor()).append("\n} else ");
			}
			sb.setLength(sb.length() - 6);
		}
		return sb.toString();
	}

	private ArrayList<ATRLFToken> getCharacterExpressionTree(ATRLFExpressionTree expressionTree) {
		ArrayList<ATRLFToken> tokens = new ArrayList<>();
		if (expressionTree instanceof ATRLFUnaryExpressionTree unaryExpressionTree) {
			tokens.addAll(getCharacterExpressionTree(unaryExpressionTree.expresion));
		} else if (expressionTree instanceof ATRLFGroupExpressionTree groupExpressionTree3) {
			tokens.addAll(getCharacterExpressionTree(groupExpressionTree3.expresion));
		} else if (expressionTree instanceof ATRLFCharacterExpressionTree characterExpressionTree) {
			tokens.add(characterExpressionTree.character);
		} else if (expressionTree instanceof ATRLFAlternativesStatementTree alternativesStatementTree) {
			tokens.addAll(getCharacterExpressionTree(alternativesStatementTree.expressionTrees.getFirst()));
		} else if (expressionTree instanceof ATRLFSequenceStatementTree sequenceStatementTree) {
			tokens.addAll(getCharacterExpressionTree(sequenceStatementTree.expressionTrees.getFirst()));
		}
		return tokens;
	}
}