package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

public final class ATRLFCharacterExpressionLexerTree extends ATRLFExpressionLexerTree {
	public final ATRLFToken character;

	public ATRLFCharacterExpressionLexerTree(ATRLFToken character) {
		this.character = character;
	}

	@Override
	public String onVisitor(boolean isNot) {
		return this.character.value();
	}

	@Override
	public String toString() {
		return this.onVisitor(false);
	}
}
