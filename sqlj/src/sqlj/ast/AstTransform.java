package sqlj.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import sqlj.core.ParsedSource;
import sqlj.core.ResultSetIterator;
import sqlj.core.ResultSetUtil;
import sqlj.core.SqljBlock;
import sqlj.exception.ParserException;
import sqlj.exception.TransformException;
import sqlj.parser.Parameter;
import sqlj.parser.ParsedSql;
import sqlj.parser.SqlPosition;

public class AstTransform {
	public static final String METHOD_GET = "get";
	public static final String METHOD_TO_STRING = "toString";
	public static final String METHOD_APPEND = "append";
	public static final String METHOD_GET_CONNECTION = "getConnection";
	public static final String UPDATE_COUNT = "UPDATE_COUNT";
	public static final String METHOD_PREPARE_CALL = "prepareCall";
	public static final String METHOD_REGISTER_OUT_PARAMETER = "registerOutParameter";
	public static final String METHOD_PREPARE_STATEMENT = "prepareStatement";
	public static final String METHOD_EXECUTE = "execute";
	public static final String METHOD_GET_UPDATE_COUNT = "getUpdateCount";
	public static final String METHOD_GET_RESULT_SET = "getResultSet";
	public static final int API_LEVEL = AST.JLS3;

	private ParsedSource parsedSource;

	public AstTransform(ParsedSource parsedSource) {
		super();
		this.parsedSource = parsedSource;
	}

	public String tranform() throws Exception {
		CompilationUnit result = createCompilationUnit();
		Document doc = new Document(parsedSource.getBuffer().toString());
		TextEdit edits = result.rewrite(doc, null);
		edits.apply(doc);
		String sourceCode = doc.get();
		return sourceCode;
	}

	/**
	 * the final java source will be set into <i>doc<i>
	 * 
	 * @param doc
	 *            the document that contains the parsed original source
	 * @throws Exception
	 */
	public void transform(IDocument doc) throws Exception {
		CompilationUnit result = createCompilationUnit();
		TextEdit edits = result.rewrite(doc, null);
		edits.apply(doc);
	}

	private CompilationUnit createCompilationUnit() throws Exception {
		StringBuilder strBuffer = parsedSource.getBuffer();
		//System.out.println(strBuffer);
		char[] cs = new char[strBuffer.length()];
		strBuffer.getChars(0, cs.length, cs, 0);
		ASTParser parser = ASTParser.newParser(API_LEVEL);
		parser.setSource(cs);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		long t0 = System.currentTimeMillis();
		CompilationUnit result = (CompilationUnit) parser.createAST(null);
		if (result.getProblems().length == 0) {
			result.recordModifications();
			List<TypeDeclaration> types = result.types();
			for (TypeDeclaration td : types) {
				transform(td);
			}
		} else {
			//System.out.println(strBuffer);
			for (IProblem p : result.getProblems()) {
				int dx = translateToAbs(p.getSourceStart())
						- p.getSourceStart();
				p.setSourceStart(p.getSourceStart() + dx);
				p.setSourceEnd(p.getSourceEnd() + dx);
			}
			throw new TransformException(result.getProblems());
		}
		return result;
	}

	private int translateToAbs(int idx) {
		int size = parsedSource.getSqljBlockSize();
		int trx = 0;
		SqljBlock lastSqljBlock = null;
		for (int i = 0; i < size; i++) {
			SqljBlock b = parsedSource.getSqlById(i);
			if (lastSqljBlock == null) {
				trx = b.getStartIdx();
				if (trx > idx)
					return idx;
			} else {
				trx += lastSqljBlock.getReplaceLength();
				trx += (b.getStartIdx() - lastSqljBlock.getBodyEndIdx() - 1);
				if (trx > idx)
					return b.getStartIdx() - (trx - idx);
			}
			lastSqljBlock = b;
		}
		if (lastSqljBlock == null)
			return idx;
		trx += lastSqljBlock.getReplaceLength();
		return lastSqljBlock.getBodyEndIdx() + (idx - trx);
	}

