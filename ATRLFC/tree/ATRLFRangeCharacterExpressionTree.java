package ATRLFC.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ATRLFRangeCharacterExpressionTree extends ATRLFExpressionTree {
	public final ArrayList<ArrayList<ATRLFExpressionTree>> expression;

	public ATRLFRangeCharacterExpressionTree(ArrayList<ArrayList<ATRLFExpressionTree>> expression) {
		this.expression = expression;
	}

	@Override
	public String onVisitor() {
		return IntStream.range(0, expression.size())
				.mapToObj(i -> {
					ArrayList<ATRLFExpressionTree> subExpresion = expression.get(i);
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
