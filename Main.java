import ATRLFC.Lexer.builder.ClassLexerBuilder;
import ATRLFC.Lexer.parser.ATRLFLexerParser;
import ATRLFC.Lexer.tree.ATRLFCompilationUnitLexerTree;
import ATRLFC.tokenizer.ATRLFScanner;

import java.io.File;
import java.nio.file.Path;

public class Main {
	public static void main(String... args) { // Using commands CLI.
		for (String str : args) {
			String name = str.substring(0, str.lastIndexOf('.') - 6);
			if (!new File(str).exists()) {
				System.err.println("[Cappuccino Main] File Not Found (CLI Error): The fle: " + name.substring(name.lastIndexOf('/') + 1) + " no exist!");
				System.exit(1);
			}

			if (str.endsWith(".atrlf.lic")) {
				ATRLFScanner scanner = new ATRLFScanner(new File(str));
				ATRLFLexerParser parser = new ATRLFLexerParser(scanner);
				ATRLFCompilationUnitLexerTree tree = (ATRLFCompilationUnitLexerTree) parser.onParser();
				ClassLexerBuilder builder = new ClassLexerBuilder(tree, name);
				builder.onBuilder();
			}
		}
	}
}