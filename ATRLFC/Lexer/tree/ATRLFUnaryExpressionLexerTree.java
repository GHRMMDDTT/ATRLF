package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.PlusSymbolArithmeticalOperatorToken;

public final class ATRLFUnaryExpressionLexerTree extends ATRLFExpressionLexerTree {
	public final TypeUnaryOperatorExpresionTree operator;
	public final ATRLFExpressionLexerTree expresion;

	private static int indexOfCount = 0;

	public ATRLFUnaryExpressionLexerTree(TypeUnaryOperatorExpresionTree operator, ATRLFExpressionLexerTree expresion) {
		this.operator = operator;
		this.expresion = expresion;
	}

	@Override
	public String onVisitor() {
		if (this.operator instanceof ATRLFUnarySingleOperatorExpresionTree unarySingleOperatorExpresionTree) {
			if (this.expresion instanceof ATRLFGroupExpressionLexerTree groupExpressionTree) {
				switch (unarySingleOperatorExpresionTree.operator.type()) {
					case PlusSymbolArithmeticalOperatorToken -> {
						return "do {\n" + groupExpressionTree.onVisitor() + " else {\nthis.error();\n}\n} while (" + getCharacterExpressionTree(groupExpressionTree.expresion).stream().map(atrlfTokens -> { String condition; if (atrlfTokens.size() == 2) { condition = String.format("(this.peek() >= %s && this.peek() <= %s)", atrlfTokens.getFirst().value(), atrlfTokens.getLast().value()); } else { condition = String.format("this.has(%s)", atrlfTokens.getFirst().value()); } return condition; }).collect(Collectors.joining(" || ")) + ");";
					}
					case QuestionSymbolOperatorToken -> {
						return "if (" + getCharacterExpressionTree(groupExpressionTree.expresion).stream().map(atrlfTokens -> { String condition; if (atrlfTokens.size() == 2) { condition = String.format("(this.peek() >= %s && this.peek() <= %s)", atrlfTokens.getFirst().value(), atrlfTokens.getLast().value()); } else { condition = String.format("this.has(%s)", atrlfTokens.getFirst().value()); } return condition; }).collect(Collectors.joining(" || ")) + ") {\n" + groupExpressionTree.onVisitor() + "\n}";
					}
					default -> {
						return groupExpressionTree.expresion.onVisitor();
					}
				}
			} else if (this.expresion instanceof ATRLFRangeCharacterExpressionLexerTree rangeCharacterExpressionTree) {
				switch (unarySingleOperatorExpresionTree.operator.type()) {
					case PlusSymbolArithmeticalOperatorToken -> {
						return "do {\n" + rangeCharacterExpressionTree.onVisitor() + "\n} while (" + rangeCharacterExpressionTree.expression.stream().map(expressionTrees -> { if (expressionTrees.size() == 2) { return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().toString(), expressionTrees.getLast().toString()); } else { return String.format("has(%s)", expressionTrees.getFirst().toString()); } }).collect(Collectors.joining(" || ")) + ");";
					}
					case QuestionSymbolOperatorToken -> {
						return "if (" + rangeCharacterExpressionTree.expression.stream().map(expressionTrees -> { if (expressionTrees.size() == 2) { return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().toString(), expressionTrees.getLast().toString()); } else { return String.format("has(%s)", expressionTrees.getFirst().toString()); } }).collect(Collectors.joining(" || ")) + ") {\n" + rangeCharacterExpressionTree.onVisitor() + "\n}";
					}
					default -> {
						return rangeCharacterExpressionTree.onVisitor();
					}
				}
			} else if (this.expresion instanceof ATRLFFunctionCalledLexerTree functionCalledLexerTree) {
				switch (unarySingleOperatorExpresionTree.operator.type()) {
					case PlusSymbolArithmeticalOperatorToken -> {
						return "do {\n" + functionCalledLexerTree.onVisitor() + "\n} while (" + getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value())).stream().map(expressionTrees -> { if (expressionTrees.size() == 2) { return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value()); } else { return String.format("has(%s)", expressionTrees.getFirst().value()); } }).collect(Collectors.joining(" || ")) + ");";
					}
					case QuestionSymbolOperatorToken -> {
						return "if (" + getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value())).stream().map(expressionTrees -> { if (expressionTrees.size() == 2) { return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value()); } else { return String.format("has(%s)", expressionTrees.getFirst().value()); } }).collect(Collectors.joining(" || ")) + ") {\n" + functionCalledLexerTree.onVisitor() + "\n}";
					}
					default -> {
						return functionCalledLexerTree.onVisitor();
					}
				}
			} else {
				switch (unarySingleOperatorExpresionTree.operator.type()) {
					case PlusSymbolArithmeticalOperatorToken -> {
						return "this.accept(" + this.expresion.onVisitor() + "); while(this.has(" + this.expresion.onVisitor() + ")) { this.consume(); }";
					}
					case QuestionSymbolOperatorToken -> {
						if (this.expresion instanceof ATRLFUnaryExpressionLexerTree unaryExpressionTree && unaryExpressionTree.operator instanceof ATRLFUnarySingleOperatorExpresionTree singleOperatorExpresionTree && singleOperatorExpresionTree.operator.type() == PlusSymbolArithmeticalOperatorToken) {
							if (unaryExpressionTree.expresion instanceof ATRLFGroupExpressionLexerTree groupExpressionTree) {
								return "while (has(" + getCharacterExpressionTree(groupExpressionTree.expresion).stream().flatMap(ArrayList::stream)
								.map(ATRLFToken::value).collect(Collectors.joining(") || has(")) + ")) {\n" + groupExpressionTree.onVisitor() + "\n}";
							} else if (unaryExpressionTree.expresion instanceof ATRLFRangeCharacterExpressionLexerTree rangeCharacterExpressionTree) {
								return "while (" + rangeCharacterExpressionTree.expression.stream().map(expressionTrees -> { if (expressionTrees.size() == 2) { return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().toString(), expressionTrees.getLast().toString()); } else { return String.format("has(%s)", expressionTrees.getFirst().toString()); } }).collect(Collectors.joining(" || ")) + ") {\n" + rangeCharacterExpressionTree.onVisitor() + "\n}";
							} else if (unaryExpressionTree.expresion instanceof ATRLFFunctionCalledLexerTree functionCalledLexerTree) {
								return "while (" +
									getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value()))
									.stream()
									.map(expressionTrees -> {
										if (expressionTrees.size() == 2) {
											return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value());
										} else {
											return String.format("has(%s)", expressionTrees.getFirst().value());
										}
									}).collect(Collectors.joining(" || ")) + ") {\n" + functionCalledLexerTree.onVisitor() + "\n}";
							}
							return "while (this.has(" + unaryExpressionTree.expresion.onVisitor() + ")) { this.consume(); }";
						}
						return "if (this.has(" + this.expresion.onVisitor() + ")) { this.consume(); }";
					}
					default -> {
						return "this.accept(" + this.expresion.onVisitor() + ");";
					}
				}
			}
		} else if (this.operator instanceof ATRLFUnaryMultipleOperatorExpresionTree unaryMultipleOperatorExpresionTree) {
			if (this.expresion instanceof ATRLFGroupExpressionLexerTree groupExpressionTree) {
				return "{\nint count" + (indexOfCount) + " = 0;\nwhile (" + getCharacterExpressionTree(groupExpressionTree.expresion).stream().map(expressionTrees -> { if (expressionTrees.size() == 2) { return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value()); } else { return String.format("has(%s)", expressionTrees.getFirst().toString()); } }).collect(Collectors.joining(" || ")) + ") {\n" + groupExpressionTree.onVisitor() + "\ncount" + (indexOfCount) + "++;\n}\nif (!(" + unaryMultipleOperatorExpresionTree.multiExpresion.stream().map(subExpresion -> { String condition; if (subExpresion.size() == 2) { condition = String.format("(count%1$s >= %2$s && count%1$s <= %3$s)", indexOfCount, subExpresion.getFirst().value(), subExpresion.getLast().value()); } else { condition = String.format("count%s == %s", indexOfCount, subExpresion.getFirst().value()); } return condition; }).collect(Collectors.joining(" || ")) + ")) {\nthis.error();\n}\n}";
			} else if (this.expresion instanceof ATRLFRangeCharacterExpressionLexerTree rangeCharacterExpressionTree) {
				String str = "{\nint count" + (indexOfCount) + " = 0;\nwhile (" + rangeCharacterExpressionTree.expression.stream().map(expressionTrees -> { if (expressionTrees.size() == 2) { return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().toString(), expressionTrees.getLast().toString()); } else { return String.format("has(%s)", expressionTrees.getFirst().toString()); } }).collect(Collectors.joining(" || ")) + ") {\nthis.consume();\ncount" + (indexOfCount) + "++;\n}\nif (!(" + unaryMultipleOperatorExpresionTree.multiExpresion.stream().map(subExpresion -> { String condition; if (subExpresion.size() == 2) { condition = String.format("(count%1$s >= %2$s && count%1$s <= %3$s)", indexOfCount, subExpresion.getFirst().value(), subExpresion.getLast().value()); } else { condition = String.format("count%s == %s", indexOfCount, subExpresion.getFirst().value()); } return condition; }).collect(Collectors.joining(" || ")) + ")) {\nthis.error();\n}\n}";
				indexOfCount++;
				return str;
			} else if (this.expresion instanceof ATRLFFunctionCalledLexerTree functionCalledLexerTree) {
				String str = "{\nint count" + (indexOfCount) + " = 0;\nwhile (" + getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value())).stream().map(expressionTrees -> { if (expressionTrees.size() == 2) { return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value()); } else { return String.format("has(%s)", expressionTrees.getFirst().value()); } }).collect(Collectors.joining(" || ")) + ") {\nthis.consume();\ncount" + (indexOfCount) + "++;\n}\nif (!(" + unaryMultipleOperatorExpresionTree.multiExpresion.stream().map(subExpresion -> { String condition; if (subExpresion.size() == 2) { condition = String.format("(count%1$s >= %2$s && count%1$s <= %3$s)", indexOfCount, subExpresion.getFirst().value(), subExpresion.getLast().value()); } else { condition = String.format("count%s == %s", indexOfCount, subExpresion.getFirst().value()); } return condition; }).collect(Collectors.joining(" || ")) + ")) {\nthis.error();\n}\n}";
				indexOfCount++;
				return str;
			} else {
				String str = "{\nint count" + (indexOfCount) + " = 0;\nwhile (this.has(" + this.expresion.onVisitor() + ")) {\nthis.consume();\ncount" + (indexOfCount) + "++;\n}\nif (!(" + unaryMultipleOperatorExpresionTree.multiExpresion.stream().map(subExpresion -> { String condition; if (subExpresion.size() == 2) { condition = String.format("(count%1$s >= %2$s && count%1$s <= %3$s)", indexOfCount, subExpresion.getFirst().value(), subExpresion.getLast().value()); } else { condition = String.format("count%s == %s", indexOfCount, subExpresion.getFirst().value()); } return condition; }).collect(Collectors.joining(" || ")) + ")) {\nthis.error();\n}\n}";
				indexOfCount++;
				return str;
			}
		}
		return "???";
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

	@Override
	public String toString() {
		return "[" + operator.toString() +
				", " + expresion +
				']';
	}

	private static class TypeUnaryOperatorExpresionTree {}

	public static final class ATRLFUnarySingleOperatorExpresionTree extends TypeUnaryOperatorExpresionTree {
		public final ATRLFToken operator;

		public ATRLFUnarySingleOperatorExpresionTree(ATRLFToken operator) { this.operator = operator; }

		@Override
		public String toString() {
			return "[" + operator +
					']';
		}
	}

	public static final class ATRLFUnaryMultipleOperatorExpresionTree extends TypeUnaryOperatorExpresionTree {
		public final ArrayList<ArrayList<ATRLFToken>> multiExpresion;

		public ATRLFUnaryMultipleOperatorExpresionTree(ArrayList<ArrayList<ATRLFToken>> multiExpresion) { this.multiExpresion = multiExpresion; }

		@Override
		public String toString() {
			return "[" + multiExpresion +
					']';
		}
	}
}
