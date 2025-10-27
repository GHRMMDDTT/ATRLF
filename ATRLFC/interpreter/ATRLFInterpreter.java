package ATRLFC.interpreter;

import ATRLFC.tokenizer.ATRLFScanner;
import ATRLFC.tokenizer.ATRLFToken;
import ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType;

import java.util.Arrays;
import java.util.HashMap;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.*;

public class ATRLFInterpreter {
	private final ATRLFScanner scanner;

	public static final int CURRENT = 1;
	public static final int NEXT = 1 << 1;
	public static final int CONSUME = 1 << 2;
	public static final int SEEK = 1 << 3;
	public static final int NOT = 1 << 4;

	public static int counter = 0;

	public static final ATRLFToken TOKEN_NOT_FOUND = new ATRLFToken("\0", NotFoundToken, -1, -1);

	public final HashMap<String, String[]> Functions = new HashMap<>();

	public ATRLFInterpreter(ATRLFScanner scanner) { this.scanner = scanner; }

	public String onInterpreter() {
		StringBuilder builderLexer = new StringBuilder();

		while (validate(CURRENT).type() != EndOfInputFile) {
			builderLexer.append(functionValidationCode()[1]);
		}

		return builderLexer.toString();
	}

	private String[] functionValidationCode() {
		validate(CURRENT | NEXT | CONSUME, FunctionKeywordToken);

		String header = "";
		StringBuilder sb = new StringBuilder();

		ATRLFToken typeClass = validate(CURRENT | NEXT | CONSUME, LexerClassTypeToken);
		validate(CURRENT | NEXT | CONSUME, ColonSymbolDelimiterOperatorToken);
		ATRLFToken name = validate(CURRENT | NEXT | CONSUME, IdentifierToken);

		StringBuilder parameters = new StringBuilder();

		validate(CURRENT | NEXT | CONSUME, ParenthesisLeftSymbolDelimiterSeparatorOperatorToken);

		if (validate(CURRENT | SEEK, IdentifierToken) != TOKEN_NOT_FOUND) {
			parameters.append(parameters());

			while (validate(CURRENT | SEEK, CommaSymbolDelimiterOperatorToken) != TOKEN_NOT_FOUND) {
				validate(CURRENT | NEXT);
				parameters.append(parameters());
			}
		}

		validate(CURRENT | NEXT | CONSUME, ParenthesisRightSymbolDelimiterSeparatorOperatorToken);
		validate(CURRENT | NEXT | CONSUME, ColonSymbolDelimiterOperatorToken);

		ATRLFToken type = validate(CURRENT | NEXT | CONSUME, IdentifierToken);
		validate(CURRENT | NEXT | CONSUME, EqualSymbolOperatorToken);
		validate(CURRENT | NEXT | CONSUME, CurlyLeftSymbolDelimiterSeparatorOperatorToken);

		switch (typeClass.type()) {
			case LexerClassTypeToken -> {
				String[] code = alternativesValidationCode();
				header = code[0];
				sb.append(code[1]);
				Functions.put(name.value(), code);
			}
		}
		validate(CURRENT | NEXT | CONSUME, CurlyRightSymbolDelimiterSeparatorOperatorToken);

		if (validate(CURRENT | SEEK | NEXT, EqualSymbolOperatorToken) != TOKEN_NOT_FOUND) {
			validate(CURRENT | NEXT | CONSUME, GreaterThanSymbolOperatorToken);

			ATRLFToken tokenName = validate(CURRENT | NEXT | CONSUME, IdentifierToken);


			sb.insert(0, "public " + (type.value().equals("Unit") ? "Token " : type.value()) + name.value() + "(int startPos, " + (parameters.isEmpty() ? "" : ", " + parameters) + ") {\n");
			sb.append("\nreturn new Token(new String(this.buffer, startPos, this.position - startPos), MyTokens.").append(tokenName.value()).append(");");
		} else {
			sb.insert(0, "public void " + name.value() + "(int startPos" + (parameters.isEmpty() ? "" : ", " + parameters) + ") {\n");
		}
		sb.append("\n}\n");

		validate(CURRENT | NEXT | CONSUME, SemicolonSymbolDelimiterOperatorToken);

		return new String[] { header, sb.toString() };
	}

