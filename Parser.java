package jivePackage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Parser {
	public static boolean IsLexed = false;
	public static void main(String[] args) {
		System.out.println("Enter program ending with 'end' keyword\n");
		
		Lexer.lex();
		new Program();
		Code.output();
	}
}

class Program {
	Decls decl;
	Stmts statements;
	
	public Program(){
		decl = new Decls();
		statements = new Stmts();
	}
}

class Decls { // decls -> int a,b,c;
	Idlist idList;
	Decls declarations;
	
	public Decls() {
		idList = new Idlist();
		Lexer.lex();
		if(Lexer.nextToken == Token.DECL)
			declarations = new Decls();
	}
}

class Idlist { // Idlist -> int a,b,c; all ids (a,b,c)
	char id;
	Idlist ilist;
	
	public Idlist(){
		id = Lexer.buffer.bufferChar.getChar();
		
		// saving all variables in global map
		StoreLocation.TotalVariables++;
		StoreLocation.VariableLocations.put(id, StoreLocation.TotalVariables);
		
		char nextChar = Lexer.buffer.bufferChar.getChar();
		if(Lexer.buffer.bufferChar.token == Token.COMMA)
			ilist = new Idlist();
	}
}

class Stmts {
	Stmt statement;
	Stmts statements;
	
	public Stmts(){
		if(Lexer.ln == null)
			return;
		if(Lexer.ln.trim().length() == 1 && Lexer.nextToken == Token.RIGHT_BRCT)
			return;
		else if(Lexer.ln.trim().length() == 1 && Lexer.nextToken == Token.LEFT_BRCT)
			Lexer.lex();
		
		if(Lexer.nextToken != Token.END)
			statement = new Stmt();
		
		if(Lexer.ln != null && Lexer.ln.contains("}")) // It means we have just finished compound statements
			return;
		
		if(!ParserGlobalVars.IsLexed)
			Lexer.lex();
		
		ParserGlobalVars.IsLexed = false;
		
		if(Lexer.nextToken != Token.END)
			statements = new Stmts();
	}
}

class Stmt {
	Stmt stmt;
	Assign assign;
	Cond condition;
	Cmpd compound;
	Loop loop;
	
	public Stmt(){
		switch(Lexer.nextToken)
		{
		case Token.ASSIGN:
			assign = new Assign();
			Code.GenerateStoreInstruction(Lexer.buffer.bufferChar.assignLhs);
			break;
		case Token.COND:
			condition = new Cond();
			break;
		case Token.CMPD:
			compound = new Cmpd();
			break;
		case Token.LOOP:
			loop = new Loop();
			break;
		default:
				break;
		}
	}
}

class Assign {
	Expr expr;
	char lhs;
	
	public Assign(){
		lhs = Lexer.buffer.bufferChar.getChar();
		if(lhs == '{') // in case of if condition statement like {x = y;}
			lhs = Lexer.buffer.bufferChar.getChar();
		
		// updating variable storage
		if(!StoreLocation.VariableLocations.containsKey(lhs))
			StoreLocation.VariableLocations.put(lhs, 0);
		
		Lexer.buffer.bufferChar.assignLhs = lhs;
		
		Lexer.buffer.bufferChar.getChar(); // For getting = symbol
		Lexer.buffer.bufferChar.getChar();
		expr = new Expr();
	}
}

class Cond {
	Rexpr regularExp;
	Cmpd compoundStatements;
	Stmt ifStatement;
	Stmt elseStatement;
	int ExpSavedOn;
	
