package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.*;

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
			switch (this.expresion) {
				case ATRLFGroupExpressionLexerTree groupExpressionTree -> {
					switch (unarySingleOperatorExpresionTree.operator.type()) {
						case PlusSymbolArithmeticalOperatorToken -> {
							return "do {\n" + groupExpressionTree.onVisitor() + " else {\nthis.error();\n}\n} while (" + getCharacterExpressionTree(groupExpressionTree.expresion, false).stream().map(atrlfTokens -> {
								String condition = "";
								if (atrlfTokens.size() == 2) {
									if (atrlfTokens.getFirst().type() == NotToken) condition = "!(";
									condition += String.format("(this.peek() >= %s && this.peek() <= %s)", atrlfTokens.getFirst().value(), atrlfTokens.getLast().value());
								} else {
									if (atrlfTokens.getFirst().type() == NotToken) condition = "!";
									condition += String.format("this.has(%s)", atrlfTokens.getFirst().value());
								}
								return condition;
							}).collect(Collectors.joining(" || ")) + ");";
						}
						case QuestionSymbolOperatorToken -> {
							return "if (" + getCharacterExpressionTree(groupExpressionTree.expresion, false).stream().map(atrlfTokens -> {
								String condition = "";
								if (atrlfTokens.size() == 2) {
									if (atrlfTokens.getFirst().type() == NotToken) condition = "!(";
									condition += String.format("(this.peek() >= %s && this.peek() <= %s)", atrlfTokens.getFirst().value(), atrlfTokens.getLast().value());
									if (atrlfTokens.getFirst().type() == NotToken) condition += ")";
								} else {
									if (atrlfTokens.getFirst().type() == NotToken) condition = "!";
									condition += String.format("this.has(%s)", atrlfTokens.getFirst().value());
								}
								return condition;
							}).collect(Collectors.joining(" || ")) + ") {\n" + groupExpressionTree.onVisitor() + "\n}";
						}
						default -> {
							return groupExpressionTree.expresion.onVisitor();
						}
					}
				}
				case ATRLFRangeCharacterExpressionLexerTree rangeCharacterExpressionTree -> {
					switch (unarySingleOperatorExpresionTree.operator.type()) {
						case PlusSymbolArithmeticalOperatorToken -> {
							return "do {\n" + rangeCharacterExpressionTree.onVisitor() + "\n} while (" + rangeCharacterExpressionTree.expression.stream().map(expressionTrees -> {
								if (expressionTrees.size() == 2) {
									return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().toString(), expressionTrees.getLast().toString());
								} else {
									return String.format("has(%s)", expressionTrees.getFirst().toString());
								}
							}).collect(Collectors.joining(" || ")) + ");";
						}
						case QuestionSymbolOperatorToken -> {
							return "if (" + rangeCharacterExpressionTree.expression.stream().map(expressionTrees -> {
								if (expressionTrees.size() == 2) {
									return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().toString(), expressionTrees.getLast().toString());
								} else {
									return String.format("has(%s)", expressionTrees.getFirst().toString());
								}
							}).collect(Collectors.joining(" || ")) + ") {\n" + rangeCharacterExpressionTree.onVisitor() + "\n}";
						}
						default -> {
							return rangeCharacterExpressionTree.onVisitor();
						}
					}
				}
				case ATRLFAnyExpressionLexerTree _ -> {
					return "this.consume();";
				}
				case ATRLFFunctionCalledLexerTree functionCalledLexerTree -> {
					switch (unarySingleOperatorExpresionTree.operator.type()) {
						case PlusSymbolArithmeticalOperatorToken -> {
							return "do {\n" + functionCalledLexerTree.onVisitor() + "\n} while (" + getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value()), false).stream().map(expressionTrees -> {
								if (expressionTrees.size() == 2) {
									return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value());
								} else {
									return String.format("has(%s)", expressionTrees.getFirst().value());
								}
							}).collect(Collectors.joining(" || ")) + ");";
						}
						case QuestionSymbolOperatorToken -> {
							String value = functionCalledLexerTree.onVisitor();
							return "if (" + getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value()), false).stream().map(expressionTrees -> {
								if (expressionTrees.size() == 2) {
									return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value());
								} else {
									return String.format("has(%s)", expressionTrees.getFirst().value());
								}
							}).collect(Collectors.joining(" || ")) + ") {\n" + value + "\n}";
						}
						default -> {
							return functionCalledLexerTree.onVisitor();
						}
					}
				}
				default -> {
					switch (unarySingleOperatorExpresionTree.operator.type()) {
						case PlusSymbolArithmeticalOperatorToken -> {
							return "this.accept(" + this.expresion.onVisitor() + "); while(this.has(" + this.expresion.onVisitor() + ")) {\nthis.consume();\n}";
						}
						case QuestionSymbolOperatorToken -> {
							if (this.expresion instanceof ATRLFUnaryExpressionLexerTree unaryExpressionTree && unaryExpressionTree.operator instanceof ATRLFUnarySingleOperatorExpresionTree singleOperatorExpresionTree && singleOperatorExpresionTree.operator.type() == PlusSymbolArithmeticalOperatorToken) {
								if (unaryExpressionTree.expresion instanceof ATRLFGroupExpressionLexerTree groupExpressionTree) {
									return "while (" + getCharacterExpressionTree(groupExpressionTree.expresion, false).stream().flatMap(ArrayList::stream)
											.map((atrlfToken -> {
												String condition = "";
												if (atrlfToken.type() == NotToken) condition = "!";
												condition += String.format("this.has(%s)", atrlfToken.value());
												return condition;
											})).collect(Collectors.joining(" || ")) + ") {\n" + groupExpressionTree.onVisitor() + "\n}";
								} else if (unaryExpressionTree.expresion instanceof ATRLFRangeCharacterExpressionLexerTree rangeCharacterExpressionTree) {
									return "while (" + rangeCharacterExpressionTree.expression.stream().map(expressionTrees -> {
										if (expressionTrees.size() == 2) {
											return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().toString(), expressionTrees.getLast().toString());
										} else {
											return String.format("has(%s)", expressionTrees.getFirst().toString());
										}
									}).collect(Collectors.joining(" || ")) + ") {\n" + rangeCharacterExpressionTree.onVisitor() + "\n}";
								} else if (unaryExpressionTree.expresion instanceof ATRLFFunctionCalledLexerTree functionCalledLexerTree) {
									return "while (" +
											getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value()), false)
													.stream()
													.map(expressionTrees -> {
														if (expressionTrees.size() == 2) {
															return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value());
														} else {
															return String.format("has(%s)", expressionTrees.getFirst().value());
														}
													}).collect(Collectors.joining(" || ")) + ") {\n" + functionCalledLexerTree.onVisitor() + "\n}";
								}
								return "while (this.has(" + unaryExpressionTree.expresion.onVisitor() + ")) {\nthis.consume();\n}";
							}
							return "if (this.has(" + this.expresion.onVisitor() + ")) {\nthis.consume();\n}";
						}
						case NotSymbolOperatorToken -> {
							if (this.expresion instanceof ATRLFUnaryExpressionLexerTree unaryExpressionTree && unaryExpressionTree.operator instanceof ATRLFUnarySingleOperatorExpresionTree singleOperatorExpresionTree && singleOperatorExpresionTree.operator.type() == QuestionSymbolOperatorToken) {
								if (unaryExpressionTree.expresion instanceof ATRLFUnaryExpressionLexerTree unaryExpressionLexerTree && unaryExpressionLexerTree.operator instanceof ATRLFUnarySingleOperatorExpresionTree subSingleOperatorExpresionTree && subSingleOperatorExpresionTree.operator.type() == PlusSymbolArithmeticalOperatorToken) {
									return "while (!this.has(" + unaryExpressionLexerTree.expresion.onVisitor() + ")) {\nthis.consume();\n}";
								} else {
									return "if (!this.has(" + unaryExpressionTree.expresion.onVisitor() + ")) {\nthis.consume();\n}";
								}
							} else if (this.expresion instanceof ATRLFUnaryExpressionLexerTree unaryExpressionTree && unaryExpressionTree.operator instanceof ATRLFUnarySingleOperatorExpresionTree singleOperatorExpresionTree && singleOperatorExpresionTree.operator.type() == PlusSymbolArithmeticalOperatorToken) {
								return "do {\nif (!this.has(" + unaryExpressionTree.expresion.onVisitor() + ")) {\nthis.consume();\n} else {\nthis.error();\n}\n} while (!this.has(" + unaryExpressionTree.expresion.onVisitor() + "));";
							}
							return "if (!this.has(" + this.expresion.onVisitor() + ")) {\nthis.consume();\n} else {\nthis.error();\n}";
						}
						default -> {
							return "this.accept(" + this.expresion.onVisitor() + ");";
						}
					}
				}
			}
		} else if (this.operator instanceof ATRLFUnaryMultipleOperatorExpresionTree unaryMultipleOperatorExpresionTree) {
			switch (this.expresion) {
				case ATRLFGroupExpressionLexerTree groupExpressionTree -> {
					return "{\nint count" + (indexOfCount) + " = 0;\nwhile (" + getCharacterExpressionTree(groupExpressionTree.expresion, false).stream().map(expressionTrees -> {
						if (expressionTrees.size() == 2) {
							return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value());
						} else {
							return String.format("has(%s)", expressionTrees.getFirst().toString());
						}
					}).collect(Collectors.joining(" || ")) + ") {\n" + groupExpressionTree.onVisitor() + "\ncount" + (indexOfCount) + "++;\n}\nif (!(" + unaryMultipleOperatorExpresionTree.multiExpresion.stream().map(subExpresion -> {
						String condition;
						if (subExpresion.size() == 2) {
							condition = String.format("(count%1$s >= %2$s && count%1$s <= %3$s)", indexOfCount, subExpresion.getFirst().value(), subExpresion.getLast().value());
						} else {
							condition = String.format("count%s == %s", indexOfCount, subExpresion.getFirst().value());
						}
						return condition;
					}).collect(Collectors.joining(" || ")) + ")) {\nthis.error();\n}\n}";
				}
				case ATRLFRangeCharacterExpressionLexerTree rangeCharacterExpressionTree -> {
					String str = "{\nint count" + (indexOfCount) + " = 0;\nwhile (" + rangeCharacterExpressionTree.expression.stream().map(expressionTrees -> {
						if (expressionTrees.size() == 2) {
							return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().toString(), expressionTrees.getLast().toString());
						} else {
							return String.format("has(%s)", expressionTrees.getFirst().toString());
						}
					}).collect(Collectors.joining(" || ")) + ") {\nthis.consume();\ncount" + (indexOfCount) + "++;\n}\nif (!(" + unaryMultipleOperatorExpresionTree.multiExpresion.stream().map(subExpresion -> {
						String condition;
						if (subExpresion.size() == 2) {
							condition = String.format("(count%1$s >= %2$s && count%1$s <= %3$s)", indexOfCount, subExpresion.getFirst().value(), subExpresion.getLast().value());
						} else {
							condition = String.format("count%s == %s", indexOfCount, subExpresion.getFirst().value());
						}
						return condition;
					}).collect(Collectors.joining(" || ")) + ")) {\nthis.error();\n}\n}";
					indexOfCount++;
					return str;
				}
				case ATRLFFunctionCalledLexerTree functionCalledLexerTree -> {
					String str = "{\nint count" + (indexOfCount) + " = 0;\nwhile (" + getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value()), false).stream().map(expressionTrees -> {
						if (expressionTrees.size() == 2) {
							return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value());
						} else {
							return String.format("has(%s)", expressionTrees.getFirst().value());
						}
					}).collect(Collectors.joining(" || ")) + ") {\nthis.consume();\ncount" + (indexOfCount) + "++;\n}\nif (!(" + unaryMultipleOperatorExpresionTree.multiExpresion.stream().map(subExpresion -> {
						String condition;
						if (subExpresion.size() == 2) {
							condition = String.format("(count%1$s >= %2$s && count%1$s <= %3$s)", indexOfCount, subExpresion.getFirst().value(), subExpresion.getLast().value());
						} else {
							condition = String.format("count%s == %s", indexOfCount, subExpresion.getFirst().value());
						}
						return condition;
					}).collect(Collectors.joining(" || ")) + ")) {\nthis.error();\n}\n}";
					indexOfCount++;
					return str;
				}
				default -> {
					String str = "{\nint count" + (indexOfCount) + " = 0;\nwhile (this.has(" + this.expresion.onVisitor() + ")) {\nthis.consume();\ncount" + (indexOfCount) + "++;\n}\nif (!(" + unaryMultipleOperatorExpresionTree.multiExpresion.stream().map(subExpresion -> {
						String condition;
						if (subExpresion.size() == 2) {
							condition = String.format("(count%1$s >= %2$s && count%1$s <= %3$s)", indexOfCount, subExpresion.getFirst().value(), subExpresion.getLast().value());
						} else {
							condition = String.format("count%s == %s", indexOfCount, subExpresion.getFirst().value());
						}
						return condition;
					}).collect(Collectors.joining(" || ")) + ")) {\nthis.error();\n}\n}";
					indexOfCount++;
					return str;
				}
			}
		}
		return "???";
	}

	@Override
	public String toString() {
		return "[" + operator.toString() +
				", " + expresion +
				']';
	}

	private static class TypeUnaryOperatorExpresionTree { }

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