	public void compile2Class() throws Exception {
		CompilationUnit unit = createCompilationUnit();
		org.eclipse.jdt.internal.compiler.Compiler compiler = new org.eclipse.jdt.internal.compiler.Compiler(
				new NameEnvironmentImpl(unit),
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				new HashMap(), new CompilerRequestorImpl(),
				new DefaultProblemFactory(Locale.getDefault()));
		compiler.compile(new ICompilationUnit[] { new CompilationUnitImpl(unit) });
	}

	private void transform(TypeDeclaration typeDec) throws Exception {
		final ArrayList<MethodInvocation> list = new ArrayList<MethodInvocation>();
		typeDec.accept(new ASTVisitor() {

			@Override
			public boolean visit(VariableDeclarationStatement node) {
				List<VariableDeclarationFragment> list = node.fragments();
				for (VariableDeclarationFragment vdf : list) {
					parsedSource.registerVariableType(vdf.getName()
							.getIdentifier(), node.getType().toString());
				}
				return super.visit(node);
			}

			public boolean visit(SingleVariableDeclaration svd) {
				parsedSource
						.registerVariableType(svd.getName().getIdentifier(),
								svd.getType().toString());
				return super.visit(svd);
			}

			@Override
			public boolean visit(FieldDeclaration fd) {
				String type = fd.getType().toString();
				List<VariableDeclarationFragment> list = fd.fragments();
				for (VariableDeclarationFragment vdf : list) {
					parsedSource.registerVariableType(vdf.getName()
							.getIdentifier(), type);
				}
				return false;
			}

			@Override
			public boolean visit(MethodInvocation node) {

				if (node.getName().getIdentifier()
						.equals(SqlPosition.METHOD_SQL_EXECUTE)) {
					list.add(node);
				}
				return super.visit(node);
			}

		});
		methodReplace(list);
	}

	private Statement findStatement(ASTNode n) {
		ASTNode p = n.getParent();
		if (p instanceof Statement)
			return (Statement) p;
		return findStatement(p);
	}

	private void methodReplace(List<MethodInvocation> list) throws Exception {
		for (MethodInvocation mi : list) {
			String rs_id = mi.arguments().get(1).toString();
			Statement s = findStatement(mi);
			List<ASTNode> stmts = (List<ASTNode>) s.getParent()
					.getStructuralProperty(s.getLocationInParent());
			int index = stmts.indexOf(s);
			ArrayList<Statement> gene_stmts = generate__sqlj_execute(mi);
			stmts.addAll(index, gene_stmts);
			AST ast = mi.getAST();
			if (s instanceof EnhancedForStatement) {
				EnhancedForStatement efs = (EnhancedForStatement) s;
				Expression newExp = createNewResultSetIteratorExpression(ast,
						rs_id, efs.getParameter().getType().toString());
				efs.setExpression(newExp);
				if (efs.getParameter().getType().toString()
						.equals(Map.class.getSimpleName())) {
					updateReferenceInFor(efs);
				}
			} else /*if (s instanceof ExpressionStatement)*/{
				ASTNode astn = mi.getParent();
				if (astn instanceof MethodInvocation) {
					//as para
					MethodInvocation omi = (MethodInvocation) astn;
					List<Expression> argsList = omi.arguments();
					int idx = argsList.indexOf(mi);
					argsList.set(idx, ast.newSimpleName(rs_id));
				} else {
					stmts.set(index + gene_stmts.size(),
							ast.newEmptyStatement());
				}
			}
		}
	}

