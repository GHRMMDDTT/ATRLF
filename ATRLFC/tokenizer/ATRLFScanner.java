package ATRLFC.tokenizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static ATRLFC.tokenizer.ATRLFToken.ATRLFTokenType.*;

public class ATRLFScanner {
	public ATRLFToken tokens;

	private final char[] target;

	private int position, line = 1, column;

	public ATRLFScanner(char[] target) { this.target = target; }

	public ATRLFScanner(String target) { this.target = target.toCharArray(); }

	private final boolean[] symbols = new boolean[128]; {
		// Symbols:
		this.symbols['@'] = true;
		this.symbols['_'] = true;
		this.symbols['~'] = true;
		this.symbols['"'] = true;

		// Symbols Operators;
		this.symbols['='] = true;
		this.symbols['&'] = true;
		this.symbols['|'] = true;
		this.symbols['!'] = true;
		this.symbols['?'] = true;
		this.symbols['<'] = true;
		this.symbols['>'] = true;

		// Symbols Arithmetical Operators:
		this.symbols['+'] = true;
		this.symbols['-'] = true;
		this.symbols['*'] = true;
		this.symbols['/'] = true;
		this.symbols['%'] = true;
		this.symbols['^'] = true;

		// Symbols Delimiters Operators;
		this.symbols[';'] = true;
		this.symbols[':'] = true;
		this.symbols[','] = true;
		this.symbols['.'] = true;

		// Symbols Delimiter Separator Operators
		this.symbols['('] = true;
		this.symbols[')'] = true;
		this.symbols['['] = true;
		this.symbols[']'] = true;
		this.symbols['{'] = true;
		this.symbols['}'] = true;
	}

	public ATRLFScanner(File file) {
		this.target = this.readFile(file);
	}

	private char[] readFile(File file) {
		try (FileInputStream fis = new FileInputStream(file)) {
			FileChannel channel = fis.getChannel();
			MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

			Charset charset = StandardCharsets.UTF_8;
			CharBuffer charBuffer = charset.decode(buffer);

			char[] result = new char[charBuffer.remaining()];
			charBuffer.get(result);
			return result;
		} catch (IOException e) {
			return new char[0];
		}
	}

	private ATRLFToken getCommentaryLiteral(boolean isMultipleCommentary) {
		if (!isMultipleCommentary) {
			do {
				if (this.target[this.position] == '\n') {
					break;
				}
				this.position++;
			} while (this.position < this.target.length);
		} else {
			do {
				if (this.target[this.position] == '*') {
					this.position++;
					if (this.target[this.position] == '/') {
						this.position++;
						break;
					}
				}
				if (this.target[this.position] == '\n') {
					this.line++;
				}
				this.position++;
			} while (this.position < this.target.length);
		}
		return this.getNextToken();
	}

	public void skipWhitespace() {
		do {
			if (this.target[this.position] == '\r' || this.target[this.position] == '\n') {
				this.column = this.position + 1;
				this.line++;
			}
			this.position++;
		} while (this.position < this.target.length && (this.target[this.position] == ' ' || this.target[this.position] == '\r' || this.target[this.position] == '\t' || this.target[this.position] == '\f' || this.target[this.position] == '\b' || this.target[this.position] == '\n'));
	}

	private ATRLFToken getIdentifier(int startPos) {
		do {
			this.position++;
		} while (this.position < this.target.length && ((this.target[this.position] >= 'a' && this.target[this.position] <= 'z') || (this.target[this.position] >= 'A' && this.target[this.position] <= 'Z')) || (this.target[this.position] >= '0' && this.target[this.position] <= '9') || (this.target[this.position] == '$') || (this.target[this.position] == '_'));

		String text = new String(this.target, startPos, this.position - startPos);
		return new ATRLFToken(text, switch (text) {
			case "package" -> PackageKeywordToken;
			case "import" -> ImportKeywordToken;
			case "from" -> FromKeywordToken;

			case "public" -> PublicKeywordToken;
			case "private" -> PrivateKeywordToken;
			case "protected" -> ProtectedKeywordToken;

			case "Lexer" -> LexerClassTypeToken;
			case "interpreter" -> InterpreterClassTypeToken;

			case "function" -> FunctionKeywordToken;

			case "if" -> IfKeywordControlToken;
			default -> IdentifierToken;
		}, this.line, this.position - this.column);
	}

