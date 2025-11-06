package ATRLFC.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.PlusSymbolArithmeticalOperatorToken;

public final class ATRLFGroupExpressionTree extends ATRLFExpressionTree {
	public final ATRLFExpressionTree expresion;

	public ATRLFGroupExpressionTree(ATRLFExpressionTree expresion) {
		this.expresion = expresion;
	}

	@Override
	public String onVisitor() {
		return expresion.onVisitor();
	}

	@Override
	public String toString() {
		return "[" + expresion +
				']';
	}
}