	void updateReferenceInFor(EnhancedForStatement efs) {
		SingleVariableDeclaration svd = efs.getParameter();
		final String ref = svd.getName().getIdentifier();
		final HashMap<String, String> columnTypeCache = new HashMap<String, String>();
		efs.getBody().accept(new ASTVisitor() {

			@Override
			public boolean visit(QualifiedName node) {
				if (node.getQualifier().getFullyQualifiedName().equals(ref)) {
					AST ast = node.getAST();
					ASTNode parent = node.getParent();
					StructuralPropertyDescriptor loc = node
							.getLocationInParent();
					String name = node.getName().getIdentifier();
					String type = columnTypeCache.get(name);
					if (type == null) {
						type = guessType(parent);
						columnTypeCache.put(name, type);
					}
					Expression newExp = newMethodInvocation(ast,
							ast.newSimpleName(ResultSetUtil.class
									.getSimpleName()), METHOD_GET, ast
									.newSimpleName(ref),
							newStringLiteral(ast, name),
							newTypeLiteral(ast, type));
					if (loc.isChildProperty()) {
						parent.setStructuralProperty(loc, newExp);
					} else if (loc.isChildListProperty()) {
						List<ASTNode> childList = (List<ASTNode>) parent
								.getStructuralProperty(loc);
						int idx = childList.indexOf(node);
						childList.set(idx, newExp);
					} else
						System.out.println(loc);

				}
				return false;
			}

			@Override
			public boolean visit(VariableDeclarationStatement node) {
				List<VariableDeclarationFragment> list = node.fragments();
				for (VariableDeclarationFragment vdf : list) {
					parsedSource.registerVariableType(vdf.getName().toString(),
							node.getType().toString());
				}
				return super.visit(node);
			}

			public boolean visit(MethodInvocation mi) {
				//update reference in sub sql execution
				if (mi.getName().getIdentifier()
						.equals(SqlPosition.METHOD_SQL_EXECUTE)) {
					int sqlId = Integer.parseInt(mi.arguments().get(0)
							.toString());
					SqljBlock sqljb = parsedSource.getSqlById(sqlId);
					try {
						ParsedSql psql = sqljb.getParsedSql();
						for (Parameter p : psql.getBindParameters()) {
							Expression exp = parseExpression(mi.getAST(),
									p.getExpression());
							if (exp instanceof QualifiedName) {
								QualifiedName qn = (QualifiedName) exp;
								if (qn.getQualifier().getFullyQualifiedName()
										.equals(ref)) {
									p.setExpression(String.format(
											"%s.get(\"%s\")", ref, qn.getName()
													.getIdentifier()));
								}
							}
						}
					} catch (ParserException e) {
						e.printStackTrace();
					}
				}
				return true;
			}
		});
	}

	String guessType(ASTNode parent) {
		while (!(parent instanceof Statement)) {
			if (parent instanceof Assignment) {
				Assignment assi = (Assignment) parent;
				Expression leftHandSide = assi.getLeftHandSide();
				if (!(leftHandSide instanceof SimpleName))
					continue;
				String var = ((SimpleName) leftHandSide).getIdentifier();
				String type = parsedSource.getOriginalVariableType(var);
				if (type != null)
					return type;
				break;
			} else if (parent instanceof VariableDeclarationFragment) {
				VariableDeclarationStatement vds = (VariableDeclarationStatement) parent
						.getParent();
				Type type = vds.getType();
				while (type.isArrayType()) {
					ArrayType atype = (ArrayType) type;
					type = atype.getComponentType();
				}
				return type.toString();
			} else if (parent instanceof MethodInvocation) {
				break;
			}
			parent = parent.getParent();
		}
		return Object.class.getSimpleName();
	}

