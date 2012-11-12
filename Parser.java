import java.util.Map;
import java.util.HashMap;

public class Parser
{
	//Class Variables
	Map<String,String> msg=new HashMap<String, String>();
	
	/**
	 * Initializes the Parser object. 
	 * @param
	 * @return
	 */
	public Parser()
	{
		
	}
	/**
	 * Takes in a String argument in the JSON format and parses it to decipher the message. 
	 * @param JSON The JSON formatted string containing the message to be parsed.
	 * @return True if message parsed correctly, False otherwise
	 */
	public boolean parseMessage(String JSON)
	{	
		boolean success = true;
		msg.clear();
		JSON = JSON.replaceAll("\\s","");
		String tokens[] = JSON.split(",");
		for(int i = 0; i < tokens.length; i++)
		{
			tokens[i] = tokens[i].replaceAll("\"", "");
			tokens[i] = tokens[i].replace("{", "");
			tokens[i] = tokens[i].replace("}", "");
			String objectPair[] = tokens[i].split(":");
			msg.put(objectPair[0], objectPair[1]);
			
			System.out.println(tokens[i]);
		}
		
		return success;
	}
	
	public Map getMessage()
	{
		return msg;
	}
}