	private String parameters() {
		ATRLFToken name = validate(CURRENT | NEXT | CONSUME, IdentifierToken);
		validate(CURRENT | NEXT | CONSUME, ColonSymbolDelimiterOperatorToken);
		ATRLFToken type = validate(CURRENT | NEXT | CONSUME, IdentifierToken);
		return type.value() + " " + name.value();
	}

	private String[] alternativesValidationCode() {
		StringBuilder sb = new StringBuilder();
		boolean isPipe = false;
		String[] code = sequenceValidationCode();
		StringBuilder first = new StringBuilder(code[0]);

		while (validate(CURRENT | SEEK, VerticalLineSymbolOperatorToken) != TOKEN_NOT_FOUND) {
			validate(CURRENT | NEXT | CONSUME, VerticalLineSymbolOperatorToken);
			if (!isPipe) sb.append("if (has(").append(code[0]).append(")) {\n").append(code[1]).append("} else ");
			code = sequenceValidationCode();
			first.append(")||has(").append(code[0]);
			sb.append("if (has(").append(code[0]).append(")) {\n").append(code[1]).append("} else ");
			isPipe = true;
		}

		if (!isPipe) {
			sb.append(code[1]);
		} else {
			sb.setLength(sb.length() - 6);
		}

		return new String[] { first.toString(), sb.toString() };
	}

	private String[] sequenceValidationCode() {
		StringBuilder sb = new StringBuilder();

		String value = null;
		while (validate(CURRENT | SEEK, VerticalLineSymbolOperatorToken).type() != VerticalLineSymbolOperatorToken && validate(CURRENT | SEEK, ParenthesisRightSymbolDelimiterSeparatorOperatorToken).type() != ParenthesisRightSymbolDelimiterSeparatorOperatorToken && validate(CURRENT).type() != EndOfInputFile && validate(CURRENT).type() != CurlyRightSymbolDelimiterSeparatorOperatorToken) {
			String code;
			if (value == null) {
				String[] args = regexValidationCode();
				value = args[0];
				code = args[1];
			} else {
				code = regexValidationCode()[1];
			}
			sb.append(code);
		}

		return new String[] { value, sb.toString() };
	}

	private String[] regexValidationCode() {
		ATRLFToken character = validate(CURRENT | CONSUME | NEXT, CharacterLiteralToken, ParenthesisLeftSymbolDelimiterSeparatorOperatorToken, SquareLeftSymbolDelimiterSeparatorOperatorToken, IdentifierToken);
		return switch (character.type()) {
			case ParenthesisLeftSymbolDelimiterSeparatorOperatorToken -> regexParenthesisValidationCode();
			case SquareLeftSymbolDelimiterSeparatorOperatorToken -> regexRangeValidationCode();
			case IdentifierToken -> regexFunctionValidationCode(character);
			case null, default -> regexCharacterValidationCode(character);
		};
	}

		public String[] regexParenthesisValidationCode() {
			String[] str = alternativesValidationCode();
			validate(CURRENT | CONSUME | NEXT, ParenthesisRightSymbolDelimiterSeparatorOperatorToken);
			switch (validate(CURRENT | SEEK | NEXT, PlusSymbolArithmeticalOperatorToken, QuestionSymbolOperatorToken, NotSymbolOperatorToken, CurlyLeftSymbolDelimiterSeparatorOperatorToken).type()) {
				case QuestionSymbolOperatorToken -> {
					if (validate(CURRENT | SEEK | NEXT, PlusSymbolArithmeticalOperatorToken) != TOKEN_NOT_FOUND) {
						String code = "while (has(" + str[0] + ")) {\n" + str[1] + "\n}\n";
						return new String[] { str[0], code };
					}
					String code = "if (has(" + str[0] + ")) {\n" + str[1] + "}\n";
					return new String[] { str[0], code };
				}
				case PlusSymbolArithmeticalOperatorToken -> {
					String code = "do {\n" + str[1] + "\n} while (has(" + str[0] + "));\n";
					return new String[] { str[0], code };
				}
				case CurlyLeftSymbolDelimiterSeparatorOperatorToken -> {
					StringBuilder sb = new StringBuilder();
					while (validate(CURRENT).type() != CurlyRightSymbolDelimiterSeparatorOperatorToken) {
						ATRLFToken t1 = validate(CURRENT | CONSUME | NEXT, IntegerLiteralToken);
						if (validate(CURRENT | SEEK, MinusSymbolArithmeticalOperatorToken) != TOKEN_NOT_FOUND) {
							validate(CURRENT | NEXT);
							ATRLFToken t2 = validate(CURRENT | CONSUME | NEXT, IntegerLiteralToken);
							sb.append("(i").append(++counter).append(" >= ").append(t1.value()).append(" && i").append(counter).append(" <= ").append(t2.value()).append(")");
							sb.append(" || ");

							if (validate(CURRENT | SEEK, CommaSymbolDelimiterOperatorToken) != TOKEN_NOT_FOUND) {
								validate(CURRENT | NEXT);
							}
						} else if (validate(CURRENT | SEEK, CommaSymbolDelimiterOperatorToken) != TOKEN_NOT_FOUND) {
							validate(CURRENT | NEXT);
							sb.append("i").append(counter).append(" == ").append(t1.value());
							sb.append(" || ");
						} else {
							sb.append("i").append(counter).append(" == ").append(t1.value());
							sb.append(" || ");
						}
					}
					sb.setLength(sb.length() - 4);
					validate(CURRENT | NEXT);
					return new String[] {str[0] , "{ int i" + counter + " = 0; while(has(" + str[0] +")) { " + str[1] + " i" + counter + "++; } if (!(" + sb + ")) { error(); } }\n" };
				}
				default -> {
					return new String[] { str[0], str[1] };
				}
			}
		}