	ArrayList<Statement> generate__sqlj_execute(MethodInvocation mi)
			throws ParserException {
		List<Expression> params = mi.arguments();
		int sqlid = Integer.parseInt(params.get(0).toString());
		String stmt_name = parsedSource.genId("ps");
		AST ast = mi.getAST();
		SqljBlock sqljblock = parsedSource.getSqlById(sqlid);
		ParsedSql parsedSql = sqljblock.getParsedSql();
		String stmt_type = java.sql.PreparedStatement.class.getSimpleName();
		String prepare_method = METHOD_PREPARE_STATEMENT;
		if (parsedSql.hasOutputParameter()) {
			stmt_type = java.sql.CallableStatement.class.getSimpleName();
			prepare_method = METHOD_PREPARE_CALL;
		}
		ArrayList<Statement> generated_statements = new ArrayList<Statement>();
		Expression sqlExpression = createSqlLiteralStatements(ast, parsedSql,
				generated_statements);
		VariableDeclarationStatement vds = newVariableDeclarationStatement(
				ast,
				stmt_type,
				newVariableDeclarationFragment(
						ast,
						stmt_name,
						newMethodInvocation(
								ast,
								newMethodInvocation(ast, null,
										METHOD_GET_CONNECTION), prepare_method,
								sqlExpression)));
		generated_statements.add(vds);
		//bind parameters
		performParameterBinding(ast, parsedSql, generated_statements, stmt_name);
		//execute
		MethodInvocation mi2 = newMethodInvocation(ast,
				ast.newSimpleName(stmt_name), METHOD_EXECUTE);
		generated_statements.add(ast.newExpressionStatement(mi2));
		//set UPDATE_COUNT flag
		Assignment assi = ast.newAssignment();
		assi.setLeftHandSide(ast.newSimpleName(UPDATE_COUNT));
		assi.setRightHandSide(newMethodInvocation(ast,
				ast.newSimpleName(stmt_name), METHOD_GET_UPDATE_COUNT));
		generated_statements.add(ast.newExpressionStatement(assi));
		//fetch output parameters
		performParameterFetching(ast, parsedSql, generated_statements,
				stmt_name);
		//set resultset
		VariableDeclarationStatement rs_vds = newVariableDeclarationStatement(
				ast,
				java.sql.ResultSet.class.getSimpleName(),
				newVariableDeclarationFragment(
						ast,
						params.get(1).toString(),
						newMethodInvocation(ast, ast.newSimpleName(stmt_name),
								METHOD_GET_RESULT_SET)));
		generated_statements.add(rs_vds);
		return generated_statements;
	}

	/**
	 * expression stands for <i>new
	 * ResultSetIterator&lt;typeName&gt;(rs_id,typeName.class)<i>
	 * 
	 * @param ast
	 * @param rs_id
	 * @param typeName
	 * @return
	 */

	Expression createNewResultSetIteratorExpression(AST ast, String rs_id,
			String typeName) {
		TypeLiteral fa = newTypeLiteral(ast, typeName);
		ClassInstanceCreation cic = ast.newClassInstanceCreation();
		ParameterizedType pt = ast.newParameterizedType(ast.newSimpleType(ast
				.newSimpleName(ResultSetIterator.class.getSimpleName())));
		pt.typeArguments().add(ast.newSimpleType(ast.newName(typeName)));
		cic.setType(pt);
		cic.arguments().add(ast.newSimpleName(rs_id));
		cic.arguments().add(fa);
		return cic;
	}

	/**
	 * expression stands for the sql literal<br> StringLiteral or
	 * StringBuilder.toString() (if the sql is dynamic)<br> extra statements
	 * will add to <i>list<i>
	 * 
	 * @param ast
	 * @param psql
	 * @param list
	 * @param parsedSource
	 * @return
	 */
	Expression createSqlLiteralStatements(AST ast, ParsedSql psql,
			ArrayList<Statement> list) {
		if (!psql.isDynamic()) {
			return newStringLiteral(ast, psql.getFirstFragment());
		}
		String name = parsedSource.genId("sql");
		VariableDeclarationStatement vds_sb = newVariableDeclarationStatement(
				ast,
				StringBuilder.class.getSimpleName(),
				newVariableDeclarationFragment(
						ast,
						name,
						newClassInstance(ast,
								StringBuilder.class.getSimpleName())));
		list.add(vds_sb);
		List<String> fragments = psql.getFragments();
		List<Parameter> dyParas = psql.getDynamicParameters();
		for (int i = 0; i < fragments.size(); i++) {
			MethodInvocation mi_ = newMethodInvocation(ast, ast.newName(name),
					METHOD_APPEND, newStringLiteral(ast, fragments.get(i)));
			list.add(ast.newExpressionStatement(mi_));
			if (i < dyParas.size()) {

				mi_ = newMethodInvocation(ast, ast.newName(name),
						METHOD_APPEND,
						parseExpression(ast, dyParas.get(i).getExpression()));
				list.add(ast.newExpressionStatement(mi_));
			}
		}
		return newMethodInvocation(ast, ast.newName(name), METHOD_TO_STRING);
	}

