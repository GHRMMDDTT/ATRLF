package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.NotToken;

public abstract class ATRLFLexerTree {
	public ATRLFCompilationUnitLexerTree compilationUnit;

	public abstract String onVisitor();

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
			tokens.addAll(getCharacterExpressionTree(functionLexerTree.lexerExpressions, isNot));
		} else if (expressionTree instanceof ATRLFFunctionCalledLexerTree functionCalledLexerTree) {
			tokens.addAll(getCharacterExpressionTree(compilationUnit.functions.get(functionCalledLexerTree.name.value()), isNot));
		}
		return tokens;
	}
}