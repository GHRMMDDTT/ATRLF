import ATRLFC.Lexer.builder.ClassLexerBuilder;
import ATRLFC.Lexer.parser.ATRLFLexerParser;
import ATRLFC.Lexer.tree.ATRLFLexerTree;
import ATRLFC.tokenizer.ATRLFScanner;

import java.io.File;

public class Main {
	public static void main(String... args) {
		ATRLFScanner scanner = new ATRLFScanner(new File("main.atrlf"));
		ATRLFLexerParser parser = new ATRLFLexerParser(scanner);
		ATRLFLexerTree tree = parser.onParser();
		ClassLexerBuilder builder = new ClassLexerBuilder(tree, "main");
		builder.onBuilder();
	}
}