		public String[] regexRangeValidationCode() {
			StringBuilder sb = new StringBuilder();
			sb.append("if (");
			while (validate(CURRENT).type() != SquareRightSymbolDelimiterSeparatorOperatorToken) {
				ATRLFToken t1 = validate(CURRENT | CONSUME | NEXT, CharacterLiteralToken);
				if (validate(CURRENT | SEEK, MinusSymbolArithmeticalOperatorToken) != TOKEN_NOT_FOUND) {
					validate(CURRENT | NEXT);
					ATRLFToken t2 = validate(CURRENT | CONSUME | NEXT, CharacterLiteralToken);
					sb.append("(peek() >= ").append(t1.value()).append(" && peek() <= ").append(t2.value()).append(")");
					sb.append(" || ");

					if (validate(CURRENT | SEEK, CommaSymbolDelimiterOperatorToken) != TOKEN_NOT_FOUND) {
						validate(CURRENT | NEXT);
					}
				} else if (validate(CURRENT | SEEK, CommaSymbolDelimiterOperatorToken) != TOKEN_NOT_FOUND) {
					validate(CURRENT | NEXT);
					sb.append("peek() == ").append(t1.value());
					sb.append(" || ");
				} else {
					sb.append("peek() == ").append(t1.value());
					sb.append(" || ");
				}
			}
			String condition = sb.substring(4, sb.length() - 4);
			sb.setLength(sb.length() - 4);
			sb.append(") {");
			validate(CURRENT | NEXT);

			switch (validate(CURRENT | SEEK | NEXT, PlusSymbolArithmeticalOperatorToken, QuestionSymbolOperatorToken, NotSymbolOperatorToken, CurlyLeftSymbolDelimiterSeparatorOperatorToken).type()) {
				case PlusSymbolArithmeticalOperatorToken -> {
					sb.append(" do { consume(); } while (").append(condition).append("); } else { error(); }\n");
					return new String[] { condition, sb.toString() };
				}
				case NotSymbolOperatorToken -> {
					sb.insert(4, "!").insert(condition.length() + 4 + 1, ")");
					sb.append(" consume() } else { error(); }\n");
					return new String[] { condition, sb.toString() };
				}
				case QuestionSymbolOperatorToken -> {
					if (validate(CURRENT | SEEK | NEXT, PlusSymbolArithmeticalOperatorToken) != TOKEN_NOT_FOUND) {
						sb.replace(0, 4, "while (").append(" consume() }\n");
						return new String[] { condition, sb.toString() };
					}
					sb.append(" consume(); }\n");
					return new String[] { condition, sb.toString() };
				}
				case CurlyLeftSymbolDelimiterSeparatorOperatorToken -> {
					StringBuilder sb2 = new StringBuilder();
					while (validate(CURRENT).type() != CurlyRightSymbolDelimiterSeparatorOperatorToken) {
						ATRLFToken t1 = validate(CURRENT | CONSUME | NEXT, IntegerLiteralToken);
						if (validate(CURRENT | SEEK, MinusSymbolArithmeticalOperatorToken) != TOKEN_NOT_FOUND) {
							validate(CURRENT | NEXT);
							ATRLFToken t2 = validate(CURRENT | CONSUME | NEXT, IntegerLiteralToken);
							sb2.append("(i").append(++counter).append(" >= ").append(t1.value()).append(" && i").append(counter).append(" <= ").append(t2.value()).append(")");
							sb2.append(" || ");

							if (validate(CURRENT | SEEK, CommaSymbolDelimiterOperatorToken) != TOKEN_NOT_FOUND) {
								validate(CURRENT | NEXT);
							}
						} else if (validate(CURRENT | SEEK, CommaSymbolDelimiterOperatorToken) != TOKEN_NOT_FOUND) {
							validate(CURRENT | NEXT);
							sb2.append("i").append(counter).append(" == ").append(t1.value());
							sb2.append(" || ");
						} else {
							sb2.append("i").append(counter).append(" == ").append(t1.value());
							sb2.append(" || ");
						}
					}
					sb2.setLength(sb2.length() - 4);
					validate(CURRENT | NEXT);
					return new String[] {condition , "{ int i" + counter + " = 0; while(" + condition + ") { consume(); i" + counter + "++; } if (!(" + sb2 + ")) { error(); } }\n" };
				}
				default -> {
					sb.append(" consume(); } else { error(); }\n");
					return new String[] { condition, sb.toString() };
				}
			}
		}

