import ATRLFC.Lexer.builder.ClassLexerBuilder;
import ATRLFC.Lexer.parser.ATRLFLexerParser;
import ATRLFC.Lexer.tree.ATRLFLexerTree;
import ATRLFC.tokenizer.ATRLFScanner;

import java.io.File;

public class Main {
	public static void main(String... args) { // Using commands CLI.
		for (String str : args) {
			if (str.endsWith(".atrlf.lic")) {
				ATRLFScanner scanner = new ATRLFScanner(new File(args[0]));
				ATRLFLexerParser parser = new ATRLFLexerParser(scanner);
				ATRLFLexerTree tree = parser.onParser();
				ClassLexerBuilder builder = new ClassLexerBuilder(tree, args[0].substring(0, args[0].lastIndexOf('.') - 6));
				builder.onBuilder();
			}
		}
	}
}