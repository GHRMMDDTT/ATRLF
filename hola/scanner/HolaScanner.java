package hola.scanner;

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
		whitespace();
		if ((this.peek() >= 'a' && this.peek() <= 'z') || (this.peek() >= 'A' && this.peek() <= 'Z')) {
			return identifier();
		} else if (this.has('0') || (this.peek() >= '1' && this.peek() <= '9')) {
			return numeric();
		} else if (this.has('\'')) {
			return character();
		} else if (this.has('=') || this.has('<') || this.has('>')) {
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
		}
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.IdentifierToken, this.column, this.line);
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
		} else if (this.has('b')) {
			this.accept('b');
			do {
				if ((this.peek() >= '0' && this.peek() <= '1')) {
					this.consume();
				} else {
					this.error();
				}
			} while ((this.peek() >= '0' && this.peek() <= '1'));
		} else if (this.has('o')) {
			this.accept('o');
			do {
				if ((this.peek() >= '0' && this.peek() <= '7')) {
					this.consume();
				} else {
					this.error();
				}
			} while ((this.peek() >= '0' && this.peek() <= '7'));
		} else {
			this.error();
		}
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.CompactNumericLiteralToken, this.column, this.line);
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
			} else {
				this.error();
			}
		} else if (this.has('a')) {
			this.accept('a');
		} else {
			this.error();
		}
		this.accept('\'');
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.CharacterLiteralToken, this.column, this.line);
	}

	public Token operator() {
		int oldPosition = this.position;
		if (this.has('=')) {
			this.accept('=');
			if (this.has('=') || this.has('>') || this.has('<')) {
				if (this.has('=')) {
					this.accept('=');
				} else if (this.has('>')) {
					this.accept('>');
				} else if (this.has('<')) {
					this.accept('<');
				} else {
					this.error();
				}
			}
		} else if (this.has('<')) {
			this.accept('<');
		} else if (this.has('>')) {
			this.accept('>');
			if (this.has('=')) {
				this.accept('=');
			}
		} else {
			this.error();
		}
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.OperatorSyntaxToken, this.column, this.line);
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
		throw new RuntimeException("No match in: " + this.peek() + " at " + this.position);
	}
}
