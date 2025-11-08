package main.scanner;

public class Token {
	private final String value;
	private final TokenSyntax type;

	public Token(String value, TokenSyntax type) {
		this.value = value;
		this.type = type;
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
