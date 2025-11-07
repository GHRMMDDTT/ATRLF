package ATRLFC.Lexer.parser;

import ATRLFC.tokenizer.ATRLFScanner;
import ATRLFC.tokenizer.ATRLFToken;
import ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType;
import ATRLFC.Lexer.tree.*;
import ATRLFC.Lexer.tree.ATRLFFunctionLexerTree.ATRLFFunctionParametersLexerTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.*;
import static ATRLFC.Lexer.parser.ATRLFLexerParser.Flag.*;

public record ATRLFLexerParser(ATRLFScanner scanner) {
	private final static ATRLFToken NOT_FOUND_TOKEN = new ATRLFToken("\0", NotFoundToken, -1, -1);

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

	public ATRLFLexerTree onParser() {
		ArrayList<ATRLFFunctionLexerTree> expressionLexer = new ArrayList<>();

		ATRLFCompilationUnitLexerTree compilationUnitLexerTree = new ATRLFCompilationUnitLexerTree();

		while (validate(EnumSet.of(CURRENT)).type() != EndOfInputFile) {
			expressionLexer.add(this.onFunctions(compilationUnitLexerTree));
		}
		validate(EnumSet.of(CURRENT, NEXT, CONSUME), EndOfInputFile);

		compilationUnitLexerTree.setParameters(expressionLexer);

		return compilationUnitLexerTree;
	}

	private ATRLFFunctionLexerTree onFunctions(ATRLFCompilationUnitLexerTree compilationUnitLexerTree) {
		validate(EnumSet.of(CURRENT, NEXT, CONSUME), FunctionKeywordToken);

		validate(EnumSet.of(CURRENT, NEXT, CONSUME), LexerClassTypeToken);
		validate(EnumSet.of(CURRENT, NEXT, CONSUME), ColonSymbolDelimiterOperatorToken);
		ATRLFToken name = validate(EnumSet.of(CURRENT, NEXT, CONSUME), IdentifierToken);

		validate(EnumSet.of(CURRENT, NEXT, CONSUME), ParenthesisLeftSymbolDelimiterSeparatorOperatorToken);

		ArrayList<ATRLFFunctionParametersLexerTree> parametersLexerTrees = this.onFunctionParamereters();

		validate(EnumSet.of(CURRENT, NEXT, CONSUME), ParenthesisRightSymbolDelimiterSeparatorOperatorToken);

		validate(EnumSet.of(CURRENT, NEXT, CONSUME), ColonSymbolDelimiterOperatorToken);
		validate(EnumSet.of(CURRENT, NEXT, CONSUME), EqualSymbolOperatorToken);
		validate(EnumSet.of(CURRENT, NEXT, CONSUME), CurlyLeftSymbolDelimiterSeparatorOperatorToken);

		ATRLFExpressionLexerTree expressionLexerTree = this.onParserAlternatives(compilationUnitLexerTree);

		validate(EnumSet.of(CURRENT, NEXT, CONSUME), CurlyRightSymbolDelimiterSeparatorOperatorToken);

		ATRLFToken token = null;

		if (validate(EnumSet.of(CURRENT, NEXT, SEEK), EqualSymbolOperatorToken) != NOT_FOUND_TOKEN) {
			validate(EnumSet.of(CURRENT, NEXT, CONSUME), GreaterThanSymbolOperatorToken);
			token = validate(EnumSet.of(CURRENT, NEXT, CONSUME), IdentifierToken);
		}

		validate(EnumSet.of(CURRENT, NEXT, CONSUME), SemicolonSymbolDelimiterOperatorToken);

		return new ATRLFFunctionLexerTree(name, parametersLexerTrees, expressionLexerTree, token);
	}

		private ArrayList<ATRLFFunctionParametersLexerTree> onFunctionParamereters() {
			ArrayList<ATRLFFunctionParametersLexerTree> parametersLexerTrees = new ArrayList<>();
			ATRLFFunctionParametersLexerTree _tmp;

			while (validate(EnumSet.of(CURRENT, SEEK)).type() != ParenthesisRightSymbolDelimiterSeparatorOperatorToken) {
				ATRLFToken name, type;

				name  = validate(EnumSet.of(CURRENT, NEXT, CONSUME), IdentifierToken);
				
				validate(EnumSet.of(CURRENT, NEXT, CONSUME), ColonSymbolDelimiterOperatorToken);

				type  = validate(EnumSet.of(CURRENT, NEXT, CONSUME), IdentifierToken);

				_tmp = new ATRLFFunctionParametersLexerTree(name, type);

				if (validate(EnumSet.of(CURRENT, SEEK)).type() == CommaSymbolDelimiterOperatorToken) {
					validate(EnumSet.of(CURRENT, NEXT));
				}
				parametersLexerTrees.add(_tmp);
			}
			return parametersLexerTrees;
		}