	private ATRLFToken getCharacterLiteral(int startPos) {
		boolean isEnded = true;
		int isUnicode = 0;
		this.position++;

		if (this.position < this.target.length && this.target[this.position] == '\\') {
			boolean isValidHexadecimal = false;
			this.position++;

			if (this.position < this.target.length && (((this.target[this.position] >= 'a' && this.target[this.position] <= 'f') || (this.target[this.position] >= 'A' && this.target[this.position] <= 'F') || (this.target[this.position] >= '0' && this.target[this.position] <= '9')) || this.target[this.position] == 'r' || this.target[this.position] == 't' || this.target[this.position] == 'f' || this.target[this.position] == 'n' || this.target[this.position] == '\\' || this.target[this.position] == '\'' || this.target[this.position] == '\"')) {
				this.position++;
				isValidHexadecimal = true;
				isUnicode = 2;
			} else if (this.target[this.position] == 'u') {
				this.position++;
				if ((this.target[this.position] >= 'a' && this.target[this.position] <= 'f') || (this.target[this.position] >= 'A' && this.target[this.position] <= 'F') || (this.target[this.position] >= '0' && this.target[this.position] <= '9')) {
					this.position++;
					isValidHexadecimal = true;
				}
				if ((this.target[this.position] >= 'a' && this.target[this.position] <= 'f') || (this.target[this.position] >= 'A' && this.target[this.position] <= 'F') || (this.target[this.position] >= '0' && this.target[this.position] <= '9')) {
					this.position++;
					isValidHexadecimal = true;
				}
				if ((this.target[this.position] >= 'a' && this.target[this.position] <= 'f') || (this.target[this.position] >= 'A' && this.target[this.position] <= 'F') || (this.target[this.position] >= '0' && this.target[this.position] <= '9')) {
					this.position++;
					isValidHexadecimal = true;
				}
				if ((this.target[this.position] >= 'a' && this.target[this.position] <= 'f') || (this.target[this.position] >= 'A' && this.target[this.position] <= 'F') || (this.target[this.position] >= '0' && this.target[this.position] <= '9')) {
					this.position++;
					isValidHexadecimal = true;
				}
				isUnicode = 1;
			}

			if (!isValidHexadecimal) {
				throw new RuntimeException("El escapable hexadecimal es incorrecto del de caracter: " + new String(this.target, startPos, this.position - startPos));
			}

		} else {
			this.position++;
		}

		if (this.position > this.target.length || this.target[this.position] != '\'') {
			isEnded = false;
		}

		if (!isEnded) {
			throw new RuntimeException("Falta en el final el \"'\" para dar por terminado el literal caracter en la linea: " + this.line);
		}

		this.position++;

		if (isUnicode == 1) {
			return new ATRLFToken(new String(this.target, startPos, 6), CharacterLiteralToken, this.line, this.position - this.column);
		} else if (isUnicode == 2) {
			return new ATRLFToken(new String(this.target, startPos, 4), CharacterLiteralToken, this.line, this.position - this.column);
		} else {
			return new ATRLFToken(new String(this.target, startPos, 3), CharacterLiteralToken, this.line, this.position - this.column);
		}
	}

	private ATRLFToken getNumericLiteral(int startPos) {
		do {
			this.position++;
		} while (this.position < this.target.length && (this.target[this.position] >= '0' && this.target[this.position] <= '9'));

		return new ATRLFToken(new String(this.target, startPos, this.position - startPos), IntegerLiteralToken, this.line, this.position - this.column);
	}

