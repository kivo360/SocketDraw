   import java.io.BufferedReader;
   import java.io.FileNotFoundException;
   import java.io.FileReader;
   import java.io.IOException;
   import java.util.*;
	import java.io.*;
	
public class FileGrabber {
      
		private BufferedReader br = null;
      private int fileLength = 0;
      private int randomNumber;
      private String sCurrentLine = "";
   	private ArrayList<String> strings = new ArrayList<String>();
     
   /***
    * Constructor
    * Adds Nounlist.txt to BufferedReader
    */
   	public FileGrabber()
		{
			try{
            br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream ("nounlist.txt")));
				fileLengthFile();
         }
         catch(Exception e){}
        }
   	
   		/***
   		 * Gets the file length;
   		 * Add String to ArrayList
   		 * @return fileLength;
   		 */
      public int fileLengthFile(){
         try{
					boolean stop = false;
					String in = "";
	            while(!stop) 
					{
						in = br.readLine();
						if(in.equals(null))break;
						else
						{
							fileLength++;
							strings.add(in);
						}
	            }
			 	}
            catch(Exception e){}
         return fileLength;
      }
      	
      /***
       * Grabs a word from Strings ArrayList
       * @return takenWord
       */
		public String getWord()
		{
			String takenWord = "";
			int wordLine = (int) (Math.random() * 2334); 
			takenWord = strings.get(wordLine);
			return takenWord;
		}
   
}
