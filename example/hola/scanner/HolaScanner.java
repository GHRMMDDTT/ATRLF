package example.hola.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HolaScanner {
	private final char[] target;
	private int position;

	private int line;
	private int column;

	public HolaScanner(char[] target) {
		this.target = target;
	}

	public HolaScanner(String target) {
		this.target = target.toCharArray();
	}

	public HolaScanner(File target) {
		this.target = this.readFile(target);
	}

	private Token EOIF = new Token("\0", Token.TokenSyntax.EndOfInputFileToken, this.column + 1, this.line);

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

	public Token getNextToken() {
		int oldPosition = this.position;
		if (this.position >= this.target.length) {
			return this.EOIF;
		}
		whitespace();
		if ((this.peek() >= 'a' && this.peek() <= 'z') || (this.peek() >= 'A' && this.peek() <= 'Z')) {
			return identifier();
		} else if (this.has('0') || (this.peek() >= '1' && this.peek() <= '9')) {
			return numeric();
		} else if (this.has('\'')) {
			return character();
		} else if (this.has('"')) {
			return string();
		} else if (this.has('=') || this.has('>') || this.has('<') || this.has('+') || this.has('-') || this.has('*') || this.has('/') || this.has('{') || this.has('}')) {
			return operator();
		} else {
			this.error();
		}
		return new Token(String.valueOf(this.peek()), Token.TokenSyntax.BadToken, this.column, this.line);
	}

	public Token identifier() {
		int oldPosition = this.position;
		if ((this.peek() >= 'a' && this.peek() <= 'z')) {
			this.consume();
		} else if ((this.peek() >= 'A' && this.peek() <= 'Z')) {
			this.consume();
		} else {
			this.error();
		}
		while ((this.peek() >= 'a' && this.peek() <= 'z') || (this.peek() >= 'A' && this.peek() <= 'Z') || (this.peek() >= '0' && this.peek() <= '9') || has('_')) {
			if ((this.peek() >= 'a' && this.peek() <= 'z')) {
				this.consume();
			} else if ((this.peek() >= 'A' && this.peek() <= 'Z')) {
				this.consume();
			} else if ((this.peek() >= '0' && this.peek() <= '9')) {
				this.consume();
			} else if (this.has('_')) {
				this.consume();
			} else {
				this.error();
			}
		}String value = new String(this.target, oldPosition, this.position - oldPosition);
		switch (value) {
			case "if": {
				return new Token(value, Token.TokenSyntax.IfKeywordToken, this.column, this.line);
			}
			case "else": {
				return new Token(value, Token.TokenSyntax.ElseKeywordToken, this.column, this.line);
			}
			case "switch": {
				return new Token(value, Token.TokenSyntax.SwitchKeywordToken, this.column, this.line);
			}
			case "case": {
				return new Token(value, Token.TokenSyntax.CaseKeywordToken, this.column, this.line);
			}
			case "default": {
				return new Token(value, Token.TokenSyntax.DefaultKeywordToken, this.column, this.line);
			}
			default: {
				return new Token(value, Token.TokenSyntax.IdentifierToken, this.column, this.line);
			}
		}
	}

	public Token numeric() {
		int oldPosition = this.position;
		if (this.has('0')) {
			return compactNumeric();
		} else if ((this.peek() >= '1' && this.peek() <= '9')) {
			if ((this.peek() >= '1' && this.peek() <= '9')) {
				this.consume();
			} else {
				this.error();
			}
			while ((this.peek() >= '0' && this.peek() <= '9')) {
				if ((this.peek() >= '0' && this.peek() <= '9')) {
					this.consume();
				} else {
					this.error();
				}
			}
		} else {
			this.error();
		}
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.NumericLiteralToken, this.column, this.line);
	}

	public Token compactNumeric() {
		int oldPosition = this.position;
		this.accept('0');
		if (this.has('x') || this.has('b') || this.has('o')) {
			if (this.has('x')) {
				this.accept('x');
				do {
					if ((this.peek() >= 'a' && this.peek() <= 'f')) {
						this.consume();
					} else if ((this.peek() >= 'A' && this.peek() <= 'F')) {
						this.consume();
					} else if ((this.peek() >= '0' && this.peek() <= '9')) {
						this.consume();
					} else {
						this.error();
					}
				} while ((this.peek() >= 'a' && this.peek() <= 'f') || (this.peek() >= 'A' && this.peek() <= 'F') || (this.peek() >= '0' && this.peek() <= '9'));
				return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.NumberHexadecimalLiteralToken, this.column, this.line);
			} else if (this.has('b')) {
				this.accept('b');
				do {
					if ((this.peek() >= '0' && this.peek() <= '1')) {
						this.consume();
					} else {
						this.error();
					}
				} while ((this.peek() >= '0' && this.peek() <= '1'));
				return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.NumberBinaryLiteralToken, this.column, this.line);
			} else if (this.has('o')) {
				this.accept('o');
				do {
					if ((this.peek() >= '0' && this.peek() <= '7')) {
						this.consume();
					} else {
						this.error();
					}
				} while ((this.peek() >= '0' && this.peek() <= '7'));
				return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.NumberOctalLiteralToken, this.column, this.line);
			} else {
				this.error();
			}
		}
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.NeutralLiteralToken, this.column, this.line);
	}

	public void whitespace() {
		int oldPosition = this.position;
		while (this.has(' ') || this.has('\t') || this.has('\r') || this.has('\n')) {
			if (this.has(' ')) {
				this.accept(' ');
			} else if (this.has('\t')) {
				this.accept('\t');
			} else if (this.has('\r')) {
				this.accept('\r');
			} else if (this.has('\n')) {
				this.line++;
				this.column = this.position;
				this.accept('\n');
			} else {
				this.error();
			}
		}
	}

	public Token character() {
		int oldPosition = this.position;
		this.accept('\'');
		if (this.has('\\')) {
			escapeCharacter();
		} else if (!this.has('\'')) {
			if (!this.has('\'')) {
				this.consume();
			} else {
				this.error();
			}
		} else {
			this.error();
		}
		this.accept('\'');
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.CharacterLiteralToken, this.column, this.line);
	}

	public Token string() {
		int oldPosition = this.position;
		this.accept('"');
		while (this.has('\\') || !this.has('"')) {
			if (this.has('\\')) {
				escapeCharacter();
			} else if (!this.has('"')) {
				if (!this.has('"')) {
					this.consume();
				} else {
					this.error();
				}
			} else {
				this.error();
			}
		}
		this.accept('"');
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.StringLiteralToken, this.column, this.line);
	}

	public void escapeCharacter() {
		int oldPosition = this.position;
		this.accept('\\');
		if (this.has('r')) {
			this.accept('r');
		} else if (this.has('t')) {
			this.accept('t');
		} else if (this.has('f')) {
			this.accept('f');
		} else if (this.has('n')) {
			this.accept('n');
		} else if (this.has('b')) {
			this.accept('b');
		} else if (this.has('\\')) {
			this.accept('\\');
		} else if (this.has('\'')) {
			this.accept('\'');
		} else if (this.has('"')) {
			this.accept('"');
		} else if (this.has('u')) {
			this.accept('u');
			{
				int count2 = 0;
				while ((this.peek() >= 'a' && this.peek() <= 'f') || (this.peek() >= 'A' && this.peek() <= 'F') || (this.peek() >= '0' && this.peek() <= '9')) {
					this.consume();
					count2++;
				}
				if (!(count2 == 4)) {
					this.error();
				}
			}
		} else if (this.has('o')) {
			this.accept('o');
			this.accept('0');
			if ((this.peek() >= '0' && this.peek() <= '7')) {
				this.consume();
			} else {
				this.error();
			}
		} else {
			this.error();
		}
	}

	public Token operator() {
		int oldPosition = this.position;
		if (this.has('=')) {
			this.accept('=');
			if (this.has('=') || this.has('>')) {
				if (this.has('=')) {
					this.accept('=');
					return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.EqualEqualSymbolOperatorToken, this.column, this.line);
				} else if (this.has('>')) {
					this.accept('>');
					return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.FlatLineSymbolOperatorToken, this.column, this.line);
				} else {
					this.error();
				}
			}
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.EqualSymbolOperatorToken, this.column, this.line);
		} else if (this.has('>')) {
			this.accept('>');
			if (this.has('=')) {
				this.accept('=');
				return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.EqualGreaterSymbolOperatorToken, this.column, this.line);
			}
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.GreaterSymbolOperatorToken, this.column, this.line);
		} else if (this.has('<')) {
			this.accept('<');
			if (this.has('=')) {
				this.accept('=');
				return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.EqualLessSymbolOperatorToken, this.column, this.line);
			}
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.LessSymbolOperatorToken, this.column, this.line);
		} else if (this.has('+')) {
			this.accept('+');
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.PlusSymbolOperatorToken, this.column, this.line);
		} else if (this.has('-')) {
			this.accept('-');
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.MinusSymbolOperatorToken, this.column, this.line);
		} else if (this.has('*')) {
			this.accept('*');
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.StartSymbolOperatorToken, this.column, this.line);
		} else if (this.has('/')) {
			return isComment();
		} else if (this.has('{')) {
			this.accept('{');
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.CurlyLeft, this.column, this.line);
		} else if (this.has('}')) {
			this.accept('}');
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.CurlyRight, this.column, this.line);
		} else {
			this.error();
		}
		throw new RuntimeException("This method should not return a token directly; it must return a 'Token-Expression' instead.");
	}

	public Token isComment() {
		int oldPosition = this.position;
		this.accept('/');
		if (this.has('/')) {
			this.accept('/');
			if (this.has('/')) {
				this.accept('/');
			}
			while (!this.has('\n')) {
				this.consume();
			}
			this.accept('\n');
			return getNextToken();
		}
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.SlashSymbolOperatorToken, this.column, this.line);
	}

	private char peek() {
		return seek(0);
	}

	private char seek(int of) {
		if (this.position + of >= this.target.length) return '\0';
		return this.target[this.position + of];
	}

	private boolean has(char target) {
		return this.peek() == target && this.peek() != '\0';
	}

	private void consume() {
		this.position++;
	}

	private void accept(char target) {
		if (this.has(target)) {
			this.consume();
		} else {
			this.error();
		}
	}

	private void error() {
		throw new RuntimeException("[Hola Scanner] Invalid or Missing Character (Syntax Error): The character '" + this.peek() + "' is not valid at line " + this.line + ", column " + (this.position - this.column) + " for this expression.");
	}
}
