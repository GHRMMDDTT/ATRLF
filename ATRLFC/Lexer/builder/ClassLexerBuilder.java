package ATRLFC.Lexer.builder;

import ATRLFC.Lexer.tree.ATRLFCompilationUnitLexerTree;
import ATRLFC.Lexer.tree.ATRLFLexerTree;
import ATRLFC.tokenizer.ATRLFToken;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class ClassLexerBuilder {
	private final ATRLFLexerTree tree;
	private final String name;

	public ClassLexerBuilder(ATRLFLexerTree tree, String name) {
		this.tree = tree;
		this.name = name;
	}

	public void onBuilder() {
		String ruteDir = this.name + "/scanner/";
		File carpeta = new File(ruteDir);

		if (!carpeta.exists()) {
			carpeta.mkdirs();
		}

		String code;
		code = "package " + this.name + ".scanner;\n";
		code += """

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

""";
		code += "public class " + Character.toUpperCase(this.name.charAt(0)) + this.name.substring(1) + "Scanner {\n\t" + targetArrayCharacter + "\n\t" + positionInt + "\n\n";
		code += "public "  + Character.toUpperCase(this.name.charAt(0)) + this.name.substring(1) + "Scanner(char[] target) {\nthis.target = target;\n}\n\n";
		code += "public "  + Character.toUpperCase(this.name.charAt(0)) + this.name.substring(1) + "Scanner(String target) {\nthis.target = target.toCharArray();\n}\n\n";
		code += "public "  + Character.toUpperCase(this.name.charAt(0)) + this.name.substring(1) + "Scanner(File target) {\nthis.target = this.readFile(target);\n}\n\n";
		code += readFile$argument_File$Code + "\n";
		code += this.tree.onVisitor() + "\n\n";
		code += peekCode + "\n";
		code += has$argument_char$Code + "\n";
		code += consumeCode + "\n";
		code += accept$argument_char$Code + "\n";
		code += errorCode + "}";
		code = applyIndentation(code);
		try {
			Files.writeString(Path.of(ruteDir + Character.toUpperCase(this.name.charAt(0)) + this.name.substring(1) + "Scanner.java"), code);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		code = "package " + this.name + ".scanner;\n\n" + File$Token_class + ((ATRLFCompilationUnitLexerTree) tree).tokens.stream().map(ATRLFToken::value).collect(Collectors.joining(",\n")) + ",\nBadToken;\n}\n}";
		code = applyIndentation(code);
		try {
			Files.writeString(Path.of(ruteDir + "Token.java"), code);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String applyIndentation(String lines) {
		StringBuilder result = new StringBuilder();
		int indentLevel = 0;

		for (String line : lines.split("\n")) {
			String trimmed = line.trim();

			if (trimmed.isEmpty()) {
				result.append("\n");
				continue;
			}

			if (trimmed.startsWith("}")) {
				indentLevel = Math.max(0, indentLevel - 1);
			}

			result.append("\t".repeat(indentLevel))
					.append(trimmed)
					.append("\n");

			if (trimmed.endsWith("{")) {
				indentLevel++;
			}
		}

		return result.toString();
	}

	public static final String targetArrayCharacter = "private final char[] target;";
	public static final String positionInt = "private int position;";

	public static final String peekCode = """
private char peek() {
if (this.position >= this.target.length) return '\\0';
return this.target[this.position];
}
""";

	public static final String has$argument_char$Code = """
private boolean has(char target) {
return this.peek() == target && this.peek() != '\\0';
}
""";

	public static final String consumeCode = """
private void consume() {
this.position++;
}
""";

	public static final String accept$argument_char$Code = """
private void accept(char target) {
if (this.has(target)) {
this.consume();
} else {
this.error();
}
}
""";

	public static final String errorCode = """
private void error() {
throw new RuntimeException("No match in: " + this.peek() + " at " + this.position);
}
""";

	public static final String readFile$argument_File$Code = """
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
""";

	public static final String File$Token_class = """
public class Token {
private final String value;
private final TokenSyntax type;

public Token(String value, TokenSyntax type) {
this.value = value;
this.type = type;
}

public enum TokenSyntax {
""";
}
