package ATRLFC.tokenizer;

import java.util.Objects;

public record ATRLFToken(String value, ATRLFTokenType type, int line, int column) {

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

		PublicKeywordToken,
		PrivateKeywordToken,
		ProtectedKeywordToken,

		FunctionKeywordToken,

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