	public Cond(){
		    // parsing if part
			Lexer.buffer.bufferChar.ParseIf(); // for parsing 'if' part of condition
			Lexer.buffer.bufferChar.getChar(); // this will place pointer on '(' of if condition
			Lexer.buffer.bufferChar.getChar(); // this will skip '('
			regularExp = new Rexpr();
			Lexer.lex();
			
			// executing if statements
			ifStatement = new Stmt();
			
			if(!ParserGlobalVars.IsLexed){
				Lexer.lex();
				ParserGlobalVars.IsLexed = true;
			}
			
			if (Lexer.ln != null && Lexer.ln.contains("else")){
				// inserting goto statement if else part is present
				Code.code[Code.codeptr] = MemoryLocation.CurrentMemoryLocation + ": goto ";
				ExpSavedOn = Code.codeptr;
				Code.codeptr++;
				MemoryLocation.CurrentMemoryLocation += 3;
			}
			
			// updating if instruction saved
			Code.code[regularExp.ExpSavedOn] += MemoryLocation.CurrentMemoryLocation;
			
			// parsing else part if present
			if (Lexer.ln != null && Lexer.ln.contains("else")){
				ParserGlobalVars.IsLexed = false;
				if(Lexer.ln.trim().length() == 4)
					Lexer.lex();
				else
				{
					Lexer.Retokenize();
					Lexer.buffer.bufferChar.ParseElse();
				}
				
				elseStatement = new Stmt();
			}
			
			// inserting goto location only if else condition is present
			if (Lexer.ln != null && Lexer.ln.contains("else")){
				Code.code[ExpSavedOn] += MemoryLocation.CurrentMemoryLocation;
			}
			
			if(!ParserGlobalVars.IsLexed){
				Lexer.lex();
				ParserGlobalVars.IsLexed = true;
			}
	}
}

class Loop {
	Assign assign;
	Rexpr regularExp;
	Assign incrementAssign;
	Stmt statement;
	int ExpSavedOn;
	
