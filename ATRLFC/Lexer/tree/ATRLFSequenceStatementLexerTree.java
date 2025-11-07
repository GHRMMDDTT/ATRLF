package ATRLFC.Lexer.tree;

import java.util.ArrayList;

public final class ATRLFSequenceStatementLexerTree extends ATRLFStatementLexerTree {
	public final ArrayList<ATRLFExpressionLexerTree> expressionTrees;

	public ATRLFSequenceStatementLexerTree(ArrayList<ATRLFExpressionLexerTree> expressionTrees) {
		this.expressionTrees = expressionTrees;
	}

	@Override
	public String onVisitor() {
		StringBuilder sb = new StringBuilder();
		for (ATRLFExpressionLexerTree expressionTree : this.expressionTrees) {
			sb.append(expressionTree.onVisitor()).append('\n');
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
}
