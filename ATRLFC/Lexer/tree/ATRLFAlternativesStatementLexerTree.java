package ATRLFC.Lexer.tree;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.AllToken;
import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.NotToken;

public final class ATRLFAlternativesStatementLexerTree extends ATRLFStatementLexerTree {
	public final ArrayList<ATRLFExpressionLexerTree> expressionTrees;

	public ATRLFAlternativesStatementLexerTree(ArrayList<ATRLFExpressionLexerTree> expressionTrees) {
		this.expressionTrees = expressionTrees;
	}

	@Override
	public String onVisitor() {
		StringBuilder sb = new StringBuilder();
		if (expressionTrees.size() == 1) {
			sb.append(expressionTrees.getFirst().onVisitor());
		} else {
			for (ATRLFExpressionLexerTree expressionTree : this.expressionTrees) {
				AtomicBoolean isNewLine = new AtomicBoolean(false);
				sb.append("if (").append(
						getCharacterExpressionTree(expressionTree, false).stream().map(subExpresion -> {
									String condition = "";

									if (subExpresion.size() == 2) {
										if (subExpresion.getFirst().type() == NotToken) condition = "!(";
										condition += String.format("(this.peek() >= %s && this.peek() <= %s)", subExpresion.getFirst().value(), subExpresion.getLast().value());
										if (subExpresion.getFirst().type() == NotToken) condition += ")";
									} else {
										if (subExpresion.getFirst().type() == AllToken) {
											condition = "true";
										} else {
											if (subExpresion.getFirst().type() == NotToken) condition = "!";
											condition += String.format("this.has(%s)", subExpresion.getFirst().value());
											isNewLine.set(subExpresion.getFirst().value().equals("'\\n'"));
										}
									}

									return condition;
								})
								.collect(Collectors.joining(" || "))
				).append(") {\n").append(isNewLine.get() ? "this.line++;\nthis.column = this.position;\n" : "").append(expressionTree.onVisitor()).append("\n} else ");
			}
			sb.append("{\nthis.error();\n}");
		}
		return sb.toString();
	}
}