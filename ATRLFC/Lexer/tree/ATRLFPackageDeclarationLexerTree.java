package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;

public final class ATRLFPackageDeclarationLexerTree extends ATRLFLexerTree {
	public final ArrayList<ATRLFToken> expressionTrees;

	public ATRLFPackageDeclarationLexerTree(ArrayList<ATRLFToken> expressionTrees) {
		this.expressionTrees = expressionTrees;
	}

	@Override
	public String onVisitor(boolean isNot) {
		StringBuilder sb = new StringBuilder();
		for (ATRLFToken expressionTree : this.expressionTrees) {
			sb.append(expressionTree.value()).append('.');
		}
		sb.setLength(sb.length());
		return sb.toString();
	}
}
