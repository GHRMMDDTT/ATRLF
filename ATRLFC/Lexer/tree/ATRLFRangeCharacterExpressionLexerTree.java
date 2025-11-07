package ATRLFC.Lexer.tree;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ATRLFRangeCharacterExpressionLexerTree extends ATRLFExpressionLexerTree {
	public final ArrayList<ArrayList<ATRLFExpressionLexerTree>> expression;

	public ATRLFRangeCharacterExpressionLexerTree(ArrayList<ArrayList<ATRLFExpressionLexerTree>> expression) {
		this.expression = expression;
	}

	@Override
	public String onVisitor() {
		return IntStream.range(0, expression.size())
				.mapToObj(i -> {
					ArrayList<ATRLFExpressionLexerTree> subExpresion = expression.get(i);
					String condition;
					String accept;

					if (subExpresion.size() == 2) {
						condition = String.format("(this.peek() >= %s && this.peek() <= %s)", subExpresion.getFirst().toString(), subExpresion.getLast().toString());
					} else {
						condition = String.format("this.has(%s)", subExpresion.getFirst().toString());
					}
					accept = "this.consume();";

					if (i == 0) {
						return "if (" + condition + ") {\n" + accept + "\n}";
					} else {
						return "else if (" + condition + ") {\n" + accept + "\n}";
					}
				})
				.collect(Collectors.joining(" "))
				+ " else {\nthis.error();\n}";
	}

	@Override
	public String toString() {
		return "[" + expression +
				']';
	}
}
