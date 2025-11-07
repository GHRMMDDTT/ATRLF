package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public final class ATRLFCompilationUnitLexerTree extends ATRLFLexerTree {
	public ArrayList<ATRLFFunctionLexerTree> parameters;
	public HashMap<String, ATRLFFunctionLexerTree> functions = new HashMap<>();

	public ATRLFCompilationUnitLexerTree() { }

	public void setParameters(ArrayList<ATRLFFunctionLexerTree> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String onVisitor() {
		this.parameters.forEach(parameters -> {
			functions.put(parameters.name.value(), parameters);
		});

		return this.parameters.stream().map((tokens) -> {
			return tokens.onVisitor();
		}).collect(Collectors.joining("\n\n"));
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
