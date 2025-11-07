import ATRLFC.Lexer.parser.ATRLFLexerParser;
import ATRLFC.Lexer.tree.ATRLFLexerTree;
import ATRLFC.tokenizer.ATRLFScanner;

import java.io.File;

public class Main {
	private static final char[] input = "asdasd".toCharArray();
	private static int position;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
	public static void main(String... args) {
		ATRLFScanner scanner = new ATRLFScanner(new File("main.atrlf"));
		ATRLFLexerParser parser = new ATRLFLexerParser(scanner);
		ATRLFLexerTree tree = parser.onParser();
		System.out.println(tree.onVisitor());
	}

	public Token identifier() {
		int oldPosition = this.position;
		letter();
		while (has('a') || has('z') || has('A') || has('Z') || has('0') || has('9')) {
			if ((this.peek() >= 'a' && this.peek() <= 'z') || (this.peek() >= 'A' && this.peek() <= 'Z')) {
				letter();
			} else if ((this.peek() >= '0' && this.peek() <= '9')) {
				digit();
			}
		}
		return new Token(new String(this.input, oldPosition, this.position - oldPosition), TokenSyntax.IdentifierTokenSyntax);
	}

	public void letter() {
		int oldPosition = this.position;
		if ((this.peek() >= 'a' && this.peek() <= 'z')) {
			this.consume();
		} else if ((this.peek() >= 'A' && this.peek() <= 'Z')) {
			this.consume();
		} else {
			this.error();
		}
		return;
	}

	public void digit() {
		int oldPosition = this.position;
		if ((this.peek() >= '0' && this.peek() <= '9')) {
			this.consume();
		} else {
			this.error();
		}
		return;
	}

	private static char peek() {
		if (position >= input.length) return '\0';
		return input[position];
	}

	private static final boolean has(char target) {
		return peek() == target && peek() != '\0';
	}

	private static final void consume() {
		position++;
	}

	private static final void accept(char target) {
		if (has(target)) {
			consume();
			return;
		}
		error();
	}

	private static final void error() {
		throw new RuntimeException("No match in: " + peek() + " at " + position + ':' + 0);
	}
}