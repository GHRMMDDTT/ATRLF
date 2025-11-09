package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

public final class ATRLFAnyExpressionLexerTree extends ATRLFExpressionLexerTree {
	public final ATRLFToken character;

	public ATRLFAnyExpressionLexerTree(ATRLFToken character) {
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
