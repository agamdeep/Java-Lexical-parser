package jivePackage;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

public class BufferLine {

		private List<String> lines = new ArrayList<String>();
		private String line = "";
		public int position = 0;
		public Buffer bufferChar = new Buffer();
		private DataInputStream inStream;
		public int val = 0;

		public BufferLine(DataInputStream i) {
			this.inStream = i;
		} // Buffer

		@SuppressWarnings("deprecation")
		public String getLine() {
			position++;
			if (lines.size() == 0) {
				try {
					while (!(line = inStream.readLine()).equals("end"))
					{
						lines.add(line);
					}
				} catch (Exception e) {
					System.err.println("Invalid read operation");
					System.exit(1);
				}
				if (line == null)
					System.exit(0);
				position = 0;
			
			}
			
			if(position >= lines.size())
				return null;
			
			String line = lines.get(position);
			while(line.trim().length() == 0){
				 position++;
				 if(position > lines.size())
					 return null;
				 
				 line = lines.get(position);
			}
			if(line.contains("int") || line.contains("String") || line.contains("boolean"))
				bufferChar = new Buffer(line, true);
			else
				bufferChar = new Buffer(line, false);
			
			
			return line;
		}
		
		public String getPrevLine(){
			if(position > 0)
				position--;
			
			if (lines.size() == 0) {
				try {
					while (!(line = inStream.readLine()).equals("end"))
					{
						lines.add(line);
					}
				} catch (Exception e) {
					System.err.println("Invalid read operation");
					System.exit(1);
				}
				if (line == null)
					System.exit(0);
				position = 0;
			
			}
			
			if(position >= lines.size())
				return null;
			
			String line = lines.get(position);
			if(line.contains("int") || line.contains("String"))
				bufferChar = new Buffer(line, true);
			else
				bufferChar = new Buffer(line, false);
			
			
			return line;
		}

	} // class Buffer
 
