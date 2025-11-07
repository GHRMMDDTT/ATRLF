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
	public String onVisitor() {
		StringBuilder sb = new StringBuilder();
		String name = this.name.value();
		name = name.equals("main") ? "getNextToken" : name;
		sb.append(name).append('(');
		/*sb.append(this.parameters.stream().map((tokens) -> {
			return tokens.value();
		}).collect(Collectors.joining(", ")));*/
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
