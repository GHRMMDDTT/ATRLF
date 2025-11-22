package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public final class ATRLFCompilationUnitLexerTree extends ATRLFLexerTree {
	public ArrayList<ATRLFFunctionLexerTree> parameters;
	public HashMap<String, ATRLFFunctionLexerTree> functions = new HashMap<>();
	public final HashSet<ATRLFToken> tokens = new HashSet<>();
	public ATRLFPackageDeclarationLexerTree packageDeclarationLexerTree;

	public ATRLFCompilationUnitLexerTree(ATRLFPackageDeclarationLexerTree packageDeclarationLexerTree) {
		this.packageDeclarationLexerTree = packageDeclarationLexerTree;
	}

	public void setParameters(ArrayList<ATRLFFunctionLexerTree> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String onVisitor(boolean isNot) {
		this.parameters.forEach(parameters -> {
			if (this.functions.containsKey(parameters.name.value())) {
				System.err.printf(
						"[ATRLF Parser] Semantic Error (Duplicate Declaration): Function '%s' is already defined at line %d, column %d.%n",
						parameters.name.value(),
						this.functions.get(parameters.name.value()).name.line(),
						this.functions.get(parameters.name.value()).name.column()
				);
				System.exit(1);
			}
			this.functions.put(parameters.name.value(), parameters);
		});

		return this.parameters.stream().map(atrlfFunctionLexerTree -> atrlfFunctionLexerTree.onVisitor(isNot)).collect(Collectors.joining("\n\n"));
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
