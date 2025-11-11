package ATRLFC.Lexer.parser;

import ATRLFC.tokenizer.ATRLFScanner;
import ATRLFC.tokenizer.ATRLFToken;
import ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType;
import ATRLFC.Lexer.tree.*;
import ATRLFC.Lexer.tree.ATRLFFunctionLexerTree.ATRLFFunctionParametersLexerTree;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.*;
import static ATRLFC.Lexer.parser.ATRLFLexerParser.Flag.*;

@SuppressWarnings({ "all" })
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

		ATRLFPackageDeclarationLexerTree packageDeclarationLexerTree = null;
		ArrayList<ATRLFToken> identifier = new ArrayList<>();

		if (validate(EnumSet.of(CURRENT, SEEK, NEXT), PackageKeywordToken) != NOT_FOUND_TOKEN) {
			ATRLFToken subDir = validate(EnumSet.of(CURRENT, CONSUME, NEXT), IdentifierToken);
			identifier.add(subDir);

			while (validate(EnumSet.of(CURRENT, SEEK), DotSymbolDelimiterOperatorToken).type() != NotFoundToken) {
				validate(EnumSet.of(CURRENT, CONSUME, NEXT), DotSymbolDelimiterOperatorToken);
				subDir = validate(EnumSet.of(CURRENT, CONSUME, NEXT), IdentifierToken);
				identifier.add(subDir);
			}
			validate(EnumSet.of(CURRENT, CONSUME, NEXT), SemicolonSymbolDelimiterOperatorToken);
			packageDeclarationLexerTree = new ATRLFPackageDeclarationLexerTree(identifier);
		}


		ATRLFCompilationUnitLexerTree compilationUnitLexerTree = new ATRLFCompilationUnitLexerTree(packageDeclarationLexerTree);

		while (validate(EnumSet.of(CURRENT, SEEK)).type() != EndOfInputFile) {
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

		ArrayList<ATRLFFunctionParametersLexerTree> parametersLexerTrees = this.onFunctionParameters();

		validate(EnumSet.of(CURRENT, NEXT, CONSUME), ParenthesisRightSymbolDelimiterSeparatorOperatorToken);

		validate(EnumSet.of(CURRENT, NEXT, CONSUME), ColonSymbolDelimiterOperatorToken);
		validate(EnumSet.of(CURRENT, NEXT, CONSUME), EqualSymbolOperatorToken);
		validate(EnumSet.of(CURRENT, NEXT, CONSUME), CurlyLeftSymbolDelimiterSeparatorOperatorToken);

		ATRLFExpressionLexerTree expressionLexerTree = this.onParserAlternatives(compilationUnitLexerTree);

		validate(EnumSet.of(CURRENT, NEXT, CONSUME), CurlyRightSymbolDelimiterSeparatorOperatorToken);

		ATRLFFunctionLexerTree.ATRLFFunctionReturn returener = null;

		if (validate(EnumSet.of(CURRENT, NEXT, SEEK), EqualSymbolOperatorToken) != NOT_FOUND_TOKEN) {
			validate(EnumSet.of(CURRENT, NEXT, CONSUME), GreaterThanSymbolOperatorToken);
			if (validate(EnumSet.of(CURRENT, SEEK), IdentifierToken) != NOT_FOUND_TOKEN) {
				returener = new ATRLFFunctionLexerTree.ATRLFFunctionSingleReturn(validate(EnumSet.of(CURRENT, NEXT), IdentifierToken));
			} else if (validate(EnumSet.of(CURRENT, NEXT, SEEK), SwitchKeywordToken) != NOT_FOUND_TOKEN) {
				validate(EnumSet.of(CURRENT, NEXT, CONSUME), ParenthesisLeftSymbolDelimiterSeparatorOperatorToken);
				validate(EnumSet.of(CURRENT, NEXT, CONSUME), IdentifierToken);
				validate(EnumSet.of(CURRENT, NEXT, CONSUME), ParenthesisRightSymbolDelimiterSeparatorOperatorToken);

				validate(EnumSet.of(CURRENT, NEXT, CONSUME), CurlyLeftSymbolDelimiterSeparatorOperatorToken);

				ArrayList<ATRLFFunctionLexerTree.ATRLFFunctionSwitchCaseReturn.ATRLFFunctionCaseReturn> caseReturns = new ArrayList<>();

				while (validate(EnumSet.of(CURRENT, SEEK)).type() == CaseKeywordToken || (validate(EnumSet.of(CURRENT, SEEK)).type() != DefaultKeywordToken)) {
					ATRLFFunctionLexerTree.ATRLFFunctionSwitchCaseReturn.ATRLFFunctionCaseReturn caseReturn;
					validate(EnumSet.of(CURRENT, NEXT, CONSUME), CaseKeywordToken);
					ATRLFToken value = validate(EnumSet.of(CURRENT, NEXT, CONSUME), StringLiteralToken);
					validate(EnumSet.of(CURRENT, NEXT, CONSUME), MinusSymbolArithmeticalOperatorToken);
					validate(EnumSet.of(CURRENT, NEXT, CONSUME), GreaterThanSymbolOperatorToken);
					ATRLFToken returnType = validate(EnumSet.of(CURRENT, NEXT, CONSUME), IdentifierToken);
					validate(EnumSet.of(CURRENT, NEXT, CONSUME), SemicolonSymbolDelimiterOperatorToken);
					caseReturn = new ATRLFFunctionLexerTree.ATRLFFunctionSwitchCaseReturn.ATRLFFunctionCaseReturn(value, returnType);
					caseReturns.add(caseReturn);
				}
				ATRLFFunctionLexerTree.ATRLFFunctionSingleReturn defaultReturn;
				validate(EnumSet.of(CURRENT, NEXT, CONSUME), DefaultKeywordToken);
				validate(EnumSet.of(CURRENT, NEXT, CONSUME), MinusSymbolArithmeticalOperatorToken);
				validate(EnumSet.of(CURRENT, NEXT, CONSUME), GreaterThanSymbolOperatorToken);
				ATRLFToken returnType = validate(EnumSet.of(CURRENT, NEXT, CONSUME), IdentifierToken);
				validate(EnumSet.of(CURRENT, NEXT, CONSUME), SemicolonSymbolDelimiterOperatorToken);
				defaultReturn = new ATRLFFunctionLexerTree.ATRLFFunctionSingleReturn(returnType);
				returener = new ATRLFFunctionLexerTree.ATRLFFunctionSwitchCaseReturn(caseReturns, defaultReturn);

				validate(EnumSet.of(CURRENT, NEXT, CONSUME), CurlyRightSymbolDelimiterSeparatorOperatorToken);
			} else {
				System.err.println("IDK, LOL");
				System.exit(-1);
			}
		}

		validate(EnumSet.of(CURRENT, NEXT, CONSUME), SemicolonSymbolDelimiterOperatorToken);

		ATRLFFunctionLexerTree functionLexerTree = new ATRLFFunctionLexerTree(name, parametersLexerTrees, expressionLexerTree, returener);
		functionLexerTree.compilationUnit = compilationUnitLexerTree;
		return functionLexerTree;
	}

		private ArrayList<ATRLFFunctionParametersLexerTree> onFunctionParameters() {
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

	private ATRLFExpressionLexerTree onParserAlternatives(ATRLFCompilationUnitLexerTree compilationUnitLexerTree) {
		ArrayList<ATRLFExpressionLexerTree> expressionTrees = new ArrayList<>();
		expressionTrees.add(this.onParserSequence(compilationUnitLexerTree));

		while (validate(EnumSet.of(CURRENT, SEEK)).type() == VerticalLineSymbolOperatorToken && validate(EnumSet.of(CURRENT, SEEK)).type() != CurlyRightSymbolDelimiterSeparatorOperatorToken && validate(EnumSet.of(CURRENT, SEEK)).type() != GreaterThanSymbolOperatorToken) {
			validate(EnumSet.of(CURRENT, NEXT));
			expressionTrees.add(this.onParserSequence(compilationUnitLexerTree));
		}

		if (expressionTrees.size() == 1) {
			return expressionTrees.getFirst();
		}

		ATRLFAlternativesStatementLexerTree alternatuives = new ATRLFAlternativesStatementLexerTree(expressionTrees);
		alternatuives.compilationUnit = compilationUnitLexerTree;
		return alternatuives;
	}

	private ATRLFExpressionLexerTree onParserSequence(ATRLFCompilationUnitLexerTree compilationUnitLexerTree) {
		ArrayList<ATRLFExpressionLexerTree> expressionTrees = new ArrayList<>();
		ATRLFToken delimiter;

		while (
				(delimiter = validate(EnumSet.of(CURRENT, SEEK))).type() != EndOfInputFile &&
						delimiter.type() != ParenthesisRightSymbolDelimiterSeparatorOperatorToken &&
						delimiter.type() != VerticalLineSymbolOperatorToken &&
						delimiter.type() != CurlyRightSymbolDelimiterSeparatorOperatorToken &&
						delimiter.type() != GreaterThanSymbolOperatorToken
		) {
			expressionTrees.add(this.onParserUnary(compilationUnitLexerTree));
		}

		if (expressionTrees.size() == 1) {
			return expressionTrees.getFirst();
		}

		return new ATRLFSequenceStatementLexerTree(expressionTrees);
	}

	private ATRLFExpressionLexerTree onParserUnary(ATRLFCompilationUnitLexerTree compilationUnitLexerTree) {
		ATRLFExpressionLexerTree right = this.onParserGroup(compilationUnitLexerTree);
		ATRLFToken expresion = validate(EnumSet.of(CURRENT, NEXT, SEEK), PlusSymbolArithmeticalOperatorToken, QuestionSymbolOperatorToken, CurlyLeftSymbolDelimiterSeparatorOperatorToken, NotSymbolOperatorToken);

		if (expresion.type() == QuestionSymbolOperatorToken) {
			ATRLFToken subExpresion;

			if ((subExpresion = validate(EnumSet.of(CURRENT, NEXT, SEEK), PlusSymbolArithmeticalOperatorToken)) != NOT_FOUND_TOKEN) {
				right = new ATRLFUnaryExpressionLexerTree(new ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree(subExpresion), right);
				right.compilationUnit = compilationUnitLexerTree;
			}
			right = new ATRLFUnaryExpressionLexerTree(new ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree(expresion), right);
		} else if (expresion.type() == NotSymbolOperatorToken) {
			ATRLFToken subExpresion = validate(EnumSet.of(CURRENT, NEXT, SEEK), PlusSymbolArithmeticalOperatorToken, QuestionSymbolOperatorToken, CurlyLeftSymbolDelimiterSeparatorOperatorToken);

			if (subExpresion.type() == QuestionSymbolOperatorToken) {
				ATRLFToken subSubExpresion;

				if ((subSubExpresion = validate(EnumSet.of(CURRENT, SEEK))).type() == PlusSymbolArithmeticalOperatorToken) {
					validate(EnumSet.of(CURRENT, NEXT));

					right = new ATRLFUnaryExpressionLexerTree(new ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree(subSubExpresion), right);
					right.compilationUnit = compilationUnitLexerTree;
				}

				right = new ATRLFUnaryExpressionLexerTree(new ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree(subExpresion), right);
			} else if (subExpresion.type() != NotFoundToken) {
				right = new ATRLFUnaryExpressionLexerTree(new ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree(subExpresion), right);
			}
			right = new ATRLFUnaryExpressionLexerTree(new ATRLFUnaryExpressionLexerTree.ATRLFUnarySingleOperatorExpresionTree(expresion), right);
		} else if (expresion.type() == CurlyLeftSymbolDelimiterSeparatorOperatorToken) {
			right = new ATRLFUnaryExpressionLexerTree(new ATRLFUnaryExpressionLexerTree.ATRLFUnaryMultipleOperatorExpresionTree(this.onParserRangeIndex()), right);

			validate(EnumSet.of(CURRENT, CONSUME, NEXT), CurlyRightSymbolDelimiterSeparatorOperatorToken);
		}  else {
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
		ATRLFToken expresion = validate(EnumSet.of(CURRENT, SEEK), ParenthesisLeftSymbolDelimiterSeparatorOperatorToken, SquareLeftSymbolDelimiterSeparatorOperatorToken, CharacterLiteralToken, IdentifierToken, StartSymbolArithmeticalOperatorToken, LessThanSymbolOperatorToken);

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
			ATRLFFunctionCalledLexerTree calledLexerTree = new ATRLFFunctionCalledLexerTree(expresion, null);
			calledLexerTree.compilationUnit = compilationUnitLexerTree;
			return calledLexerTree;
		} else if (expresion.type() == StartSymbolArithmeticalOperatorToken) {
			validate(EnumSet.of(CURRENT, NEXT));
			return new ATRLFAnyExpressionLexerTree(expresion);
		} else if (expresion.type() == LessThanSymbolOperatorToken) {
			validate(EnumSet.of(CURRENT, NEXT));
			ATRLFToken name = validate(EnumSet.of(CURRENT, NEXT, CONSUME), IdentifierToken);
			validate(EnumSet.of(CURRENT, NEXT, CONSUME), ColonSymbolDelimiterOperatorToken);
			ATRLFExpressionLexerTree expressionLexerTree = this.onParserAlternatives(compilationUnitLexerTree);
			validate(EnumSet.of(CURRENT, NEXT, CONSUME), GreaterThanSymbolOperatorToken);
			ATRLFTokenExpressionLexerTree tokenExpressionLexerTree = new ATRLFTokenExpressionLexerTree(name, expressionLexerTree);
			tokenExpressionLexerTree.compilationUnit = compilationUnitLexerTree;
			return tokenExpressionLexerTree;
		}

		return this.onParserCharacter(compilationUnitLexerTree);
	}

		private ArrayList<ATRLFToken> onCalledFunctionParameters() {
			ArrayList<ATRLFToken> parametersLexerTrees = new ArrayList<>();
			ATRLFToken _tmp;

			while (validate(EnumSet.of(CURRENT, SEEK)).type() != ParenthesisRightSymbolDelimiterSeparatorOperatorToken) {
				ATRLFToken name;

				name  = validate(EnumSet.of(CURRENT, NEXT, CONSUME), IdentifierToken);

				if (validate(EnumSet.of(CURRENT, SEEK)).type() == CommaSymbolDelimiterOperatorToken) {
					validate(EnumSet.of(CURRENT, NEXT));
				}
				parametersLexerTrees.add(name);
			}
			return parametersLexerTrees;
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
			parserError(TypeError.PARSER, "Symbol Mismatch (Syntax Error): Expected [" + buildError(types) +
					"], but found [" + t.value() + ']', t);
		}

		if (isCurrent && isNext) {
			scanner.getNextToken();
			return t;
		}

		if (isCurrent) {
			return t;
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
				.map(a -> getName(a.name()))
				.collect(Collectors.joining(" | "));
	}

	private String getName(String name) {
		return switch (name) {
			// --- Keywords ---
			case "PackageKeywordToken" -> "package";
			case "ImportKeywordToken" -> "import";
			case "FromKeywordToken" -> "from";

			case "FunctionKeywordToken" -> "function";

			case "SwitchKeywordToken" -> "switch";
			case "CaseKeywordToken" -> "case";
			case "DefaultKeywordToken" -> "default";

			case "LexerClassTypeToken" -> "Lexer";
			case "InterpreterClassTypeToken" -> "interpreter";

			case "IfKeywordControlToken" -> "if";

			// --- Symbols ---
			case "AtSymbolToken" -> "@";
			case "LowLineSymbolToken" -> "_";
			case "TildeSymbolToken" -> "~";
			case "QuotationSymbolToken" -> "\"";
			case "ApostropheSymbolToken" -> "'";

			// --- Operators ---
			case "EqualSymbolOperatorToken" -> "=";
			case "AndSymbolOperatorToken" -> "&";
			case "VerticalLineSymbolOperatorToken" -> "|";
			case "NotSymbolOperatorToken" -> "!";
			case "QuestionSymbolOperatorToken" -> "?";
			case "LessThanSymbolOperatorToken" -> "<";
			case "GreaterThanSymbolOperatorToken" -> ">";

			case "PlusSymbolArithmeticalOperatorToken" -> "+";
			case "MinusSymbolArithmeticalOperatorToken" -> "-";
			case "StartSymbolArithmeticalOperatorToken" -> "*";
			case "SlashSymbolArithmeticalOperatorToken" -> "/";
			case "ModuleSymbolArithmeticalOperatorToken" -> "%";
			case "ExponentSymbolArithmeticalOperatorToken" -> "^";

			// --- Delimiters / Separators ---
			case "SemicolonSymbolDelimiterOperatorToken" -> ";";
			case "ColonSymbolDelimiterOperatorToken" -> ":";
			case "CommaSymbolDelimiterOperatorToken" -> ",";
			case "DotSymbolDelimiterOperatorToken" -> ".";

			case "ParenthesisLeftSymbolDelimiterSeparatorOperatorToken" -> "(";
			case "ParenthesisRightSymbolDelimiterSeparatorOperatorToken" -> ")";
			case "SquareLeftSymbolDelimiterSeparatorOperatorToken" -> "[";
			case "SquareRightSymbolDelimiterSeparatorOperatorToken" -> "]";
			case "CurlyLeftSymbolDelimiterSeparatorOperatorToken" -> "{";
			case "CurlyRightSymbolDelimiterSeparatorOperatorToken" -> "}";

			// --- Literals ---
			case "StringLiteralToken" -> "string-literal";
			case "CharacterLiteralToken" -> "char-literal";
			case "IntegerLiteralToken" -> "integer-literal";

			// --- Default case ---
			default -> "Not Found";
		};
	}
}