	void performParameterBinding(AST ast, ParsedSql psql,
			ArrayList<Statement> list, String stmt_name) {
		int i = 1;
		//parameter binding
		for (Parameter p : psql.getBindParameters()) {
			if (p.getType() == Parameter.OUT) {
				MethodInvocation mi1 = newMethodInvocation(
						ast,
						ast.newSimpleName(stmt_name),
						METHOD_REGISTER_OUT_PARAMETER,
						ast.newNumberLiteral("" + i),
						newQualifiedName(ast, java.sql.Types.class
								.getSimpleName(), parsedSource
								.getVariableSqlType(p.getExpression())));
				list.add(ast.newExpressionStatement(mi1));
			} else {
				MethodInvocation mi1 = newMethodInvocation(
						ast,
						ast.newSimpleName(stmt_name),
						"set"
								+ parsedSource.getVariableType(
										p.getExpression(), "Object"),
						ast.newNumberLiteral("" + i),
						parseExpression(ast, p.getExpression()));
				list.add(ast.newExpressionStatement(mi1));
			}
			i++;
		}
	}

	/**
	 * create an ParenthesizedExpression via a expression string<br> the
	 * expression can be complex.
	 * 
	 * @param ast
	 * @param expr
	 *            complex expression e.g. <u>a.name+'-'+a.address<u>
	 * @return
	 */
	Expression parseExpression(AST ast, String expr) {
		ASTParser parser = ASTParser.newParser(API_LEVEL);
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(expr.toCharArray());
		AST a = ast.newParenthesizedExpression().getAST();
		return (Expression) ASTNode.copySubtree(a, parser.createAST(null));
	}

	void performParameterFetching(AST ast, ParsedSql psql,
			ArrayList<Statement> list, String stmt_name) {
		int i = 1;
		for (Parameter p : psql.getBindParameters()) {
			if (p.getType() == Parameter.OUT) {
				Assignment assi2 = ast.newAssignment();
				assi2.setLeftHandSide(parseExpression(ast, p.getExpression()));
				assi2.setRightHandSide(newMethodInvocation(
						ast,
						ast.newSimpleName(stmt_name),
						"get" + parsedSource.getVariableType(p.getExpression()),
						ast.newNumberLiteral("" + i)));
				list.add(ast.newExpressionStatement(assi2));
			}
			i++;
		}
	}

	ArrayList<Statement> generate__sqlj_execute_for(MethodInvocation mi) {
		ArrayList<Statement> list = new ArrayList<Statement>();
		list = generate__sqlj_execute_para(mi);
		return list;
	}

	ArrayList<Statement> generate__sqlj_execute_para(MethodInvocation mi) {
		ArrayList<Statement> list = null;
		try {
			list = generate__sqlj_execute(mi);
			ExpressionStatement es = (ExpressionStatement) list
					.get(list.size() - 2);//the last statement is __sqlj_cs_gen2.execute();
			MethodInvocation es_mi = (MethodInvocation) es.getExpression();
			String ps_id = es_mi.getExpression().toString();
			String rs_id = mi.arguments().get(1).toString();
			AST ast = mi.getAST();
			VariableDeclarationStatement vds = newVariableDeclarationStatement(
					ast,
					java.sql.ResultSet.class.getSimpleName(),
					newVariableDeclarationFragment(
							ast,
							rs_id,
							newMethodInvocation(ast, ast.newSimpleName(ps_id),
									METHOD_GET_RESULT_SET)));
			list.add(vds);
		} catch (ParserException e) {
			throw new RuntimeException(e);
		}//new ArrayList<Statement>();
		return list;
	}

