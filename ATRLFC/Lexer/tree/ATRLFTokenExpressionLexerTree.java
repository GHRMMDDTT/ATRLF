package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

public final class ATRLFTokenExpressionLexerTree extends ATRLFExpressionLexerTree {
	public final ATRLFToken name;
	public final ATRLFExpressionLexerTree expressionLexerTree;

	public ATRLFTokenExpressionLexerTree(ATRLFToken name, ATRLFExpressionLexerTree expressionLexerTree) {
		this.name = name;
		this.expressionLexerTree = expressionLexerTree;
	}

	@Override
	public String onVisitor(boolean isNot) {
		this.compilationUnit.tokens.add(this.name);
		return "%s\nreturn new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.%s, this.column, this.line);".formatted(this.expressionLexerTree.onVisitor(isNot), this.name.value());
	}

	@Override
	public String toString() {
		return this.onVisitor(false);
	}
}
