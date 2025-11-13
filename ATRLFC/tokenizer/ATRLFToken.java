package ATRLFC.tokenizer;

import java.util.Objects;

public final class ATRLFToken {
	private final String value;
	private final ATRLFTokenType type;
	private final int line;
	private final int column;

	public ATRLFToken(String value, ATRLFTokenType type, int line, int column) {
		this.value = value;
		this.type = type;
		this.line = line;
		this.column = column;
	}

	@Override
	public String toString() {
		return "ATRLFToken{" +
				"value='" + value + '\'' +
				", type=" + type +
				", line=" + line +
				", column=" + column +
				'}';
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || getClass() != object.getClass()) return false;
		ATRLFToken token = (ATRLFToken) object;
		return value.equals(token.value);
	}

	public String value() { return value; }

	public ATRLFTokenType type() { return type; }

	public int line() { return line; }

	public int column() { return column; }

	@Override
	public int hashCode() {
		return Objects.hash(value, type, line, column);
	}


	public enum ATRLFTokenType {
		// Generic:
		IdentifierToken,

		// Keyword:
		PackageKeywordToken,
		ImportKeywordToken,
		FromKeywordToken,

		FunctionKeywordToken,

		SwitchKeywordToken,
		CaseKeywordToken,
		DefaultKeywordToken,

		LexerClassTypeToken,
		InterpreterClassTypeToken,

		IfKeywordControlToken,

		// Symbols:
		AtSymbolToken,
		LowLineSymbolToken,
		TildeSymbolToken,
		QuotationSymbolToken,
		ApostropheSymbolToken,

		EqualSymbolOperatorToken,
		AndSymbolOperatorToken,
		VerticalLineSymbolOperatorToken,
		NotSymbolOperatorToken,
		QuestionSymbolOperatorToken,
		LessThanSymbolOperatorToken,
		GreaterThanSymbolOperatorToken,

		PlusSymbolArithmeticalOperatorToken,
		MinusSymbolArithmeticalOperatorToken,
		StartSymbolArithmeticalOperatorToken,
		SlashSymbolArithmeticalOperatorToken,
		ModuleSymbolArithmeticalOperatorToken,
		ExponentSymbolArithmeticalOperatorToken,

		SemicolonSymbolDelimiterOperatorToken,
		ColonSymbolDelimiterOperatorToken,
		CommaSymbolDelimiterOperatorToken,
		DotSymbolDelimiterOperatorToken,

		ParenthesisLeftSymbolDelimiterSeparatorOperatorToken,
		ParenthesisRightSymbolDelimiterSeparatorOperatorToken,
		SquareLeftSymbolDelimiterSeparatorOperatorToken,
		SquareRightSymbolDelimiterSeparatorOperatorToken,
		CurlyLeftSymbolDelimiterSeparatorOperatorToken,
		CurlyRightSymbolDelimiterSeparatorOperatorToken,

		// Literals:
		StringLiteralToken,
		CharacterLiteralToken,
		IntegerLiteralToken,

		EndOfInputFile,
		NotFoundToken,
		NotToken,
		AllToken;
	}
}
