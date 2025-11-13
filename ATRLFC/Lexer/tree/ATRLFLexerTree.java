package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.NotToken;

public abstract class ATRLFLexerTree {
	public ATRLFCompilationUnitLexerTree compilationUnit;

	public abstract String onVisitor(boolean isNot);

	protected String getCharacterExpressionTreeToString(ATRLFExpressionLexerTree expressionTree, Boolean isNot, boolean is) {
		String collect;
		if (isNot == null) {
			collect = getCharacterExpressionTree(expressionTree, false).stream().map(new Function<ArrayList<ATRLFToken>, String>() {
				@Override
				public String apply(ArrayList<ATRLFToken> atrlfTokens) {
					if (atrlfTokens.size() == 2) {
						return (atrlfTokens.getFirst().type() != NotToken && atrlfTokens.getLast().type() != NotToken && !is? "!" : "") + "(this.peek() >= %s && this.peek() <= %s)".formatted(atrlfTokens.getFirst().value(), atrlfTokens.getLast().value());
					} else if (atrlfTokens.size() == 1) {
						return (atrlfTokens.getFirst().type() != NotToken & !is ? "!" : "") + "this.has(%s)".formatted(atrlfTokens.getFirst().value());
					}
					return "?";
				}
			}).collect(Collectors.joining(" || "));
		} else {
			collect = getCharacterExpressionTree(expressionTree).stream().map(new Function<ArrayList<ATRLFToken>, String>() {
				@Override
				public String apply(ArrayList<ATRLFToken> atrlfTokens) {
					if (atrlfTokens.size() == 2) {
						return "(this.peek() >= %s && this.peek() <= %s)".formatted(atrlfTokens.getFirst().value(), atrlfTokens.getLast().value());
					} else if (atrlfTokens.size() == 1) {
						return "this.has(%s)".formatted(atrlfTokens.getFirst().value());
					}
					return "?";
				}
			}).collect(Collectors.joining(" || "));
			if (isNot) return "!(%s)".formatted(collect);
		}
		return collect;
	}

	protected String getCharacterExpressionTreeToString(ATRLFExpressionLexerTree expressionTree, Boolean isNot) {
		return getCharacterExpressionTreeToString(expressionTree, isNot, false);
	}

