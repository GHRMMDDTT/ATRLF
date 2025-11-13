package ATRLFC.Lexer.tree;

import java.util.stream.Collectors;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.*;
import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.NotToken;

public class IDKTree {
	/* private String getATRLFDefaultVisitorString$Single(ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree unarySingleOperatorExpresionTree) {
		switch (unarySingleOperatorExpresionTree.operator.type()) {
			case PlusSymbolArithmeticalOperatorToken -> {
				return "this.accept(" + this.expresion.onVisitor() + "); while(this.has(" + this.expresion.onVisitor() + ")) {\nthis.consume();\n}";
			}
			case QuestionSymbolOperatorToken -> {
				if (this.expresion instanceof ATRLFUnaryExpressionLexerTree unaryExpressionTree && unaryExpressionTree.operator instanceof ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree singleOperatorExpresionTree && singleOperatorExpresionTree.operator.type() == PlusSymbolArithmeticalOperatorToken) {
					if (unaryExpressionTree.expresion instanceof ATRLFGroupExpressionLexerTree groupExpressionTree) {
						return "while (" + getCharacterExpressionTree(groupExpressionTree.expresion, false).stream().map(atrlfTokens -> {
							if (atrlfTokens.size() == 2) {
								return String.format("(this.peek() >= %s && this.peek() <= %s)", atrlfTokens.getFirst().value(), atrlfTokens.getLast().value());
							} else {
								return String.format("this.has(%s)", atrlfTokens.getFirst().value());
							}
						}).collect(Collectors.joining(" || ")) + ") {\n" + groupExpressionTree.onVisitor() + "\n}";
					} else if (unaryExpressionTree.expresion instanceof ATRLFRangeCharacterExpressionLexerTree rangeCharacterExpressionTree) {
						return "while (" + String.format("(this.peek() >= %s && this.peek() <= %s)", rangeCharacterExpressionTree.minimum.value(), rangeCharacterExpressionTree.maximum.value()) + ") {\nthis.consume();\n}";
					} else if (unaryExpressionTree.expresion instanceof ATRLFFunctionCalledLexerTree functionCalledLexerTree) {
						return "while (" +
								getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value()), false)
										.stream()
										.map(expressionTrees -> {
											if (expressionTrees.size() == 2) {
												return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value());
											} else {
												return String.format("this.has(%s)", expressionTrees.getFirst().value());
											}
										}).collect(Collectors.joining(" || ")) + ") {\n" + functionCalledLexerTree.onVisitor() + "\n}";
					}
					return "while (this.has(" + unaryExpressionTree.expresion.onVisitor() + ")) {\nthis.consume();\n}";
				}
				return "if (this.has(" + this.expresion.onVisitor() + ")) {\nthis.consume();\n}";
			}
			case NotSymbolOperatorToken -> {
				if (this.expresion instanceof ATRLFUnaryExpressionLexerTree unaryExpressionTree && unaryExpressionTree.operator instanceof ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree singleOperatorExpresionTree && singleOperatorExpresionTree.operator.type() == QuestionSymbolOperatorToken) {
					if (unaryExpressionTree.expresion instanceof ATRLFUnaryExpressionLexerTree unaryExpressionLexerTree && unaryExpressionLexerTree.operator instanceof ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree subSingleOperatorExpresionTree && subSingleOperatorExpresionTree.operator.type() == PlusSymbolArithmeticalOperatorToken) {
						return "while (!this.has(" + unaryExpressionLexerTree.expresion.onVisitor() + ")) {\nthis.consume();\n}";
					} else {
						return "if (!this.has(" + unaryExpressionTree.expresion.onVisitor() + ")) {\nthis.consume();\n}";
					}
				} else if (this.expresion instanceof ATRLFUnaryExpressionLexerTree unaryExpressionTree && unaryExpressionTree.operator instanceof ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree singleOperatorExpresionTree && singleOperatorExpresionTree.operator.type() == PlusSymbolArithmeticalOperatorToken) {
					return "do {\nif (!this.has(" + unaryExpressionTree.expresion.onVisitor() + ")) {\nthis.consume();\n} else {\nthis.error();\n}\n} while (!this.has(" + unaryExpressionTree.expresion.onVisitor() + "));";
				}
				return "if (!this.has(" + this.expresion.onVisitor() + ")) {\nthis.consume();\n} else {\nthis.error();\n}";
			}
			default -> {
				return "this.accept(" + this.expresion.onVisitor() + ");";
			}
		}
	} */

	/* private String getATRLFFunctionCallVisitorString$Single(ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree unarySingleOperatorExpresionTree, ATRLFFunctionCalledLexerTree functionCalledLexerTree) {
		switch (unarySingleOperatorExpresionTree.operator.type()) {
			case PlusSymbolArithmeticalOperatorToken -> {
				return "do {\n" + functionCalledLexerTree.onVisitor() + "\n} while (" + getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value()), false).stream().map(expressionTrees -> {
					if (expressionTrees.size() == 2) {
						return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value());
					} else {
						return String.format("this.has(%s)", expressionTrees.getFirst().value());
					}
				}).collect(Collectors.joining(" || ")) + ");";
			}
			case QuestionSymbolOperatorToken -> {
				String value = functionCalledLexerTree.onVisitor();
				return "if (" + getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value()), false).stream().map(expressionTrees -> {
					if (expressionTrees.size() == 2) {
						return String.format("(this.peek() >= %s && this.peek() <= %s)", expressionTrees.getFirst().value(), expressionTrees.getLast().value());
					} else {
						return String.format("this.has(%s)", expressionTrees.getFirst().value());
					}
				}).collect(Collectors.joining(" || ")) + ") {\n" + value + "\n}";
			}
			default -> {
				return functionCalledLexerTree.onVisitor();
			}
		}
	} */

	/* private static String getATRLFCharacterRangeVisitorString$Single(ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree unarySingleOperatorExpresionTree, ATRLFRangeCharacterExpressionLexerTree rangeCharacterExpressionTree) {
		final String range = String.format("(this.peek() >= %s && this.peek() <= %s)", rangeCharacterExpressionTree.minimum.value(), rangeCharacterExpressionTree.maximum.value());
		switch (unarySingleOperatorExpresionTree.operator.type()) {
			case PlusSymbolArithmeticalOperatorToken -> {
				return "do {\nthis.consume();\n} while (" + range + ");";
			}
			case QuestionSymbolOperatorToken -> {
				return "if (" + range + ") {\nthis.consume();\n}";
			}
			default -> {
				return rangeCharacterExpressionTree.onVisitor();
			}
		}
	} */

	/* private String getATRLFGroupVisitorString$Single(ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree unarySingleOperatorExpresionTree, ATRLFGroupExpressionLexerTree groupExpressionTree) {
		switch (unarySingleOperatorExpresionTree.operator.type()) {
			case PlusSymbolArithmeticalOperatorToken -> {
				return "do {\n" + groupExpressionTree.onVisitor() + "\n} while (" + getCharacterExpressionTree(groupExpressionTree.expresion, false).stream().map(atrlfTokens -> {
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
	} */
}
