package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ATRLFLexerTree {
	public ATRLFCompilationUnitLexerTree compilationUnit;

	public abstract String onVisitor(boolean var1);

	protected String getCharacterExpressionTreeToString(ATRLFExpressionLexerTree expressionTree, boolean isNot) {
		String collect = this.getCharacterExpressionTree(expressionTree).stream().map(atrlfTokens -> {
			if (atrlfTokens.size() == 2) {
				return "(this.peek() >= %s && this.peek() <= %s)".formatted(atrlfTokens.getFirst().value(), atrlfTokens.getLast().value());
			} else {
				return atrlfTokens.size() == 1 ? "this.has(%s)".formatted(atrlfTokens.getFirst().value()) : "?";
			}
		}).collect(Collectors.joining(" || "));
		return isNot ? "!(%s)".formatted(collect) : collect;
	}

	protected ArrayList<ArrayList<ATRLFToken>> getCharacterExpressionTree(ATRLFExpressionLexerTree expressionTree) {
		ArrayList<ArrayList<ATRLFToken>> tokens = new ArrayList<>();
		if (expressionTree instanceof ATRLFUnaryExpressionLexerTree unaryExpressionTree) {
			tokens.addAll(this.getCharacterExpressionTree(unaryExpressionTree.expresion));
		} else if (expressionTree instanceof ATRLFGroupExpressionLexerTree groupExpressionTree3) {
			tokens.addAll(this.getCharacterExpressionTree(groupExpressionTree3.expresion));
		} else if (expressionTree instanceof ATRLFCharacterExpressionLexerTree characterExpressionTree) {
			tokens.add(new ArrayList<>(List.of(characterExpressionTree.character)));
		} else if (expressionTree instanceof ATRLFAnyExpressionLexerTree anyExpressionLexerTree) {
			tokens.add(new ArrayList<>(List.of(new ATRLFToken("true", ATRLFToken.ATRLFTokenType.AllToken, anyExpressionLexerTree.character.line(), anyExpressionLexerTree.character.column()))));
		} else if (expressionTree instanceof ATRLFTokenExpressionLexerTree tokenExpressionLexerTree) {
			tokens.addAll(this.getCharacterExpressionTree(tokenExpressionLexerTree.expressionLexerTree));
		} else if (expressionTree instanceof ATRLFAlternativesStatementLexerTree alternativesStatementTree) {
			for(ATRLFExpressionLexerTree expressionTree1 : alternativesStatementTree.expressionTrees) {
				tokens.addAll(this.getCharacterExpressionTree(expressionTree1));
			}
		} else if (expressionTree instanceof ATRLFSequenceStatementLexerTree sequenceStatementTree) {
			tokens.addAll(this.getCharacterExpressionTree(sequenceStatementTree.expressionTrees.getFirst()));
		} else if (expressionTree instanceof ATRLFRangeCharacterExpressionLexerTree characterExpressionTree) {
			tokens.add(new ArrayList<>(Arrays.asList(characterExpressionTree.minimum, characterExpressionTree.maximum)));
		} else if (expressionTree instanceof ATRLFFunctionLexerTree functionLexerTree) {
			tokens.addAll(this.getCharacterExpressionTree(functionLexerTree.lexerExpressions));
		} else if (expressionTree instanceof ATRLFFunctionCalledLexerTree functionCalledLexerTree) {
			tokens.addAll(this.getCharacterExpressionTree(this.compilationUnit.functions.get(functionCalledLexerTree.name.value())));
		}

		return tokens;
	}
}
