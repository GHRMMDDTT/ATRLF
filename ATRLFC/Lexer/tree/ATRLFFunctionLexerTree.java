package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.stream.Collectors;

public final class ATRLFFunctionLexerTree extends ATRLFExpressionLexerTree {
	public final ATRLFToken name;
	public final ArrayList<ATRLFFunctionParametersLexerTree> parameters;
	public final ATRLFExpressionLexerTree lexerExpressions;
	public final ATRLFToken token;

	public ATRLFFunctionLexerTree(ATRLFToken name, ArrayList<ATRLFFunctionParametersLexerTree> parameters, ATRLFExpressionLexerTree lexerExpressions, ATRLFToken token) {
		this.name = name;
		this.parameters = parameters;
		this.lexerExpressions = lexerExpressions;
		this.token = token;
	}


	@Override
	public String onVisitor() {
		StringBuilder sb = new StringBuilder();
		String name = this.name.value();
		name = name.equals("main") ? "getNextToken" : name;
		sb.append("public " + (token == null ? "void" : "Token") + " ").append(name).append('(');
		sb.append(this.parameters.stream().map((tokens) -> {
			return tokens.type.value() + ' ' + tokens.name.value();
		}).collect(Collectors.joining(", "))).append(')').append(" {\nint oldPosition = this.position;\n");
		sb.append(this.lexerExpressions.onVisitor());
		sb.append(token == null ? "\nreturn;" : "\nreturn new Token(new String(this.input, oldPosition, this.postion - oldPosition), TokenSyntax." + token.value() + ");");
		sb.append("\n}");
		return sb.toString();
	}
	
	public static final class ATRLFFunctionParametersLexerTree {
		public final ATRLFToken name;
		public final ATRLFToken type;


		public ATRLFFunctionParametersLexerTree(ATRLFToken name, ATRLFToken type) {
			this.name = name;
			this.type = type;
		}
	}
}
