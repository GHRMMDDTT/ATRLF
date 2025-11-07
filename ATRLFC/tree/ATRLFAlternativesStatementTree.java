package ATRLFC.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
				sb.append("if (").append(
						getCharacterExpressionTree(sequenceExpressionTree).stream().map(subExpresion -> {
									String condition;

									if (subExpresion.size() == 2) {
										condition = String.format("(this.peek() >= %s && this.peek() <= %s)", subExpresion.getFirst().value(), subExpresion.getLast().value());
									} else {
										condition = String.format("this.has(%s)", subExpresion.getFirst().value());
									}

									return condition;
								})
								.collect(Collectors.joining(" || "))
				).append(") {\n").append(sequenceExpressionTree.onVisitor()).append("\n} else ");
			}
			sb.setLength(sb.length() - 6);
		}
		return sb.toString();
	}

	private ArrayList<ArrayList<ATRLFToken>> getCharacterExpressionTree(ATRLFExpressionTree expressionTree) {
		ArrayList<ArrayList<ATRLFToken>> tokens = new ArrayList<>();
		if (expressionTree instanceof ATRLFUnaryExpressionTree unaryExpressionTree) {
			tokens.addAll(getCharacterExpressionTree(unaryExpressionTree.expresion));
		} else if (expressionTree instanceof ATRLFGroupExpressionTree groupExpressionTree3) {
			tokens.addAll(getCharacterExpressionTree(groupExpressionTree3.expresion));
		} else if (expressionTree instanceof ATRLFCharacterExpressionTree characterExpressionTree) {
			tokens.add(new ArrayList<>(List.of(characterExpressionTree.character)));
		} else if (expressionTree instanceof ATRLFAlternativesStatementTree alternativesStatementTree) {
			tokens.addAll(getCharacterExpressionTree(alternativesStatementTree.expressionTrees.getFirst()));
		} else if (expressionTree instanceof ATRLFSequenceStatementTree sequenceStatementTree) {
			tokens.addAll(getCharacterExpressionTree(sequenceStatementTree.expressionTrees.getFirst()));
		} else if (expressionTree instanceof ATRLFRangeCharacterExpressionTree characterExpressionTree) {
			tokens.addAll(new ArrayList<>(
					characterExpressionTree.expression.stream().map(atrlfExpressionTrees -> {
						ArrayList<ATRLFExpressionTree> subExpresion = atrlfExpressionTrees;
						ArrayList<ATRLFToken> tokens1 = new ArrayList<>();

						if (subExpresion.size() == 2) {
							tokens1.add(((ATRLFCharacterExpressionTree) subExpresion.getFirst()).character);
							tokens1.add(((ATRLFCharacterExpressionTree) subExpresion.getLast()).character);
						} else {
							tokens1.add(((ATRLFCharacterExpressionTree) subExpresion.getFirst()).character);
						}

						return tokens1;
					}).collect(Collectors.toList())
			));
		}
		return tokens;
	}
}