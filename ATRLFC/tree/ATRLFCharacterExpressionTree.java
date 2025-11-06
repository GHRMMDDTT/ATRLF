package ATRLFC.tree;

import ATRLFC.tokenizer.ATRLFToken;

public final class ATRLFCharacterExpressionTree extends ATRLFExpressionTree {
	public final ATRLFToken character;

	public ATRLFCharacterExpressionTree(ATRLFToken character) {
		this.character = character;
	}

	@Override
	public String onVisitor() {
		return this.character.value();
	}

	@Override
	public String toString() {
		return this.onVisitor();
	}
}
