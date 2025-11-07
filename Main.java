import ATRLFC.parser.ATRLFParser;
import ATRLFC.tokenizer.ATRLFScanner;
import ATRLFC.tree.ATRLFTree;

import java.io.File;

private final static String FILE_EXTENSION = ".atrlf";
private final static String FILE_EXTENSION_LEXER = ".lic";
private final static String FILE_EXTENSION_PARSER = ".pic";
private final static String FILE_EXTENSION_INTERPRETER = ".iic";
private final static String FILE_EXTENSION_TREE = ".tic";

private final char[] target = "tmp".toCharArray();
private int position;

void main() {
	ATRLFScanner scanner = new ATRLFScanner(new File("main" + FILE_EXTENSION));
	ATRLFParser parser = new ATRLFParser(scanner);
	ATRLFTree tree = parser.onParser();
	System.out.println(tree.onVisitor());
}

private char peek() {
	if (this.position >= this.target.length) return '\0';
	return this.target[this.position];
}

private final boolean has(char target) {
	return this.peek() == target && this.peek() != '\0';
}

private final void consume() {
	this.position++;
}

private final void accept(char target) {
	if (this.has(target)) {
		this.consume();
		return;
	}
	this.error();
}

private final void error() {
	throw new RuntimeException("No match in: " + this.peek() + " at " + this.position + ':' + 0);
}