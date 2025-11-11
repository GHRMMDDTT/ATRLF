package example.hola.scanner;

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

	public final String getValue() {
		return this.value;
	}

	public final TokenSyntax getType() {
		return this.type;
	}

	public final int getColumn() {
		return this.column;
	}

	public final int getLine() {
		return this.line;
	}

	public enum TokenSyntax {
		EqualLessSymbolOperatorToken,
		EqualSymbolOperatorToken,
		CurlyLeft,
		EqualGreaterSymbolOperatorToken,
		StartSymbolOperatorToken,
		FlatLineSymbolOperatorToken,
		SwitchKeywordToken,
		IdentifierToken,
		NumberOctalLiteralToken,
		NumberBinaryLiteralToken,
		CurlyRight,
		ElseKeywordToken,
		GreaterSymbolOperatorToken,
		NeutralLiteralToken,
		EqualEqualSymbolOperatorToken,
		NumberHexadecimalLiteralToken,
		CaseKeywordToken,
		MinusSymbolOperatorToken,
		NumericLiteralToken,
		IfKeywordToken,
		DefaultKeywordToken,
		StringLiteralToken,
		CharacterLiteralToken,
		SlashSymbolOperatorToken,
		PlusSymbolOperatorToken,
		LessSymbolOperatorToken,
		BadToken,
		EndOfInputFileToken
	}
}
