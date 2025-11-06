package ATRLFC.parser;

import ATRLFC.tokenizer.ATRLFScanner;
import ATRLFC.tokenizer.ATRLFToken;
import ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType;
import ATRLFC.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.*;

public class ATRLFParser {
	private final static ATRLFToken NOT_FOUND_TOKEN = new ATRLFToken("\0", ATRLFTokenType.NotFoundToken, -1, -1);

	private final ATRLFScanner scanner;

	public enum Flag {
		CURRENT,
		NEXT,
		CONSUME,
		SEEK,
		NOT
	}

	public enum TypeError {
		PARSER("Parser"),
		CONSTRUCTION("Constructor");

		private final String text;

		TypeError(String text) { this.text = text; }
	}

	public ATRLFParser(ATRLFScanner scanner) {
		this.scanner = scanner;
	}

	public ATRLFTree onParser() {
		return this.onParserAlternatives();
	}

	private ATRLFStatementTree onParserAlternatives() {
		ArrayList<ATRLFStatementTree> expressionTrees = new ArrayList<>();
		expressionTrees.add(this.onParserSequence());

		while (validate(EnumSet.of(Flag.CURRENT)).type() == VerticalLineSymbolOperatorToken)  {
			validate(EnumSet.of(Flag.CURRENT, Flag.NEXT));
			expressionTrees.add(this.onParserSequence());
		}

		return new ATRLFAlternativesStatementTree(expressionTrees);
	}

	private ATRLFStatementTree onParserSequence() {
		ArrayList<ATRLFExpressionTree> expressionTrees = new ArrayList<>();
		ATRLFToken delimiter;

		while (
				(delimiter = validate(EnumSet.of(Flag.CURRENT, Flag.SEEK))).type() != EndOfInputFile &&
						delimiter.type() != ParenthesisRightSymbolDelimiterSeparatorOperatorToken &&
						delimiter.type() != VerticalLineSymbolOperatorToken
		) {
			expressionTrees.add(this.onParserUnary());
		}

		return new ATRLFSequenceStatementTree(expressionTrees);
	}

	private ATRLFExpressionTree onParserUnary() {
		ATRLFExpressionTree right = this.onParserGroup();
		ATRLFToken expresion = validate(EnumSet.of(Flag.CURRENT, Flag.NEXT, Flag.SEEK), PlusSymbolArithmeticalOperatorToken, QuestionSymbolOperatorToken);

		if (expresion != NOT_FOUND_TOKEN) {
			ATRLFToken subExpresion;

			if (expresion.type() == QuestionSymbolOperatorToken && (subExpresion = validate(EnumSet.of(Flag.CURRENT, Flag.NEXT, Flag.SEEK), PlusSymbolArithmeticalOperatorToken)) != NOT_FOUND_TOKEN) {
				right = new ATRLFUnaryExpressionTree(subExpresion, right);
			}
		}

		right = new ATRLFUnaryExpressionTree(expresion, right);

		return right;
	}

	private ATRLFExpressionTree onParserGroup() {
		ATRLFToken expresion = validate(EnumSet.of(Flag.CURRENT, Flag.SEEK), ParenthesisLeftSymbolDelimiterSeparatorOperatorToken, SquareLeftSymbolDelimiterSeparatorOperatorToken, CharacterLiteralToken);

		if (expresion.type() == ParenthesisLeftSymbolDelimiterSeparatorOperatorToken) {
			validate(EnumSet.of(Flag.CURRENT, Flag.NEXT));

			ATRLFExpressionTree expressionTree = this.onParserAlternatives();

			validate(EnumSet.of(Flag.CURRENT, Flag.CONSUME, Flag.NEXT), ParenthesisRightSymbolDelimiterSeparatorOperatorToken);
			return new ATRLFGroupExpressionTree(expressionTree);
		} else if (expresion.type() == SquareLeftSymbolDelimiterSeparatorOperatorToken) {
			validate(EnumSet.of(Flag.CURRENT, Flag.NEXT));

			ArrayList<ArrayList<ATRLFExpressionTree>> expressionTree = this.onParserRangeCharacter();

			validate(EnumSet.of(Flag.CURRENT, Flag.CONSUME, Flag.NEXT), SquareRightSymbolDelimiterSeparatorOperatorToken);

			return new ATRLFRangeCharacterExpressionTree(expressionTree);
		}

		return this.onParserCharacter();
	}

