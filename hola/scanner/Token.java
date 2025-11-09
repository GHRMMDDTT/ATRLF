package hola.scanner;

public class Token {
	private final String value;
	private final TokenSyntax type;
	private final int column;
	private final int line;

	public Token(String value, TokenSyntax type, int column, int line) {
		this.value = value;
		this.type = type;
		this.column = column;
		this.line = line;
	}

	public enum TokenSyntax {
		IdentifierToken,
		NumericLiteralToken,
		CompactNumericLiteralToken,
		CharacterLiteralToken,
		OperatorSyntaxToken,
		BadToken;
	}
}
