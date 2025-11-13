package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
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
	public String onVisitor(boolean isNot) {
		if (this.operator instanceof ATRLFUnarySingleOperatorExpresionTree single) {
			switch (this.expresion) {
				case ATRLFCharacterExpressionLexerTree character -> {
					switch (single.operator.type()) {
						case PlusSymbolArithmeticalOperatorToken -> {
							return "this.accept(%1$s);\nwhile (this.has(%1$s)) {\nthis.consume();\n}".formatted(character.character.value());
						}
						case QuestionSymbolOperatorToken -> {
							return "if (this.has(%s)) {\nthis.consume();\n}".formatted(character.character.value());
						}
						case NotSymbolOperatorToken -> {
							return "if (!this.has(%s)) {\nthis.consume();\n} else {\nthis.error();\n}".formatted(character.character.value());
						}
						default -> {
							return "this.accept(%s);".formatted(character.character.value());
						}
					}
				}
				case ATRLFGroupExpressionLexerTree group -> {
					switch (single.operator.type()) {
						case PlusSymbolArithmeticalOperatorToken -> {
							return "%1$s\nwhile (%2$s) {\n%1$s\n}".formatted(group.onVisitor(isNot), getCharacterExpressionTreeToString(group, isNot));
						}
						case QuestionSymbolOperatorToken -> {
							return "if (%2$s) {\n%1$s\n}".formatted(group.onVisitor(isNot), getCharacterExpressionTreeToString(group, isNot));
						}
						default -> {
							return group.onVisitor(isNot);
						}
					}
				}
				case ATRLFRangeCharacterExpressionLexerTree characterRange -> {
					switch (single.operator.type()) {
						case PlusSymbolArithmeticalOperatorToken -> {
							return "if (%1$s) {\nthis.consume();\nwhile (%1$s) {\nthis.consume();\n}\n} else {\nthis.error();\n}".formatted(getCharacterExpressionTreeToString(characterRange, isNot));
						}
						case QuestionSymbolOperatorToken -> {
							return "if (%s) {\nthis.consume();\n}".formatted(getCharacterExpressionTreeToString(characterRange, isNot));
						}
						default -> {
							return "if (%s) {\nthis.consume();\n} else {\nthis.error();\n}".formatted(getCharacterExpressionTreeToString(characterRange, isNot));
						}
					}
				}
				case ATRLFFunctionCalledLexerTree function -> {
					switch (single.operator.type()) {
						case PlusSymbolArithmeticalOperatorToken -> {
							return "%1$s\nwhile (%2$s) {\n%1$s\n}".formatted(function.onVisitor(isNot), getCharacterExpressionTreeToString(function, isNot));
						}
						case QuestionSymbolOperatorToken -> {
							return "if (%2$s) {\n%1$s\n}".formatted(function.onVisitor(isNot), getCharacterExpressionTreeToString(function, isNot));
						}
						default -> {
							return function.onVisitor(isNot);
						}
					}
				}
				case ATRLFTokenExpressionLexerTree token -> {
					return token.onVisitor(isNot);
				}
				case ATRLFAnyExpressionLexerTree any -> {
					return any.onVisitor(isNot);
				}
				case ATRLFUnaryExpressionLexerTree unary -> {
					if (unary.operator instanceof ATRLFUnarySingleOperatorExpresionTree subSingle) {
						final boolean isOptionalMore = subSingle.operator.type() == PlusSymbolArithmeticalOperatorToken && single.operator.type() == QuestionSymbolOperatorToken;
						switch (unary.expresion) {
							case ATRLFCharacterExpressionLexerTree character -> {
								if (isOptionalMore) {
									return "while (this.has(%s)) {\nthis.consume();\n}".formatted(character.character.value());
								} else if (single.operator.type() == NotSymbolOperatorToken) {
									switch (subSingle.operator.type()) {
										case QuestionSymbolOperatorToken -> {
											return "if (!this.has(%s)) {\nthis.consume();\n}".formatted(character.character.value());
										}
										case PlusSymbolArithmeticalOperatorToken -> {
											return "if (!this.has(%1$s)) {\nthis.consume();\nwhile(!this.has(%1$s)) {\nthis.consume();\n}\n}".formatted(character.character.value());
										}
									}
								}
							}
							case ATRLFGroupExpressionLexerTree group -> {
								if (isOptionalMore) {
									return "while (%2$s) {\n%1$s\n}".formatted(group.onVisitor(isNot), getCharacterExpressionTreeToString(group, isNot));
								}
							}
							case ATRLFFunctionCalledLexerTree function -> {
								if (isOptionalMore) {
									return "while (%2$s) {\n%1$s\n}".formatted(function.onVisitor(isNot), getCharacterExpressionTreeToString(function, isNot));
								}
							}
							case ATRLFRangeCharacterExpressionLexerTree characterRange -> {
								if (isOptionalMore) {
									return "while (%1$s) {\nthis.consume();\n}".formatted(getCharacterExpressionTreeToString(characterRange, isNot));
								}
							}
							case ATRLFUnaryExpressionLexerTree subUnary -> {
								if (subUnary.operator instanceof ATRLFUnarySingleOperatorExpresionTree preSingle) {
									switch (subUnary.expresion) {
										case ATRLFCharacterExpressionLexerTree character -> {
											if (single.operator.type() == NotSymbolOperatorToken && subSingle.operator.type() == QuestionSymbolOperatorToken && preSingle.operator.type() == PlusSymbolArithmeticalOperatorToken) {
												if (!isNot) return "while (this.has(%1$s)) {\nthis.consume();\n}".formatted(character.character.value());
												return "while (!this.has(%1$s)) {\nthis.consume();\n}".formatted(character.character.value());
											}
										}
										default -> {
											return "?";
										}
									}
								}
							}
							default -> {
								return "??";
							}
						}
					}
				}
				default -> {
					return "???";
				}
			}
		} else if (this.operator instanceof ATRLFUnaryMultipleOperatorExpresionTree unaryMultipleOperatorExpresionTree) {
			switch (this.expresion) {
				case ATRLFCharacterExpressionLexerTree character -> {
					return "{\nint count%1$s = 0;\nwhile(this.has(%2$s)) {\nthis.consume();\ncount%1$s++;\n}\nif (!(%3$s)) {\nthis.error();\n}\n}".formatted(indexOfCount, character, getRangeIndex(unaryMultipleOperatorExpresionTree, indexOfCount++));
				}
				case ATRLFRangeCharacterExpressionLexerTree characterRange -> {
					return "{\nint count%1$s = 0;\nwhile(%2$s) {\nthis.consume();\ncount%1$s++;\n}\nif (!(%3$s)) {\nthis.error();\n}\n}".formatted(indexOfCount, getCharacterExpressionTreeToString(characterRange, isNot), getRangeIndex(unaryMultipleOperatorExpresionTree, indexOfCount++));
				}
				case ATRLFGroupExpressionLexerTree group -> {
					return "{\nint count%1$s = 0;\nwhile(%2$s) {\nthis.consume();\ncount%1$s++;\n}\nif (!(%3$s)) {\nthis.error();\n}\n}".formatted(indexOfCount, getCharacterExpressionTreeToString(group, isNot), getRangeIndex(unaryMultipleOperatorExpresionTree, indexOfCount++));
				}
				case ATRLFFunctionCalledLexerTree function -> {
					return "{\nint count%1$s = 0;\nwhile(%2$s) {\nthis.consume();\ncount%1$s++;\n}\nif (!(%3$s)) {\nthis.error();\n}\n}".formatted(indexOfCount, getCharacterExpressionTreeToString(function, isNot), getRangeIndex(unaryMultipleOperatorExpresionTree, indexOfCount++));
				}
				default -> {
					return "???";
				}
			}
		}
		return "????";
	}

	private String getRangeIndex(ATRLFUnaryMultipleOperatorExpresionTree multiple, int i) {
		return multiple.multiExpresion.stream().map((indexRange) -> {
			if (indexRange.size() == 1) {
				return "%s == count%s".formatted(indexRange.getFirst().value(), i);
			} else if (indexRange.size() == 2) {
				return "(count%1$s >= %2$s && count%1$s <= %3$s)".formatted(i, indexRange.getFirst().value(), indexRange.getLast().value());
			}
			return "nope!";
		}).collect(Collectors.joining(" || "));
	}

	private boolean getNot(ATRLFGroupExpressionLexerTree group) {
		if (group.expresion instanceof ATRLFAlternativesStatementLexerTree alternatives && alternatives.expressionTrees.getFirst() instanceof ATRLFSequenceStatementLexerTree sequence && sequence.expressionTrees.getFirst() instanceof ATRLFUnaryExpressionLexerTree unary&& unary.operator instanceof ATRLFUnarySingleOperatorExpresionTree single) {
			return single.operator.type() == NotSymbolOperatorToken;
		}
		return false;
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