	protected ArrayList<ArrayList<ATRLFToken>> getCharacterExpressionTree(ATRLFExpressionLexerTree expressionTree, boolean isNot) {
		ArrayList<ArrayList<ATRLFToken>> tokens = new ArrayList<>();
		if (expressionTree instanceof ATRLFUnaryExpressionLexerTree unaryExpressionTree) {
			if (unaryExpressionTree.operator instanceof ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree singleOperatorExpresionTree) {
				tokens.addAll(getCharacterExpressionTree(unaryExpressionTree.expresion, singleOperatorExpresionTree.operator.type() == ATRLFToken.ATRLFTokenType.NotSymbolOperatorToken));
			} else {
				tokens.addAll(getCharacterExpressionTree(unaryExpressionTree.expresion, false));
			}
		} else if (expressionTree instanceof ATRLFGroupExpressionLexerTree groupExpressionTree3) {
			tokens.addAll(getCharacterExpressionTree(groupExpressionTree3.expresion, isNot));
		} else if (expressionTree instanceof ATRLFCharacterExpressionLexerTree characterExpressionTree) {
			if (isNot) {
				tokens.add(new ArrayList<>(List.of(new ATRLFToken(characterExpressionTree.character.value(), NotToken, characterExpressionTree.character.line(), characterExpressionTree.character.column()))));
			} else {
				tokens.add(new ArrayList<>(List.of(characterExpressionTree.character)));
			}
		} else if (expressionTree instanceof ATRLFAnyExpressionLexerTree anyExpressionLexerTree) {
			tokens.add(new ArrayList<>(List.of(new ATRLFToken("true", ATRLFToken.ATRLFTokenType.AllToken, anyExpressionLexerTree.character.line(), anyExpressionLexerTree.character.column()))));
		} else if (expressionTree instanceof ATRLFAlternativesStatementLexerTree alternativesStatementTree) {
			for (ATRLFExpressionLexerTree expressionTree1 : alternativesStatementTree.expressionTrees) {
				tokens.addAll(getCharacterExpressionTree(expressionTree1, isNot));
			}
		} else if (expressionTree instanceof ATRLFSequenceStatementLexerTree sequenceStatementTree) {
			tokens.addAll(getCharacterExpressionTree(sequenceStatementTree.expressionTrees.getFirst(), isNot));
		} else if (expressionTree instanceof ATRLFRangeCharacterExpressionLexerTree characterExpressionTree) {
			if (isNot) {
				tokens.add(new ArrayList<>(List.of(new ATRLFToken(characterExpressionTree.minimum.value(), NotToken, characterExpressionTree.minimum.line(), characterExpressionTree.minimum.column()), new ATRLFToken(characterExpressionTree.maximum.value(), NotToken, characterExpressionTree.maximum.line(), characterExpressionTree.maximum.column()))));
			} else {
				tokens.add(new ArrayList<>(Arrays.asList(characterExpressionTree.minimum, characterExpressionTree.maximum)));
			}
		} else if (expressionTree instanceof ATRLFFunctionLexerTree functionLexerTree) {
			tokens.addAll(getCharacterExpressionTree(functionLexerTree.lexerExpressions, isNot));
		} else if (expressionTree instanceof ATRLFFunctionCalledLexerTree functionCalledLexerTree) {
			tokens.addAll(getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value()), isNot));
		} else if (expressionTree instanceof ATRLFTokenExpressionLexerTree tokenExpressionLexerTree) {
			tokens.addAll(getCharacterExpressionTree(tokenExpressionLexerTree.expressionLexerTree));
		}
		return tokens;
	}

	protected ArrayList<ArrayList<ATRLFToken>> getCharacterExpressionTree(ATRLFExpressionLexerTree expressionTree) {
		ArrayList<ArrayList<ATRLFToken>> tokens = new ArrayList<>();
		if (expressionTree instanceof ATRLFUnaryExpressionLexerTree unaryExpressionTree) {
			if (unaryExpressionTree.operator instanceof ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree singleOperatorExpresionTree) {
				tokens.addAll(getCharacterExpressionTree(unaryExpressionTree.expresion));
			} else {
				tokens.addAll(getCharacterExpressionTree(unaryExpressionTree.expresion));
			}
		} else if (expressionTree instanceof ATRLFGroupExpressionLexerTree groupExpressionTree3) {
			tokens.addAll(getCharacterExpressionTree(groupExpressionTree3.expresion));
		} else if (expressionTree instanceof ATRLFCharacterExpressionLexerTree characterExpressionTree) {
			tokens.add(new ArrayList<>(List.of(characterExpressionTree.character)));
		} else if (expressionTree instanceof ATRLFAnyExpressionLexerTree anyExpressionLexerTree) {
			tokens.add(new ArrayList<>(List.of(new ATRLFToken("true", ATRLFToken.ATRLFTokenType.AllToken, anyExpressionLexerTree.character.line(), anyExpressionLexerTree.character.column()))));
		} else if (expressionTree instanceof ATRLFTokenExpressionLexerTree tokenExpressionLexerTree) {
			tokens.addAll(getCharacterExpressionTree(tokenExpressionLexerTree.expressionLexerTree));
		} else if (expressionTree instanceof ATRLFAlternativesStatementLexerTree alternativesStatementTree) {
			for (ATRLFExpressionLexerTree expressionTree1 : alternativesStatementTree.expressionTrees) {
				tokens.addAll(getCharacterExpressionTree(expressionTree1));
			}
		} else if (expressionTree instanceof ATRLFSequenceStatementLexerTree sequenceStatementTree) {
			tokens.addAll(getCharacterExpressionTree(sequenceStatementTree.expressionTrees.getFirst()));
		} else if (expressionTree instanceof ATRLFRangeCharacterExpressionLexerTree characterExpressionTree) {
			tokens.add(new ArrayList<>(Arrays.asList(characterExpressionTree.minimum, characterExpressionTree.maximum)));
		} else if (expressionTree instanceof ATRLFFunctionLexerTree functionLexerTree) {
			tokens.addAll(getCharacterExpressionTree(functionLexerTree.lexerExpressions));
		} else if (expressionTree instanceof ATRLFFunctionCalledLexerTree functionCalledLexerTree) {
			tokens.addAll(getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value())));
		}
		return tokens;
	}
}