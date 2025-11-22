package example.main.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MainScanner {
	private final char[] target;
	private int position;

	private int line;
	private int column;

	public MainScanner(char[] target) {
		this.target = target;
	}

	public MainScanner(String target) {
		this.target = target.toCharArray();
	}

	public MainScanner(File target) {
		this.target = this.readFile(target);
	}

	private final Token EOIF = new Token("\0", Token.TokenSyntax.EndOfInputFileToken, this.column + 1, this.line);

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
		if ((this.peek() >= '1' && this.peek() <= '9')) {
			return number();
		} else if (this.has('+')) {
			return operator();
		} else {
			this.error();
		}
		return new Token(String.valueOf(this.peek()), Token.TokenSyntax.BadToken, this.column, this.line);
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

	public Token number() {
		int oldPosition = this.position;
		if ((this.peek() >= '1' && this.peek() <= '9')) {
			this.consume();
		} else {
			this.error();
		}
		while ((this.peek() >= '0' && this.peek() <= '9')) {
			this.consume();
		}
		if (this.has('b') || this.has('B') || this.has('s') || this.has('S') || this.has('i') || this.has('I') || this.has('l') || this.has('L') || this.has('n') || this.has('N') || this.has('f') || this.has('F') || this.has('d') || this.has('D')) {
			return suffixNumericInteger();
		} else if (this.has('.')) {
			this.accept('.');
			while ((this.peek() >= '0' && this.peek() <= '9')) {
				this.consume();
			}
			if (this.has('n') || this.has('N') || this.has('f') || this.has('F') || this.has('d') || this.has('D')) {
				return suffixNumericDecimal();
			}
		} else {
			this.error();
		}
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.DigitLiteralToken, this.column, this.line);
	}

	public Token suffixNumericInteger() {
		int oldPosition = this.position;
		if (this.has('b') || this.has('B')) {
			if (this.has('b')) {
				this.accept('b');
			} else if (this.has('B')) {
				this.accept('B');
			} else {
				this.error();
			}
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.ByteLiteralToken, this.column, this.line);
		} else if (this.has('s') || this.has('S')) {
			if (this.has('s')) {
				this.accept('s');
			} else if (this.has('S')) {
				this.accept('S');
			} else {
				this.error();
			}
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.ShortLiteralToken, this.column, this.line);
		} else if (this.has('i') || this.has('I')) {
			if (this.has('i')) {
				this.accept('i');
			} else if (this.has('I')) {
				this.accept('I');
			} else {
				this.error();
			}
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.IntegerLiteralToken, this.column, this.line);
		} else if (this.has('l') || this.has('L')) {
			if (this.has('l')) {
				this.accept('l');
			} else if (this.has('L')) {
				this.accept('L');
			} else {
				this.error();
			}
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.LongLiteralToken, this.column, this.line);
		} else if (this.has('n') || this.has('N') || this.has('f') || this.has('F') || this.has('d') || this.has('D')) {
			return suffixNumericDecimal();
		} else {
			this.error();
		}
		throw new RuntimeException("This method should not return a token directly; it must return a 'Token-Expression' instead.");
	}

	public Token suffixNumericDecimal() {
		int oldPosition = this.position;
		if (this.has('n') || this.has('N')) {
			if (this.has('n')) {
				this.accept('n');
			} else if (this.has('N')) {
				this.accept('N');
			} else {
				this.error();
			}
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.NumberLiteralToken, this.column, this.line);
		} else if (this.has('f') || this.has('F')) {
			if (this.has('f')) {
				this.accept('f');
			} else if (this.has('F')) {
				this.accept('F');
			} else {
				this.error();
			}
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.FloatLiteralToken, this.column, this.line);
		} else if (this.has('d') || this.has('D')) {
			if (this.has('d')) {
				this.accept('d');
			} else if (this.has('D')) {
				this.accept('D');
			} else {
				this.error();
			}
			return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.DoubleLiteralToken, this.column, this.line);
		} else {
			this.error();
		}
		throw new RuntimeException("This method should not return a token directly; it must return a 'Token-Expression' instead.");
	}

	public Token operator() {
		int oldPosition = this.position;
		this.accept('+');
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.PlusOperatorSymbolToken, this.column, this.line);
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
		throw new RuntimeException("[Main Scanner] Invalid or Missing Character (Syntax Error): The character '" + this.peek() + "' is not valid at line " + this.line + ", column " + (this.position - this.column) + " for this expression.");
	}
}