	public Loop(){
		// parsing loop till 'for (' part below
		Lexer.buffer.bufferChar.ParseFor(); // Parsing 'for' part of loop
		Lexer.buffer.bufferChar.getChar(); // will take pointer to '(' part of for loop
		
		// check if we will get ';' then assignment part in loop is missing
		char assignCheck = Lexer.buffer.bufferChar.getChar(); // will skip '('
		
		// reading assign part of loop if it is available
		if(assignCheck != ';')
		{
			Lexer.buffer.bufferChar.position--;
			assign = new Assign();
			Code.GenerateStoreInstruction(Lexer.buffer.bufferChar.assignLhs);
		}
		
		ExpSavedOn = MemoryLocation.CurrentMemoryLocation;
		Lexer.buffer.bufferChar.getChar(); // To skip ; between assignment and conditional part
		
		// check if we will get ';' then conditional part in loop is missing
		char conditionPart = Lexer.buffer.bufferChar.ch;
		
		// reading regular expression or condition part if it is available
		if(conditionPart != ';')
			regularExp = new Rexpr();
		
		// check if we will get ')' then increment part in loop is missing
		char incrementPart = Lexer.buffer.bufferChar.getChar();
		
		// swap start for part1
		int SwapPart1StartMemoryLoc = MemoryLocation.CurrentMemoryLocation;
		int SwapPart1StartCodeLoc = Code.codeptr;
		
		// reading increment assignment part of for loop if it is available
		if(incrementPart != ')')
		{
			Lexer.buffer.bufferChar.position--;
			incrementAssign = new Assign();
			Code.GenerateStoreInstruction(Lexer.buffer.bufferChar.assignLhs);
		}
		
		// swap end for part1 also swap start for part2
		int SwapPart1EndMemoryLoc = MemoryLocation.CurrentMemoryLocation;
		int SwapPart1EndCodeLoc = Code.codeptr;
		
		// checking if for loop has ended with starting curly bracket for compound statements
		char lastCharLoop = Lexer.ln.trim().substring(Lexer.ln.trim().length() - 1).charAt(0);
		
		// for getting to next line for reading statement part of loop
		Lexer.lex();
		
		// changing token to compound on discovery of '{' character at end of loop
		if(lastCharLoop == '{')
			Lexer.nextToken = Token.CMPD;
		
		// reading statement part of loop 
		statement = new Stmt();
		
		// swap end for part1 also swap start for part2
		int SwapPart2EndMemoryLoc = MemoryLocation.CurrentMemoryLocation;
		int SwapPart2EndCodeLoc = Code.codeptr;
		
		// we are swapping only if Assign happened
		if(incrementPart != ')')
		{
			// these will store instructions of assign and statements to be swapped
			List<String> Part1 = new ArrayList<String>();
			List<String> Part2 = new ArrayList<String>();
			int memoryOffset = SwapPart1EndMemoryLoc - SwapPart1StartMemoryLoc;
			int memoryOffsetPart1 = SwapPart2EndMemoryLoc - SwapPart1EndMemoryLoc;
			
			for(int i = SwapPart1StartCodeLoc; i < SwapPart1EndCodeLoc; i++)
			{
				String instruction = Code.code[i];
				String memLoc = "";
				memLoc += instruction.charAt(0);
				memLoc += instruction.charAt(1);
				int intMemLoc = Integer.parseInt(memLoc);
				intMemLoc += memoryOffsetPart1;
				String newInstruction = Integer.toString(intMemLoc) + instruction.substring(2, instruction.length());
				
				// also checking if instruction contains if_ part then updating jump part
				if(newInstruction.contains("if_"))
				{
					String ifMemLoc = "";
					ifMemLoc += newInstruction.charAt(newInstruction.length() - 2);
					ifMemLoc += newInstruction.charAt(newInstruction.length() - 1);
					
					int intIfMemLoc = Integer.parseInt(ifMemLoc);
					intIfMemLoc += memoryOffsetPart1;
					
					newInstruction = newInstruction.substring(0, instruction.length()-2) + Integer.toString(intIfMemLoc);
				}
				
				Part1.add(newInstruction);
			}
			
			for(int i = SwapPart1EndCodeLoc; i < SwapPart2EndCodeLoc; i++)
			{
				String instruction = Code.code[i];
				String memLoc = "";
				memLoc += instruction.charAt(0);
				memLoc += instruction.charAt(1);
				int intMemLoc = Integer.parseInt(memLoc);
				intMemLoc -= memoryOffset;
				String newInstruction = Integer.toString(intMemLoc) + instruction.substring(2, instruction.length());
				
				// also checking if instruction contains if_ part then updating jump part
				if(newInstruction.contains("if_"))
				{
					String ifMemLoc = "";
					ifMemLoc += newInstruction.charAt(newInstruction.length() - 2);
					ifMemLoc += newInstruction.charAt(newInstruction.length() - 1);
					
					int intIfMemLoc = Integer.parseInt(ifMemLoc);
					intIfMemLoc -= memoryOffset;
					
					newInstruction = newInstruction.substring(0, instruction.length()-2) + Integer.toString(intIfMemLoc);
				}
				
				Part2.add(newInstruction);
			}
			
			List<String> swappedList = new ArrayList<String>(Part2);
			swappedList.addAll(Part1);
			String[] swappedListArray = new String[swappedList.size()];
			swappedList.toArray(swappedListArray);
			
			// swapping assign and statement parts of for loop
			int swapListCount = 0;
			for(int i = SwapPart1StartCodeLoc; i < SwapPart2EndCodeLoc; i++)
			{
				Code.code[i] = swappedListArray[swapListCount++];
			}
		}
		
		// generating code for goto instruction
		Code.code[Code.codeptr] = MemoryLocation.CurrentMemoryLocation + ": goto " + ExpSavedOn;
		Code.codeptr++;
		MemoryLocation.CurrentMemoryLocation += 3;
		
		Code.code[regularExp.ExpSavedOn] += MemoryLocation.CurrentMemoryLocation;
	}
}

class Rexpr {
	Expr expression1;
	String op;
	Expr expression2;
	int ExpSavedOn;
	
