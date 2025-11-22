package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.AllToken;
import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.NotToken;

public final class ATRLFAlternativesStatementLexerTree extends ATRLFStatementLexerTree {
	public final ArrayList<ATRLFExpressionLexerTree> expressionTrees;

	public ATRLFAlternativesStatementLexerTree(ArrayList<ATRLFExpressionLexerTree> expressionTrees) {
		this.expressionTrees = expressionTrees;
	}

	@Override
	public String onVisitor(boolean isNot) {
		StringBuilder sb = new StringBuilder();
		if (this.expressionTrees.size() == 1) {
			sb.append(this.expressionTrees.getFirst().onVisitor(isNot));
		} else {
			for (ATRLFExpressionLexerTree expressionTree : this.expressionTrees) {
				AtomicBoolean isNewLine = new AtomicBoolean(false);
				sb.append("if (").append(
						getCharacterExpressionTree(expressionTree).stream().map(new Function<ArrayList<ATRLFToken>, String>() {
							@Override
							public String apply(ArrayList<ATRLFToken> atrlfTokens) {
								if (atrlfTokens.size() == 2) {
									return "(this.peek() >= %s && this.peek() <= %s)".formatted(atrlfTokens.getFirst().value(), atrlfTokens.getLast().value());
								} else if (atrlfTokens.size() == 1) {
									if (atrlfTokens.getFirst().value().equals("'\\n'")) isNewLine.set(true);
									return "this.has(%s)".formatted(atrlfTokens.getFirst().value());
								}
								return "?";
							}
						}).collect(Collectors.joining(" || "))
				).append(") {\n").append(isNewLine.get() ? "this.line++;\nthis.column = this.position;\n" : "").append(expressionTree.onVisitor(isNot)).append("\n} else ");
			}
			sb.append("{\nthis.error();\n}");
		}
		return sb.toString();
	}
}