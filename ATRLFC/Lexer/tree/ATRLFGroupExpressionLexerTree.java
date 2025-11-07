package ATRLFC.Lexer.tree;

public final class ATRLFGroupExpressionLexerTree extends ATRLFExpressionLexerTree {
	public final ATRLFExpressionLexerTree expresion;

	public ATRLFGroupExpressionLexerTree(ATRLFExpressionLexerTree expresion) {
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
