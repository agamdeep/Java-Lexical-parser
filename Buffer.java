package jivePackage;


public class Buffer {

		private String line = "";
		public int position = 0;
		public int token = 0;
		public int val = 0;
		public char ch = ' ';
		boolean lineFirst = true;
		public char assignLhs = ' ';
		
		public Buffer() { }

		public Buffer(String line, boolean IsIden) {
			this.line = line;
			lineFirst = true;
			String iden = "";
			if(IsIden)
			{
				lineFirst = false;
				while(position <= 2){
					iden += line.charAt(position);
					position++;
				}
				if(iden.equals("int")){
					while ((line.charAt(position) == ' ')){
						position++;
					}
				} else if(iden.equals("Str")){
					position = 6;
					while (!(line.charAt(position) == ' ')){
						position++;
					}
				} else if(iden.equals("boo")){
					position = 7;
					while (!(line.charAt(position) == ' ')){
						position++;
					}
				}
				
				position--;
			}
		}

		public char getChar() {
			position++;
			
			if(lineFirst == true)
				position = 0;
			
			lineFirst = false;
			if (position <= (line.length() - 1)) {
				if (line == null)
					System.exit(0);

				// System.out.println(line);
				line = line + "\n";
			}
			
			if(position >= line.length())
				return ' ';
			
			// for removing any preceeding tabs
			while(line.charAt(position) == '\t')
				position++;
			
			// for removing all blank spaces
			while(line.charAt(position) == ' ')
				position++;
			
			char toReturn = line.charAt(position);
			if(toReturn == ',')
				token = Token.COMMA;
			else if(toReturn == ';')
				token = Token.SEMICOLON;
			else if(toReturn == '=')
				token = Token.ASSIGN_OP;
			else if(toReturn == '*')
				token = Token.MULT_OP;
			else if(toReturn == '+')
				token = Token.ADD_OP;
			else if(toReturn == '/')
				token = Token.DIV_OP;
			else if(toReturn == '-')
				token = Token.SUB_OP;
			else if(toReturn == '(')
				token = Token.LEFT_PAREN;
			else if(toReturn == ')')
				token = Token.RIGHT_PAREN;
			else if(!Character.isDigit(toReturn) )
				token = Token.ID;
			
			int num = 0;
			
			if(Character.isDigit(toReturn))
			{
				while(Character.isDigit(line.charAt(position)))
				{
					num = num * 10 + Character.digit(line.charAt(position), 10);
					position++;
				}
				position--;
				val = num;
				token = Token.INT_LIT;
			}
				
			ch = toReturn;
			return toReturn;
		}
		
		public void ParseIf(){
			position++;
			
			if(lineFirst == true)
				position = 0;
			
			lineFirst = false;
			
			// for removing any preceeding tabs
			while(line.charAt(position) == '\t')
				position++;
						
			while(line.charAt(position) == ' ')
				position++;
			
			String iden = "";
			while(!iden.equals("if")){
				iden += line.charAt(position);
				position++;
			}
			
			position--;
		}
		
		public void ParseElse(){
			position++;
			
			if(lineFirst == true)
				position = 0;
			
			lineFirst = false;
			
			// for removing any preceeding tabs
			while(line.charAt(position) == '\t')
				position++;
			
			while(line.charAt(position) == ' ')
				position++;
			
			String iden = "";
			while(!iden.equals("else")){
				iden += line.charAt(position);
				position++;
			}
			
			position--;
		}
		
		public void ParseFor(){
			position++;
			
			if(lineFirst == true)
				position = 0;
			
			lineFirst = false;
			
			// for removing any preceeding tabs
			while(line.charAt(position) == '\t')
				position++;
			
			while(line.charAt(position) == ' ')
				position++;
			
			String iden = "";
			while(!iden.equals("for")){
				iden += line.charAt(position);
				position++;
			}
			
			while(line.charAt(position) == ' ')
				position++;
			
			position--;
		}

	}
 

