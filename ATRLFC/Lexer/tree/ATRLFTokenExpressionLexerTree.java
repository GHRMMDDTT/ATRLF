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
	public String onVisitor() {
		this.compilationUnit.tokens.add(this.name);
		return this.expressionLexerTree.onVisitor() + "\nreturn new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax." + this.name.value() + ", this.column, this.line);";
	}

	@Override
	public String toString() {
		return this.onVisitor();
	}
}
