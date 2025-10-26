import ATRLFC.interpreter.ATRLFInterpreter;
import ATRLFC.tokenizer.ATRLFScanner;

import java.io.File;

private int position = 0;
private final char[] target = {
		'0', 'o', '7'
};

void main() {
	ATRLFScanner scanner = new ATRLFScanner(new File("main.atrlf"));
	ATRLFInterpreter interpreter = new ATRLFInterpreter(scanner);
	System.out.println(interpreter.onInterpreter());
}

private char peek() {
	if (this.position >= this.target.length) return '\0';
	return this.target[this.position];
}

private final boolean has(char target) {
	return peek() == target || peek() != '\0';
}

private final void consume() {
	this.position++;
}

private final void accept(char target) {
	if (has(target)) {
		consume();
		return;
	}
	error(target);
}

private final void error(char target) {
	if (this.peek() == '\0') return;
	throw new RuntimeException("No matched in: " + target + " with: " + this.peek());
}

private final void error() {
	if (this.peek() == '\0') return;
	throw new RuntimeException("No matched in: " + this.peek());
}