	public Rexpr(){
		expression1 = new Expr(); // this will evaluate left hand side of expression
		
		// getting operator
		op = Character.toString(Lexer.buffer.bufferChar.ch);
		if(op.equals("=") || op.equals("!"))
			op += Character.toString(Lexer.buffer.bufferChar.getChar());
		
		Lexer.buffer.bufferChar.getChar(); // this will place pointer on rhs expression of condition
		
		if((op.equals("<") || op.equals(">"))  && Lexer.buffer.bufferChar.ch == '=')
		{
			op += "=";
			Lexer.buffer.bufferChar.getChar();
		}
		
		expression2 = new Expr();
		
		// updating instruction
		String str = MemoryLocation.CurrentMemoryLocation + ": ";
		if(op.equals("<"))
			str += "if_icmpge ";
		else if(op.equals(">"))
			str += "if_icmple ";
		else if(op.equals("<="))
			str += "if_icmpgt ";
		else if(op.equals(">="))
			str += "if_icmplt ";
		else if(op.equals("=="))
			str += "if_icmpne ";
		else if(op.equals("!="))
			str += "if_icmpeq ";
		
		Code.code[Code.codeptr] = str;
		ExpSavedOn = Code.codeptr;
		
		Code.codeptr++;
		MemoryLocation.CurrentMemoryLocation += 3;
	}
}

class Cmpd {
	Stmts statements;
	
	public Cmpd(){
		Lexer.Retokenize();
		statements = new Stmts();
		Lexer.lex();
		ParserGlobalVars.IsLexed = true;
	}
}

class Expr   { // expr -> term (+ | -) expr | term
	Term t;
	Expr e;
	char op;

	public Expr() {
		
		t = new Term();
		//LexerLine.buffer.bufferChar.getChar(); 
		if (Lexer.buffer.bufferChar.token == Token.ADD_OP || Lexer.buffer.bufferChar.token == Token.SUB_OP) {
			op = Lexer.buffer.bufferChar.ch;
			Lexer.buffer.bufferChar.getChar();
			//LexerLine.lex();
			e = new Expr();
			Code.gen(Code.opcode(op));	 
		}
	}
}

class Term    { // term -> factor (* | /) term | factor
	Factor f;
	Term t;
	char op;

	public Term() {
		f = new Factor();
		if (Lexer.buffer.bufferChar.token == Token.MULT_OP || Lexer.buffer.bufferChar.token == Token.DIV_OP) {
			op = Lexer.buffer.bufferChar.ch; 
			//LexerLine.lex();
			Lexer.buffer.bufferChar.getChar();
			t = new Term();
			Code.gen(Code.opcode(op));
			}
	}
}

class Factor { // factor -> number | '(' expr ')'
	Expr e;
	int i;

	public Factor() {
		
		switch (Lexer.buffer.bufferChar.token) {
		case Token.ID:
			Code.GenerateLoadInstruction(Lexer.buffer.bufferChar.ch);
			Lexer.buffer.bufferChar.getChar();
			break;
		case Token.INT_LIT: // number
			i = Lexer.buffer.bufferChar.val;
			Code.gen(Code.intcode(i));
			Lexer.buffer.bufferChar.getChar(); 
			//LexerLine.lex();
			break;
		case Token.LEFT_PAREN: // '('
			//LexerLine.lex();
			Lexer.buffer.bufferChar.getChar(); 
			e = new Expr();
			Lexer.buffer.bufferChar.getChar();  // skip over ')'
			break;
		default:
			break;
		}
	}
}


class Code {
	static String[] code = new String[100];
	static int codeptr = 0;
	
	public static void gen(String s) {
		code[codeptr] = s;
		codeptr++;
	}
	
	public static void GenerateLoadInstruction(char id){
		int location = StoreLocation.VariableLocations != null ? (int)StoreLocation.VariableLocations.get(id):0;
		if(location < 4)
		{
			code[codeptr] = MemoryLocation.CurrentMemoryLocation + ": iload_" + location;
			MemoryLocation.CurrentMemoryLocation++;
		}
		else
		{
			code[codeptr] = MemoryLocation.CurrentMemoryLocation + ": iload " + location;
			MemoryLocation.CurrentMemoryLocation += 2;
		}
		
		codeptr++;
	}
	
