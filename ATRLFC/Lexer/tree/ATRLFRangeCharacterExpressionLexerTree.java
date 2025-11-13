package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

public final class ATRLFRangeCharacterExpressionLexerTree extends ATRLFExpressionLexerTree {
	public final ATRLFToken minimum;
	public final ATRLFToken maximum;

	public ATRLFRangeCharacterExpressionLexerTree(ATRLFToken minimum, ATRLFToken maximum) {
		this.minimum = minimum;
		this.maximum = maximum;
	}

	@Override
	public String onVisitor(boolean isNot) {
		return "this.consume();";
	}

	@Override
	public String toString() {
		return "[" + minimum +
				']';
	}
}