		public String[] regexFunctionValidationCode(ATRLFToken name) {
			if (!Functions.containsKey(name.value())) throw new RuntimeException("No function found");
			String[] arg = Functions.get(name.value());
			return new String[] { arg[0], name.value() + "(this.position);\n" };
		}

		public String[] regexCharacterValidationCode(ATRLFToken character) {
			switch (validate(CURRENT | SEEK | NEXT, PlusSymbolArithmeticalOperatorToken, QuestionSymbolOperatorToken, NotSymbolOperatorToken, CurlyLeftSymbolDelimiterSeparatorOperatorToken).type()) {
				case PlusSymbolArithmeticalOperatorToken -> {
					return new String[] { character.value(), "do { accept(" + character.value() + "); } while (has(" + character.value() + "));\n" };
				}
				case NotSymbolOperatorToken -> {
					return new String[] { character.value(), "if (!has(" + character.value() + ")) { consume(); } else { error(" + character.value() + "); }\n" };
				}
				case QuestionSymbolOperatorToken -> {
					if (validate(CURRENT | SEEK | NEXT, PlusSymbolArithmeticalOperatorToken) != TOKEN_NOT_FOUND) {
						return new String[] { character.value(), "while (has(" + character.value() + ")) { consume(); }\n" };
					}
					return new String[] { character.value(), "if (has(" + character.value() + ")) { consume(); }\n" };
				}
				case CurlyLeftSymbolDelimiterSeparatorOperatorToken -> {
					StringBuilder sb = new StringBuilder();
					while (validate(CURRENT).type() != CurlyRightSymbolDelimiterSeparatorOperatorToken) {
						ATRLFToken t1 = validate(CURRENT | CONSUME | NEXT, IntegerLiteralToken);
						if (validate(CURRENT | SEEK, MinusSymbolArithmeticalOperatorToken) != TOKEN_NOT_FOUND) {
							validate(CURRENT | NEXT);
							ATRLFToken t2 = validate(CURRENT | CONSUME | NEXT, IntegerLiteralToken);
							sb.append("(i").append(counter).append(" >= ").append(t1.value()).append(" && i").append(counter).append(" <= ").append(t2.value()).append(")");
							sb.append(" || ");

							if (validate(CURRENT | SEEK, CommaSymbolDelimiterOperatorToken) != TOKEN_NOT_FOUND) {
								validate(CURRENT | NEXT);
							}
						} else if (validate(CURRENT | SEEK, CommaSymbolDelimiterOperatorToken) != TOKEN_NOT_FOUND) {
							sb.append("i").append(counter).append(" == ").append(t1.value());
							sb.append(" || ");
						} else {
							sb.append("i").append(counter).append(" == ").append(t1.value());
							sb.append(" || ");
						}
					}
					sb.setLength(sb.length() - 4);
					validate(CURRENT | NEXT);
					return new String[] { character.value(), "{ int i" + counter + " = 0; while(has(" + character.value() +")) { consume(); i" + counter + "++; } if (!(" + sb + ")) { error(" + character.value() + "); } }\n" };
				}
				default -> {
					return new String[] { character.value(), "accept(" + character.value() + ");\n" };
				}
			}
		}

