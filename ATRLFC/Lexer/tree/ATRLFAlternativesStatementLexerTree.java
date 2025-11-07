package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ATRLFAlternativesStatementLexerTree extends ATRLFStatementLexerTree {
	public final ArrayList<ATRLFStatementLexerTree> expressionTrees;

	public ATRLFAlternativesStatementLexerTree(ArrayList<ATRLFStatementLexerTree> expressionTrees) {
		this.expressionTrees = expressionTrees;
	}

	@Override
	public String onVisitor() {
		StringBuilder sb = new StringBuilder();
		if (expressionTrees.size() == 1) {
			sb.append(expressionTrees.getFirst().onVisitor());
		} else {
			for (ATRLFStatementLexerTree expressionTree : this.expressionTrees) {
				ATRLFSequenceStatementLexerTree sequenceExpressionTree = (ATRLFSequenceStatementLexerTree) expressionTree;
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

	private ArrayList<ArrayList<ATRLFToken>> getCharacterExpressionTree(ATRLFExpressionLexerTree expressionTree) {
		ArrayList<ArrayList<ATRLFToken>> tokens = new ArrayList<>();
		if (expressionTree instanceof ATRLFUnaryExpressionLexerTree unaryExpressionTree) {
			tokens.addAll(getCharacterExpressionTree(unaryExpressionTree.expresion));
		} else if (expressionTree instanceof ATRLFGroupExpressionLexerTree groupExpressionTree3) {
			tokens.addAll(getCharacterExpressionTree(groupExpressionTree3.expresion));
		} else if (expressionTree instanceof ATRLFCharacterExpressionLexerTree characterExpressionTree) {
			tokens.add(new ArrayList<>(List.of(characterExpressionTree.character)));
		} else if (expressionTree instanceof ATRLFAlternativesStatementLexerTree alternativesStatementTree) {
			for (ATRLFExpressionLexerTree expressionTree1 : alternativesStatementTree.expressionTrees) {
				tokens.addAll(getCharacterExpressionTree(expressionTree1));
			}
		} else if (expressionTree instanceof ATRLFSequenceStatementLexerTree sequenceStatementTree) {
			tokens.addAll(getCharacterExpressionTree(sequenceStatementTree.expressionTrees.getFirst()));
		} else if (expressionTree instanceof ATRLFRangeCharacterExpressionLexerTree characterExpressionTree) {
			tokens.addAll(new ArrayList<>(
					characterExpressionTree.expression.stream().map(atrlfExpressionTrees -> {
						ArrayList<ATRLFExpressionLexerTree> subExpresion = atrlfExpressionTrees;
						ArrayList<ATRLFToken> tokens1 = new ArrayList<>();

						if (subExpresion.size() == 2) {
							tokens1.add(((ATRLFCharacterExpressionLexerTree) subExpresion.getFirst()).character);
							tokens1.add(((ATRLFCharacterExpressionLexerTree) subExpresion.getLast()).character);
						} else {
							tokens1.add(((ATRLFCharacterExpressionLexerTree) subExpresion.getFirst()).character);
						}

						return tokens1;
					}).collect(Collectors.toList())
			));
		} else if (expressionTree instanceof ATRLFFunctionLexerTree functionLexerTree) {
			tokens.addAll(getCharacterExpressionTree(functionLexerTree.lexerExpressions));
		} else if (expressionTree instanceof ATRLFFunctionCalledLexerTree functionCalledLexerTree) {
			tokens.addAll(getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value())));
		}
		return tokens;
	}
}