	private ATRLFToken getSymbol(int startPos) {
		if (this.position < this.target.length) {
			if (this.target[this.position] == '/') {
				if (this.target[this.position + 1] == '/') {
					this.position++;
					return this.getCommentaryLiteral(false);
				} else if (this.target[this.position + 1] == '*') {
					this.position++;
					return this.getCommentaryLiteral(true);
				}
			}
		}

		return new ATRLFToken(String.valueOf(this.target[this.position++]), switch (this.target[startPos]) {
			case '@' -> AtSymbolToken;
			case '_' -> LowLineSymbolToken;
			case '~' -> TildeSymbolToken;
			case '"' -> QuotationSymbolToken;
			case '\'' -> ApostropheSymbolToken;

			// Symbols Operators;
			case '=' -> EqualSymbolOperatorToken;
			case '&' -> AndSymbolOperatorToken;
			case '|' -> VerticalLineSymbolOperatorToken;
			case '!' -> NotSymbolOperatorToken;
			case '?' -> QuestionSymbolOperatorToken;
			case '<' -> LessThanSymbolOperatorToken;
			case '>' -> GreaterThanSymbolOperatorToken;

			// Symbols Arithmetical Operators:
			case '+' -> PlusSymbolArithmeticalOperatorToken;
			case '-' -> MinusSymbolArithmeticalOperatorToken;
			case '*' -> StartSymbolArithmeticalOperatorToken;
			case '/' -> SlashSymbolArithmeticalOperatorToken;
			case '%' -> ModuleSymbolArithmeticalOperatorToken;
			case '^' -> ExponentSymbolArithmeticalOperatorToken;

			// Symbols Delimiters Operators;
			case ';' -> SemicolonSymbolDelimiterOperatorToken;
			case ':' -> ColonSymbolDelimiterOperatorToken;
			case ',' -> CommaSymbolDelimiterOperatorToken;
			case '.' -> DotSymbolDelimiterOperatorToken;

			// Symbols Delimiter Separator Operators
			case '(' -> ParenthesisLeftSymbolDelimiterSeparatorOperatorToken;
			case ')' -> ParenthesisRightSymbolDelimiterSeparatorOperatorToken;
			case '[' -> SquareLeftSymbolDelimiterSeparatorOperatorToken;
			case ']' -> SquareRightSymbolDelimiterSeparatorOperatorToken;
			case '{' -> CurlyLeftSymbolDelimiterSeparatorOperatorToken;
			case '}' -> CurlyRightSymbolDelimiterSeparatorOperatorToken;
			default -> throw new IllegalStateException("Unexpected value: " + this.target[this.position]);
		}, this.line, this.position - this.column);
	}

	public ATRLFToken getNextToken() {
		if (this.position >= this.target.length) {
			this.tokens = new ATRLFToken("\3", EndOfInputFile, this.line, this.target.length);
			return this.tokens;
		}

		if (this.target[this.position] == ' ' || this.target[this.position] == '\r' || this.target[this.position] == '\t' || this.target[this.position] == '\f' || this.target[this.position] == '\b' || this.target[this.position] == '\n') {
			skipWhitespace();

			if (this.position >= this.target.length) {
				this.tokens = new ATRLFToken("\3", EndOfInputFile, this.line, this.target.length);
				return this.tokens;
			}
		}

		if ((this.target[this.position] >= 'a' && this.target[this.position] <= 'z') || (this.target[this.position] >= 'A' && this.target[this.position] <= 'Z')) {
			this.tokens = this.getIdentifier(this.position);
			return this.tokens;
		}

		if ((this.target[this.position] >= '0' && this.target[this.position] <= '9')) {
			this.tokens = this.getNumericLiteral(this.position);
			return this.tokens;
		}

		if (this.target[this.position] == '\'') {
			this.tokens = this.getCharacterLiteral(this.position);
			return this.tokens;
		}

		if (this.symbols[this.target[this.position]]) {
			this.tokens = this.getSymbol(this.position);
			return this.tokens;
		}

		System.err.println("[ATRLF Scanner] Symbol Mismatched (Syntax Error): Unexpected symbol: '" + this.target[this.position] + "' in the matched identifier | symbols. In " + this.line + ':' + (this.position - this.column));
		System.exit(-1);
		return null;
	}

	public ATRLFToken getToken() {
		return this.tokens;
	}
}