	private ATRLFStatementLexerTree onParserAlternatives(ATRLFCompilationUnitLexerTree compilationUnitLexerTree) {
		ArrayList<ATRLFStatementLexerTree> expressionTrees = new ArrayList<>();
		expressionTrees.add(this.onParserSequence(compilationUnitLexerTree));

		while (validate(EnumSet.of(CURRENT)).type() == VerticalLineSymbolOperatorToken && validate(EnumSet.of(CURRENT)).type() != CurlyRightSymbolDelimiterSeparatorOperatorToken) {
			validate(EnumSet.of(CURRENT, NEXT));
			expressionTrees.add(this.onParserSequence(compilationUnitLexerTree));
		}

		ATRLFAlternativesStatementLexerTree alternatuives = new ATRLFAlternativesStatementLexerTree(expressionTrees);
		alternatuives.compilationUnit = compilationUnitLexerTree;
		return alternatuives;
	}

	private ATRLFStatementLexerTree onParserSequence(ATRLFCompilationUnitLexerTree compilationUnitLexerTree) {
		ArrayList<ATRLFExpressionLexerTree> expressionTrees = new ArrayList<>();
		ATRLFToken delimiter;

		while (
				(delimiter = validate(EnumSet.of(CURRENT, SEEK))).type() != EndOfInputFile &&
						delimiter.type() != ParenthesisRightSymbolDelimiterSeparatorOperatorToken &&
						delimiter.type() != VerticalLineSymbolOperatorToken &&
						delimiter.type() != CurlyRightSymbolDelimiterSeparatorOperatorToken
		) {
			expressionTrees.add(this.onParserUnary(compilationUnitLexerTree));
		}

		return new ATRLFSequenceStatementLexerTree(expressionTrees);
	}

	private ATRLFExpressionLexerTree onParserUnary(ATRLFCompilationUnitLexerTree compilationUnitLexerTree) {
		ATRLFExpressionLexerTree right = this.onParserGroup(compilationUnitLexerTree);
		ATRLFToken expresion = validate(EnumSet.of(CURRENT, NEXT, SEEK), PlusSymbolArithmeticalOperatorToken, QuestionSymbolOperatorToken, CurlyLeftSymbolDelimiterSeparatorOperatorToken);

		if (expresion != NOT_FOUND_TOKEN) {
			ATRLFToken subExpresion;

			if (expresion.type() == QuestionSymbolOperatorToken && (subExpresion = validate(EnumSet.of(CURRENT, NEXT, SEEK), PlusSymbolArithmeticalOperatorToken)) != NOT_FOUND_TOKEN) {
				right = new ATRLFUnaryExpressionLexerTree(new ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree(subExpresion), right);
				right.compilationUnit = compilationUnitLexerTree;
			}
		}

		if (expresion.type() == CurlyLeftSymbolDelimiterSeparatorOperatorToken) {
			right = new ATRLFUnaryExpressionLexerTree(new ATRLFUnaryExpressionLexerTree.ATRLFUnaryMultipleOperatorExpresionTree(this.onParserRangeIndex()), right);

			validate(EnumSet.of(CURRENT, CONSUME, NEXT), CurlyRightSymbolDelimiterSeparatorOperatorToken);
		} else {
			right = new ATRLFUnaryExpressionLexerTree(new ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree(expresion), right);
		}
		right.compilationUnit = compilationUnitLexerTree;

		return right;
	}

	private ArrayList<ArrayList<ATRLFToken>> onParserRangeIndex() {
		ArrayList<ArrayList<ATRLFToken>> expression = new ArrayList<>();

		while (validate(EnumSet.of(CURRENT, SEEK)).type() != CurlyRightSymbolDelimiterSeparatorOperatorToken) {
			ArrayList<ATRLFToken> subExpresion = new ArrayList<>();
			subExpresion.add(validate(EnumSet.of(CURRENT, NEXT, CONSUME), IntegerLiteralToken));

			if (validate(EnumSet.of(CURRENT, SEEK)).type() == MinusSymbolArithmeticalOperatorToken) {
				validate(EnumSet.of(CURRENT, NEXT));
				subExpresion.add(validate(EnumSet.of(CURRENT, NEXT, CONSUME), IntegerLiteralToken));
			}

			expression.add(subExpresion);

			if (validate(EnumSet.of(CURRENT, SEEK)).type() == CommaSymbolDelimiterOperatorToken) {
				validate(EnumSet.of(CURRENT, NEXT));
			}
		}

		return expression;
	}

