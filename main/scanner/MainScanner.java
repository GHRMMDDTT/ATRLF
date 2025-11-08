package main.scanner;

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

	public MainScanner(char[] target) {
		this.target = target;
	}

	public MainScanner(String target) {
		this.target = target.toCharArray();
	}

	public MainScanner(File target) {
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
		}
		return new Token(String.valueOf(this.peek()), Token.TokenSyntax.BadToken);
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
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.IdentifierToken);
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
		}
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.NumericLiteralToken);
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
		}
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.CompactNumericLiteralToken);
	}

	public void whitespace() {
		int oldPosition = this.position;
		while (has(' ') || has('\t') || has('\r') || has('\n')) {
			if (this.has(' ')) {
				this.accept(' ');
			} else if (this.has('\t')) {
				this.accept('\t');
			} else if (this.has('\r')) {
				this.accept('\r');
			} else if (this.has('\n')) {
				this.accept('\n');
			}
		}
		return;
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
			}
		} else if (this.has('a')) {
			this.accept('a');
		}
		this.accept('\'');
		return new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.CharacterLiteralToken);
	}

	private char peek() {
		if (this.position >= this.target.length) return '\0';
		return this.target[this.position];
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
