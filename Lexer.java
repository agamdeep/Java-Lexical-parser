package jivePackage;
import java.io.*;

public class Lexer {

	static public String ln = "";
	static private char ident = ' ';
	static public BufferLine buffer = new BufferLine(new DataInputStream(System.in));
	static public int nextToken;
	static public String nextLine;
	static public int intValue;

	public static int lex() {
		ln = buffer.getLine();
		if (ln == null)
		{
			nextToken = Token.END;
			intValue = 0;
			return 0;
		}
		if (ln.contains("if") || ln.contains("else")) {
			nextToken = Token.COND;
		} else if (ln.contains("for") || ln.contains("while")){
			nextToken = Token.LOOP;
		}
		else if (ln.contains("int") || ln.contains("String") || ln.contains("boolean")){
			nextToken = Token.DECL;
		} else if (ln.contains("end")) {
			nextToken = Token.END;
		}
		else if(ln.contains("{")){
			nextToken = Token.CMPD;
		}
		else {
			nextToken = Token.ASSIGN;
		}
		return nextToken;
	}
	
	public static void Retokenize(){
		if (ln.contains("if")) {
			nextToken = Token.COND;
		} else if (ln.contains("for") || ln.contains("while")){
			nextToken = Token.LOOP;
		}
		else if (ln.contains("int") || ln.contains("String")){
			nextToken = Token.DECL;
		} else if (ln.contains("end")) {
			nextToken = Token.END;
		}
		else {
			nextToken = Token.ASSIGN;
		}
	}
	
	public int number() {
		return intValue;
	} // number

	public Character identifier() {
		return ident;
	} // letter

	public static void error(String msg) {
		System.err.println(msg);
		System.exit(1);
	} // error


}
