package ATRLFC.Lexer.tree;

import ATRLFC.tokenizer.ATRLFToken;

import java.util.ArrayList;
import java.util.stream.Collectors;

public final class ATRLFFunctionLexerTree extends ATRLFExpressionLexerTree {
	public final ATRLFToken name;
	public final ArrayList<ATRLFFunctionParametersLexerTree> parameters;
	public final ATRLFExpressionLexerTree lexerExpressions;
	public final ATRLFFunctionReturn token;

	public ATRLFFunctionLexerTree(ATRLFToken name, ArrayList<ATRLFFunctionParametersLexerTree> parameters, ATRLFExpressionLexerTree lexerExpressions, ATRLFFunctionReturn token) {
		this.name = name;
		this.parameters = parameters;
		this.lexerExpressions = lexerExpressions;
		this.token = token;
	}

	@Override
	public String onVisitor() {
		if (this.token != null) {
			if (this.token instanceof ATRLFFunctionSwitchCaseReturn switchCaseReturn) {
				for (ATRLFFunctionSwitchCaseReturn.ATRLFFunctionCaseReturn caseReturn : switchCaseReturn.cases) {
					this.compilationUnit.tokens.add(caseReturn.returnToken);
				}
				this.compilationUnit.tokens.add(switchCaseReturn.defaults.token);
			} else if (this.token instanceof ATRLFFunctionSingleReturn singleReturn) {
				this.compilationUnit.tokens.add(singleReturn.token);
			}
		}
		StringBuilder sb = new StringBuilder();
		String name = this.name.value();
		name = name.equals("main") ? "getNextToken" : name;
		sb.append("public void").append(" ").append(name).append('(');
		sb.append(this.parameters.stream().map((tokens) -> tokens.type.value() + ' ' + tokens.name.value()).collect(Collectors.joining(", "))).append(')').append(" {\nint oldPosition = this.position;\n");
		if (name.equals("getNextToken")) {
			sb.append("if (this.position >= this.target.length) {\nreturn this.EOIF;\n}\n");
		}
		sb.append(this.lexerExpressions.onVisitor());
		if (sb.toString().contains("return") || this.token != null) {
			sb.replace(7, 11, "Token");
			if (!(this.lexerExpressions instanceof ATRLFUnaryExpressionLexerTree unaryExpressionLexerTree && unaryExpressionLexerTree.expresion instanceof ATRLFTokenExpressionLexerTree)) {
				if (name.equals("getNextToken")) {
					sb.append("\nreturn new Token(String.valueOf(this.peek()), Token.TokenSyntax.BadToken, this.column, this.line);");
				} else if (this.token != null) {
					if (this.token instanceof ATRLFFunctionSwitchCaseReturn switchCaseReturn) {
						sb.append("String value = new String(this.target, oldPosition, this.position - oldPosition);\nswitch (value) {\n");
						for (ATRLFFunctionSwitchCaseReturn.ATRLFFunctionCaseReturn caseReturn : switchCaseReturn.cases) {
							sb.append("case ").append(caseReturn.value.value()).append(": {\nreturn new Token(value, Token.TokenSyntax.").append(caseReturn.returnToken.value()).append(", this.column, this.line);\n}\n");
						}
						sb.append("default: {\nreturn new Token(value, Token.TokenSyntax.").append(switchCaseReturn.defaults.token.value()).append(", this.column, this.line);\n}\n");
						sb.append('}');
					} else if (this.token instanceof ATRLFFunctionSingleReturn singleReturn) {
						sb.append("\nreturn new Token(new String(this.target, oldPosition, this.position - oldPosition), Token.TokenSyntax.").append(singleReturn.token.value()).append(", this.column, this.line);");
					}
				} else {
					sb.append("\nthrow new RuntimeException(\"This method should not return a token directly; it must return a 'Token-Expression' instead.\");");
				}
			}
		}
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

	public static class ATRLFFunctionReturn { }

	public static final class ATRLFFunctionSwitchCaseReturn extends ATRLFFunctionReturn {
		public final ArrayList<ATRLFFunctionCaseReturn> cases;
		public final ATRLFFunctionSingleReturn defaults;

		public ATRLFFunctionSwitchCaseReturn(ArrayList<ATRLFFunctionCaseReturn> cases, ATRLFFunctionSingleReturn defaults) {
			this.cases = cases;
			this.defaults = defaults;
		}

		public static final class ATRLFFunctionCaseReturn {
			public final ATRLFToken value;
			public final ATRLFToken returnToken;


			public ATRLFFunctionCaseReturn(ATRLFToken value, ATRLFToken returnToken) {
				this.value = value;
				this.returnToken = returnToken;
			}
		}
	}

	public static final class ATRLFFunctionSingleReturn extends ATRLFFunctionReturn {
		private final ATRLFToken token;

		public ATRLFFunctionSingleReturn(ATRLFToken token) { this.token = token; }
	}
}