	private ATRLFExpressionLexerTree onParserGroup(ATRLFCompilationUnitLexerTree compilationUnitLexerTree) {
		ATRLFToken expresion = validate(EnumSet.of(CURRENT, SEEK), ParenthesisLeftSymbolDelimiterSeparatorOperatorToken, SquareLeftSymbolDelimiterSeparatorOperatorToken, CharacterLiteralToken, IdentifierToken);

		if (expresion.type() == ParenthesisLeftSymbolDelimiterSeparatorOperatorToken) {
			validate(EnumSet.of(CURRENT, NEXT));

			ATRLFExpressionLexerTree expressionTree = this.onParserAlternatives(compilationUnitLexerTree);

			validate(EnumSet.of(CURRENT, CONSUME, NEXT), ParenthesisRightSymbolDelimiterSeparatorOperatorToken);
			return new ATRLFGroupExpressionLexerTree(expressionTree);
		} else if (expresion.type() == SquareLeftSymbolDelimiterSeparatorOperatorToken) {
			validate(EnumSet.of(CURRENT, NEXT));

			ArrayList<ArrayList<ATRLFExpressionLexerTree>> expressionTree = this.onParserRangeCharacter(compilationUnitLexerTree);

			validate(EnumSet.of(CURRENT, CONSUME, NEXT), SquareRightSymbolDelimiterSeparatorOperatorToken);

			return new ATRLFRangeCharacterExpressionLexerTree(expressionTree);
		} else if (expresion.type() == IdentifierToken) {
			validate(EnumSet.of(CURRENT, NEXT));
			validate(EnumSet.of(CURRENT, NEXT, CONSUME), ParenthesisLeftSymbolDelimiterSeparatorOperatorToken);
			validate(EnumSet.of(CURRENT, NEXT, CONSUME), ParenthesisRightSymbolDelimiterSeparatorOperatorToken);
			return new ATRLFFunctionCalledLexerTree(expresion, null);
		}

		return this.onParserCharacter(compilationUnitLexerTree);
	}

	private ArrayList<ArrayList<ATRLFExpressionLexerTree>> onParserRangeCharacter(ATRLFCompilationUnitLexerTree compilationUnitLexerTree) {
		ArrayList<ArrayList<ATRLFExpressionLexerTree>> expression = new ArrayList<>();

		while (validate(EnumSet.of(CURRENT, SEEK)).type() != SquareRightSymbolDelimiterSeparatorOperatorToken) {
			ArrayList<ATRLFExpressionLexerTree> subExpresion = new ArrayList<>();
			subExpresion.add(this.onParserCharacter(compilationUnitLexerTree));

			if (validate(EnumSet.of(CURRENT, SEEK)).type() == MinusSymbolArithmeticalOperatorToken) {
				validate(EnumSet.of(CURRENT, NEXT));
				subExpresion.add(this.onParserCharacter(compilationUnitLexerTree));
			}

			expression.add(subExpresion);

			if (validate(EnumSet.of(CURRENT, SEEK)).type() == CommaSymbolDelimiterOperatorToken) {
				validate(EnumSet.of(CURRENT, NEXT));
			}
		}

		return expression;
	}

	private ATRLFExpressionLexerTree onParserCharacter(ATRLFCompilationUnitLexerTree compilationUnitLexerTree) {
		ATRLFToken character = validate(EnumSet.of(CURRENT, NEXT, CONSUME), CharacterLiteralToken);
		return new ATRLFCharacterExpressionLexerTree(character);
	}


	public ATRLFToken validate(Set<Flag> flags, ATRLFTokenType... types) {
		boolean isCurrent = flags.contains(CURRENT);
		boolean isNext = flags.contains(NEXT);
		boolean isConsume = flags.contains(CONSUME);
		boolean isSeek = flags.contains(SEEK);
		boolean isNot = flags.contains(NOT);

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
} else {
this.error(target);
}
}
""";

	public static final String errorCode = """
private final void error() {
throw new RuntimeException("No match in: " + this.peek());
}
""";
}