	public static void GenerateStoreInstruction(char id)
	{
		String str = "";
		if((int)StoreLocation.VariableLocations.get(id) > 0)
		{
			int location = (int)StoreLocation.VariableLocations.get(id);
			str += MemoryLocation.CurrentMemoryLocation + ": istore_" + location;
			if(location < 4)
				MemoryLocation.CurrentMemoryLocation++;
			else
				MemoryLocation.CurrentMemoryLocation += 2;
			
			code[codeptr] = str;
			codeptr++;
			
			return;
		}
		else if(StoreLocation.CurrentStoreLocation < 4)
		{
			str += MemoryLocation.CurrentMemoryLocation + ": istore_" + StoreLocation.CurrentStoreLocation;
			
			// updating variable location
			Iterator entries = StoreLocation.VariableLocations.entrySet().iterator();
			while (entries.hasNext()) {
			    Map.Entry entry = (Map.Entry) entries.next();
			    char key = (char)entry.getKey();
			    Integer value = (Integer)entry.getValue();
			    if(value == 0)
			    {
			    	StoreLocation.VariableLocations.put(key, StoreLocation.CurrentStoreLocation);
			    	break;
			    }
			}
			
			MemoryLocation.CurrentMemoryLocation++;
		}
		else
		{
			str += MemoryLocation.CurrentMemoryLocation + ": istore " + StoreLocation.CurrentStoreLocation;
			
			// updating variable location
			Iterator entries = StoreLocation.VariableLocations.entrySet().iterator();
			while (entries.hasNext()) {
			    Map.Entry entry = (Map.Entry) entries.next();
			    char key = (char)entry.getKey();
			    Integer value = (Integer)entry.getValue();
			    if(value == 0)
			    {
			    	StoreLocation.VariableLocations.put(key, StoreLocation.CurrentStoreLocation);
			    	break;
			    }
			}
			
			MemoryLocation.CurrentMemoryLocation += 2;
		}
		
		code[codeptr] = str;
		codeptr++;
		
		StoreLocation.CurrentStoreLocation++;
	}
	
	public static String intcode(int i) {
		String str = "";
		
		if (i > 127) 
		{
			str += MemoryLocation.CurrentMemoryLocation + ": sipush " + i;
			MemoryLocation.CurrentMemoryLocation += 3;
			
			return str;
		}
		
		if (i > 5) 
		{
			str += MemoryLocation.CurrentMemoryLocation + ": bipush " + i;
			MemoryLocation.CurrentMemoryLocation += 2;
			
			return str;
		}
		
		
		str += MemoryLocation.CurrentMemoryLocation + ": iconst_" + i;
		MemoryLocation.CurrentMemoryLocation++;
		
		return str;
	}
	
	public static String opcode(char op) {
		String toReturn = "";
		switch(op) {
		case '+' : 
			toReturn += MemoryLocation.CurrentMemoryLocation + ": iadd";
			MemoryLocation.CurrentMemoryLocation++;
			return toReturn;
		case '-':  
			toReturn += MemoryLocation.CurrentMemoryLocation + ": isub";
			MemoryLocation.CurrentMemoryLocation++;
			return toReturn;
		case '*':  
			toReturn += MemoryLocation.CurrentMemoryLocation + ": imul";
			MemoryLocation.CurrentMemoryLocation++;
			return toReturn;
		case '/':  
			toReturn += MemoryLocation.CurrentMemoryLocation + ": idiv";
			MemoryLocation.CurrentMemoryLocation++;
			return toReturn;
		default: 
			return "";
		}
	}
	
	public static void output() {
		
		// adding last keyword return
		code[codeptr] = MemoryLocation.CurrentMemoryLocation + ": return";
		codeptr++;
		
		for (int i=0; i<codeptr; i++)
			System.out.println(code[i]);
	}
}