	ClassInstanceCreation newClassInstance(AST ast, String className,
			Expression... args) {
		ClassInstanceCreation inst = ast.newClassInstanceCreation();
		inst.setType(ast.newSimpleType(ast.newName(className)));
		inst.arguments().addAll(Arrays.asList(args));
		return inst;
	}

	QualifiedName newQualifiedName(AST ast, String leftPart, String rightPart) {
		return ast.newQualifiedName(ast.newName(leftPart),
				ast.newSimpleName(rightPart));
	}

	VariableDeclarationFragment newVariableDeclarationFragment(AST ast,
			String name, Expression init) {
		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
		vdf.setName(ast.newSimpleName(name));
		vdf.setInitializer(init);
		return vdf;
	}

	VariableDeclarationStatement newVariableDeclarationStatement(AST ast,
			String typeName, VariableDeclarationFragment... vdfs) {
		VariableDeclarationStatement vds = ast
				.newVariableDeclarationStatement(vdfs[0]);
		vds.setType(ast.newSimpleType(ast.newName(typeName)));
		for (int i = 1; i < vdfs.length; i++)
			vds.fragments().add(vdfs[i]);
		return vds;
	}

	MethodInvocation newMethodInvocation(AST ast, Expression exp, String name,
			Expression... args) {
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setExpression(exp);
		mi.setName(ast.newSimpleName(name));
		for (Expression e : args) {
			mi.arguments().add(e);
		}
		return mi;
	}

	TypeLiteral newTypeLiteral(AST ast, String type) {
		TypeLiteral tl = ast.newTypeLiteral();
		if (isPrimitiveType(type))
			tl.setType(ast.newPrimitiveType(PrimitiveType.toCode(type)));
		else {
			tl.setType(ast.newSimpleType(ast.newName(type)));
		}
		return tl;
	}

	boolean isPrimitiveType(String type) {
		return Arrays.asList("int", "double", "long", "short", "byte", "char",
				"boolean", "float").contains(type);
	}

	StringLiteral newStringLiteral(AST ast, String value) {
		StringLiteral sl = ast.newStringLiteral();
		sl.setLiteralValue(value);
		return sl;
	}

	static private class CompilationUnitImpl implements ICompilationUnit {
		private CompilationUnit unit;

		CompilationUnitImpl(CompilationUnit unit) {
			this.unit = unit;
		}

		public char[] getContents() {
			char[] contents = null;
			try {
				Document doc = new Document();
				TextEdit edits = unit.rewrite(doc, null);
				edits.apply(doc);
				String sourceCode = doc.get();
				if (sourceCode != null)
					contents = sourceCode.toCharArray();
			} catch (BadLocationException e) {
				throw new RuntimeException(e);
			}
			return contents;
		}

		public char[] getMainTypeName() {
			TypeDeclaration classType = (TypeDeclaration) unit.types().get(0);
			return classType.getName().getFullyQualifiedName().toCharArray();
		}

		public char[][] getPackageName() {
			String[] names = getSimpleNames(this.unit.getPackage().getName()
					.getFullyQualifiedName());
			char[][] packages = new char[names.length][];
			for (int i = 0; i < names.length; ++i)
				packages[i] = names[i].toCharArray();
			return packages;
		}

		private String[] getSimpleNames(String fullQname) {
			return fullQname.split("\\.");
		}

		public char[] getFileName() {
			String name = new String(getMainTypeName()) + ".java";
			return name.toCharArray();
		}

		@Override
		public boolean ignoreOptionalProblems() {
			return false;
		}
	}

	private static class NameEnvironmentImpl implements INameEnvironment {

		public NameEnvironmentImpl(CompilationUnit unit) {

		}

		@Override
		public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public NameEnvironmentAnswer findType(char[] typeName,
				char[][] packageName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isPackage(char[][] parentPackageName, char[] packageName) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void cleanup() {
			// TODO Auto-generated method stub

		}

	}

	private static class CompilerRequestorImpl implements ICompilerRequestor {

		@Override
		public void acceptResult(CompilationResult result) {
			System.out.println(result);
		}

	}
}
