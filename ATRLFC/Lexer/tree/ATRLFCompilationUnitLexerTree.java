package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public final class ATRLFCompilationUnitLexerTree extends ATRLFLexerTree {
	public ArrayList<ATRLFFunctionLexerTree> parameters;
	public HashMap<String, ATRLFFunctionLexerTree> functions = new HashMap<>();
	public final ArrayList<ATRLFToken> tokens = new ArrayList<>();

	public ATRLFCompilationUnitLexerTree() { }

	public void setParameters(ArrayList<ATRLFFunctionLexerTree> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String onVisitor() {
		this.parameters.forEach(parameters -> {
			if (this.functions.containsKey(parameters.name.value())) {
				throw new RuntimeException("Duplicate function name (" + parameters.name.value() + ") is exist in: " + this.functions.get(parameters.name.value()).token.line() + ":" + this.functions.get(parameters.name.value()).token.column());
			}
			this.functions.put(parameters.name.value(), parameters);
		});

		return this.parameters.stream().map(ATRLFFunctionLexerTree::onVisitor).collect(Collectors.joining("\n\n"));
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
