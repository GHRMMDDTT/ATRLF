package ATRLFC.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.PlusSymbolArithmeticalOperatorToken;

public final class ATRLFUnaryExpressionTree extends ATRLFExpressionTree {
	public final ATRLFToken operator;
	public final ATRLFExpressionTree expresion;

	public ATRLFUnaryExpressionTree(ATRLFToken operator, ATRLFExpressionTree expresion) {
		this.operator = operator;
		this.expresion = expresion;
	}

	@Override
	public String onVisitor() {
		if (this.expresion instanceof ATRLFGroupExpressionTree groupExpressionTree) {
			switch (operator.type()) {
				case PlusSymbolArithmeticalOperatorToken -> {
					return "do {\n" + groupExpressionTree.onVisitor() + " else {\nthis.error();\n}\n} while(has(" + Arrays.stream(getCharacterExpressionTree(groupExpressionTree.expresion).toArray(new ATRLFToken[0])).map(ATRLFToken::value).collect(Collectors.joining(") || has(")) + "));";
				}
				case QuestionSymbolOperatorToken -> {
					return "if (has(" + Arrays.stream(getCharacterExpressionTree(groupExpressionTree.expresion).toArray(new ATRLFToken[0])).map(ATRLFToken::value).collect(Collectors.joining(") || has(")) + ")) {\n" + groupExpressionTree.onVisitor() + "\n}";
				}
				default -> {
					return groupExpressionTree.expresion.onVisitor();
				}
			}
		}
		switch (operator.type()) {
			case PlusSymbolArithmeticalOperatorToken -> {
				return "this.accept(" + this.expresion.onVisitor() + "); while(this.has(" + this.expresion.onVisitor() + ")) { this.consume(); }";
			}
			case QuestionSymbolOperatorToken -> {
				if (this.expresion instanceof ATRLFUnaryExpressionTree unaryExpressionTree && unaryExpressionTree.operator.type() == PlusSymbolArithmeticalOperatorToken) {
					if (unaryExpressionTree.expresion instanceof ATRLFGroupExpressionTree groupExpressionTree) {
						return "while (has(" + Arrays.stream(getCharacterExpressionTree(groupExpressionTree.expresion).toArray(new ATRLFToken[0])).map(ATRLFToken::value).collect(Collectors.joining(") || has(")) + ")) {\n" + groupExpressionTree.onVisitor() + "\n}";
					}
					return "while (this.has(" + unaryExpressionTree.expresion.onVisitor() + ")) { this.consume(); }";
				}
				return "if (this.has(" + this.expresion.onVisitor() + ")) { this.consume(); }";
			}
			default -> {
				if (this.expresion instanceof ATRLFRangeCharacterExpressionTree rangeCharacterExpressionTree) {
					return this.expresion.onVisitor();
				}
				return "this.accept(" + this.expresion.onVisitor() + ");";
			}
		}
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
			tokens.addAll(getCharacterExpressionTreeFirst(alternativesStatementTree));
		} else if (expressionTree instanceof ATRLFSequenceStatementTree sequenceStatementTree) {
			tokens.addAll(getCharacterExpressionTreeFirst(sequenceStatementTree));
		}
		return tokens;
	}

	private ArrayList<ATRLFToken> getCharacterExpressionTreeFirst(ATRLFExpressionTree expressionTree) {
		ArrayList<ATRLFToken> tokens = new ArrayList<>();
		if (expressionTree instanceof ATRLFAlternativesStatementTree alternativesStatementTree) {
			for (ATRLFExpressionTree expressionTree1 : alternativesStatementTree.expressionTrees) {
				if (expressionTree1 instanceof ATRLFUnaryExpressionTree unaryExpressionTree) {
					tokens.addAll(getCharacterExpressionTree(unaryExpressionTree.expresion));
				} else if (expressionTree1 instanceof ATRLFGroupExpressionTree groupExpressionTree3) {
					tokens.addAll(getCharacterExpressionTree(groupExpressionTree3.expresion));
				} else if (expressionTree1 instanceof ATRLFCharacterExpressionTree characterExpressionTree) {
					tokens.add(characterExpressionTree.character);
				} else if (expressionTree1 instanceof ATRLFAlternativesStatementTree subAlternativesStatementTree) {
					tokens.addAll(getCharacterExpressionTree(subAlternativesStatementTree.expressionTrees.getFirst()));
				} else if (expressionTree1 instanceof ATRLFSequenceStatementTree sequenceStatementTree) {
					tokens.addAll(getCharacterExpressionTree(sequenceStatementTree.expressionTrees.getFirst()));
				}
			}
		} else if (expressionTree instanceof ATRLFSequenceStatementTree sequenceStatementTree) {
			for (ATRLFExpressionTree expressionTree1 : sequenceStatementTree.expressionTrees) {
				if (expressionTree1 instanceof ATRLFUnaryExpressionTree unaryExpressionTree) {
					tokens.addAll(getCharacterExpressionTree(unaryExpressionTree.expresion));
				} else if (expressionTree1 instanceof ATRLFGroupExpressionTree groupExpressionTree3) {
					tokens.addAll(getCharacterExpressionTree(groupExpressionTree3.expresion));
				} else if (expressionTree1 instanceof ATRLFCharacterExpressionTree characterExpressionTree) {
					tokens.add(characterExpressionTree.character);
				} else if (expressionTree1 instanceof ATRLFAlternativesStatementTree alternativesStatementTree) {
					tokens.addAll(getCharacterExpressionTree(alternativesStatementTree.expressionTrees.getFirst()));
				} else if (expressionTree1 instanceof ATRLFSequenceStatementTree subSequenceStatementTree) {
					tokens.addAll(getCharacterExpressionTree(subSequenceStatementTree.expressionTrees.getFirst()));
				}
			}
		}
		return tokens;
	}

	@Override
	public String toString() {
		return "[" + operator.value() +
				", " + expresion +
				']';
	}
}
