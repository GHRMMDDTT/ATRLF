package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.stream.Collectors;

public final class ATRLFFunctionCalledLexerTree extends ATRLFExpressionLexerTree {
	public final ATRLFToken name;
	public final ArrayList<ATRLFToken> parameters;

	public ATRLFFunctionCalledLexerTree(ATRLFToken name, ArrayList<ATRLFToken> parameters) {
		this.name = name;
		this.parameters = parameters;
	}


	@Override
	public String onVisitor(boolean isNot) {
		StringBuilder sb = new StringBuilder();
		String name = this.name.value();
		name = this.name.value().equals("main") ? "getNextToken" : name;
		if (compilationUnit.functions.get(this.name.value()).token != null || compilationUnit.functions.get(this.name.value()).onVisitor(isNot).contains("return") || name.equals("getNextToken")) {
			sb.append("return ");
		}
		sb.append(name).append('(');
		sb.append(')').append(';');
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

		/*sb.append(this.parameters.stream().map((tokens) -> {
			return tokens.value();
		}).collect(Collectors.joining(", ")));*/