	private ArrayList<ArrayList<ATRLFExpressionTree>> onParserRangeCharacter() {
		ArrayList<ArrayList<ATRLFExpressionTree>> expression = new ArrayList<>();

		while (validate(EnumSet.of(Flag.CURRENT, Flag.SEEK)).type() != SquareRightSymbolDelimiterSeparatorOperatorToken) {
			ArrayList<ATRLFExpressionTree> subExpresion = new ArrayList<>();
			subExpresion.add(this.onParserCharacter());

			if (validate(EnumSet.of(Flag.CURRENT, Flag.SEEK)).type() == MinusSymbolArithmeticalOperatorToken) {
				validate(EnumSet.of(Flag.CURRENT, Flag.NEXT));
				subExpresion.add(this.onParserCharacter());
			}

			expression.add(subExpresion);

			if (validate(EnumSet.of(Flag.CURRENT, Flag.SEEK)).type() == CommaSymbolDelimiterOperatorToken) {
				validate(EnumSet.of(Flag.CURRENT, Flag.NEXT));
			}
		}

		return expression;
	}

	private ATRLFExpressionTree onParserCharacter() {
		ATRLFToken character = validate(EnumSet.of(Flag.CURRENT, Flag.NEXT, Flag.CONSUME), CharacterLiteralToken);
		return new ATRLFCharacterExpressionTree(character);
	}


	public ATRLFToken validate(Set<Flag> flags, ATRLFTokenType... types) {
		boolean isCurrent = flags.contains(Flag.CURRENT);
		boolean isNext = flags.contains(Flag.NEXT);
		boolean isConsume = flags.contains(Flag.CONSUME);
		boolean isSeek = flags.contains(Flag.SEEK);
		boolean isNot = flags.contains(Flag.NOT);

		if (!isCurrent) {
			parserError(TypeError.CONSTRUCTION, "Construction Error: Missing 'CURRENT' flag to get the Token.", null);
		}

		ATRLFToken t = requireToken();

		if (types.length == 0) {
			if (isNext) scanner.getNextToken();
			return t;
		}

		if (isNot) {
			for (ATRLFTokenType type : types) {
				if (t.type() == type) {
					parserError(TypeError.PARSER, "Symbol Match With Not Match (Syntax Error): The type '" + t.value() +
							"' exactly matches forbidden type(s): " + buildError(types), t);
				}
			}
			if (isNext) scanner.getNextToken();
			return t;
		}

		if (isSeek) {
			for (ATRLFTokenType type : types) {
				if (t.type() == type) {
					if (isNext) scanner.getNextToken();
					return t;
				}
			}
			return NOT_FOUND_TOKEN;
		}

		if (isConsume) {
			for (ATRLFTokenType type : types) {
				if (t.type() == type) {
					if (isNext) scanner.getNextToken();
					return t;
				}
			}
			parserError(TypeError.PARSER, "Symbol Mismatch (Syntax Error): Expected " + buildError(types) +
					", but found '" + t.value() + "'", t);
		}

		parserError(TypeError.CONSTRUCTION, "Construction Error: Missing valid flag configuration for 'validate'.", t);
		return null;
	}

	private ATRLFToken requireToken() {
		ATRLFToken t = scanner.getToken();
		while (t == null) {
			scanner.getNextToken();
			t = scanner.getToken();
		}
		return t;
	}

	private void parserError(TypeError error, String message, ATRLFToken t) {
		if (t != null) {
			System.err.printf("[ATRLF %s] %s at %d:%d.%n", error.text, message, t.line(), t.column());
		} else {
			System.err.printf("[ATRLF %s] %s%n", error.text, message);
		}
		System.exit(-1);
	}

	private String buildError(ATRLFTokenType... types) {
		if (types == null || types.length == 0) return "(no expected types)";
		return Arrays.stream(types)
				.map(Enum::name)
				.collect(Collectors.joining(" | "));
	}

	public static final String targetArrayCharacter = "private final char[] target;";
	public static final String positionInt = "private int position;";

	public static final String peekCode = """
private char peek() {
    if (this.position >= this.target.length) return '\\0';
    return this.target[this.position];
}
        """;

	public static final String has$argument_char$Code = """
private final boolean has(char target) {
    return this.peek() == target || this.peek() != '\\0';
}
        """;

	public static final String consumeCode = """
private final void consume() {
    this.position++;
}
        """;

	public static final String accept$argument_char$Code = """
private final void accept(char target) {
    if (this.has(target)) {
        this.consume();
        return;
    }
    this.error(target);
}
        """;

	public static final String errorCode = """
private final char error() {
    if (this.peek() == '\\0') return '\\0';
    throw new RuntimeException("No match in: " + this.peek());
}
        """;
}