	public ATRLFToken validate(int mask, ATRLFTokenType... types) {
		boolean isCurrent = (mask & CURRENT) != 0;
		boolean isNext = (mask & NEXT) != 0;
		boolean isConsume = (mask & CONSUME) != 0;
		boolean isNot = (mask & NOT) != 0;
		ATRLFToken t = null;

		if (types.length != 0) {
			boolean isSeek = (mask & SEEK) != 0;

			if (isCurrent) {
				t = this.scanner.getToken();
				while (t == null) {
					this.scanner.getNextToken();
					t = this.scanner.getToken();
				}
			} else {
				System.err.println("[ATRLF Parser] Flag Mask Missing (Construction Error): Missing the Flag Mask 'Current' for get the Token,Otherwise nothing can be done.");
				System.exit(-1);
			}

			if (isNot) {
				for (ATRLFTokenType type : types) {
					if (type == t.type()) {
						System.err.println("[ATRLF Parser] Symbol Match With Not Match (Syntax Error): The type '" + t.value() + "' exactly match the '" + buildError(types) + "' above the: " + t.line() + ':' + t.column());
						System.exit(-1);
					}
				}
				if (isNext) { this.scanner.getNextToken(); }
				return t;
			}

			if (isSeek) {
				for (ATRLFTokenType type : types) { if (type == t.type()) { if (isNext) { this.scanner.getNextToken(); } return t; } }
				return TOKEN_NOT_FOUND;
			}

			if (isConsume) {
				for (ATRLFTokenType type : types) { if (type == t.type()) { if (isNext) this.scanner.getNextToken(); return t; } }
				System.err.println("[ATRLF Parser] Symbol Mismatch (Syntax Error): The type '" + t.value() + "' does bit exactly match the '" + buildError(types) + "' above the: " + t.line() + ':' + t.column());
				System.exit(-1);
			}

			System.err.println("[ATRLF Parser] Flags Mask Missing (Construction Error): The Flags Mask are missing in order to properly 'validate' the token.");
			System.exit(-1);

			return null;
		}

		if (isCurrent) {
			t = this.scanner.getToken();
			while (t == null) {
				this.scanner.getNextToken();
				t = this.scanner.getToken();
			}
		} else {
			System.err.println("[ATRLF Parser] Flag Mask Missing (Construction Error): Missing the Flag Mask 'Current' for get the Token,Otherwise nothing can be done.");
			System.exit(-1);
		}

		if (isNext) {
			this.scanner.getNextToken();
		}

		return t;
	}

	private String buildError(ATRLFTokenType... types) {
		StringBuilder sb = new StringBuilder(64);
		for (ATRLFTokenType type : types) {
			sb.append(type).append(" | ");
		}
		sb.setLength(sb.length() - 3);
		return sb.toString();
	}

	public static final String peekCode = "\nprivate char peek() {\nif (this.position >= this.target.length) return '\\0';\nreturn this.target[this.position];\n}";
	public static final String has$argument_char$Code = "\nprivate final boolean has(char target) {\nreturn peek() == target || peek() != '\\0';\n}\n";
	public static final String consumeCode = "\nprivate final void consume() {\nthis.position++;\n}\n";
	public static final String accept$argument_char$Code = "\nprivate final void accept(char target) {\nif (has(target)) {\nconsume();\nreturn;\n}\nerror(target);\n}\n";
	public static final String error$argument_char$Code = "\nprivate final void error(char target) {\n\tif (this.peek() == '\\0') return;\nthrow new RuntimeException(\"No matched in: \" + target + \" with: \" + this.peek());\n}\n";
	public static final String errorCode = "\nprivate final char error() {\nif (this.peek() == '\\0') return;\nthrow new RuntimeException(\"No matched in: \" + this.peek());\n}\n";
}
