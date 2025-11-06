package ATRLFC.tree;

import java.util.ArrayList;

public final class ATRLFSequenceStatementTree extends ATRLFStatementTree {
	public final ArrayList<ATRLFExpressionTree> expressionTrees;

	public ATRLFSequenceStatementTree(ArrayList<ATRLFExpressionTree> expressionTrees) {
		this.expressionTrees = expressionTrees;
	}

	@Override
	public String onVisitor() {
		StringBuilder sb = new StringBuilder();
		for (ATRLFExpressionTree expressionTree : this.expressionTrees) {
			sb.append(expressionTree.onVisitor()).append('\n');
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
}
