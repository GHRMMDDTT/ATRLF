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

		String name = Character.toUpperCase(this.name.charAt(this.name.lastIndexOf('/') + 1)) + this.name.substring(this.name.lastIndexOf('/') + 1 + 1);
		String packager = this.name.substring(this.name.lastIndexOf('/') + 1);

		String code;
		code = "package " + packager + ".scanner;\n";
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
		code += "public class " + name + "Scanner {\n\t" + targetArrayCharacter + "\n\t" + positionInt + "\n\n" + "\t" + lineInt + "\n\t" + columnInt + "\n\n";
		code += "public "  + name + "Scanner(char[] target) {\nthis.target = target;\n}\n\n";
		code += "public "  + name + "Scanner(String target) {\nthis.target = target.toCharArray();\n}\n\n";
		code += "public "  + name + "Scanner(File target) {\nthis.target = this.readFile(target);\n}\n\n";
		code += readFile$argument_File$Code + "\n";
		code += this.tree.onVisitor() + "\n\n";
		code += peekCode + "\n";
		code += seekCode$argument_int$Code + "\n";
		code += has$argument_char$Code + "\n";
		code += consumeCode + "\n";
		code += accept$argument_char$Code + "\n";
		code += errorCode(name) + "}";
		code = applyIndentation(code);
		try {
			Files.writeString(Path.of(ruteDir + name  + "Scanner.java"), code);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		code = "package " + packager + ".scanner;\n\n" + File$Token_class + ((ATRLFCompilationUnitLexerTree) tree).tokens.stream().map(ATRLFToken::value).collect(Collectors.joining(",\n")) + ",\nBadToken;\n}\n}";
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
	public static final String columnInt = "private int column;";
	public static final String lineInt = "private int line;";

	public static final String peekCode = """
private char peek() {
return seek(0);
}
""";

	public static final String seekCode$argument_int$Code = """
private char seek(int of) {
if (this.position + of >= this.target.length) return '\\0';
return this.target[this.position + of];
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

	public static final String errorCode(String name) {
		return "private void error() {\nthrow new RuntimeException(\"[" + name + " Scanner] Invalid or Missing Character (Syntax Error): The character '\" + this.peek() + \"' is not valid at line \" + this.line + \", column \" + (this.position - this.column) + \" for this expression.\");\n}\n";
	}

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
private final int column;
private final int line;

public Token(String value, TokenSyntax type, int column, int line) {
this.value = value;
this.type = type;
this.column = column;
this.line = line;
}

public enum TokenSyntax {
""";
}
