import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;


public class Parser
{
	private Map<String,String> msg=new HashMap<String, String>();
	
	public class MatchConnect
	{
		String comm_type;
		String resp;
		String client_token;
	}
	
	public class GameMove
	{
		String comm_type;
		String resp;
	}
	
	public class GameBoardState
	{
		String comm_type;
		String game_name;
		String sequence;
		float timestamp;
		public class states
		{
			public class client1
			{
				String board_state;
				int piece_number;
				int[] cleared_rows;
			}
			public class client2
			{
				String board_state;
				int piece_number;
				int[] cleared_rows;
			}
		}
		String[] queue;
	}
	
	public class GamePieceState
	{
		String comm_type;
		String game_name;
		String sequence;
		float timestamp;
		public class states
		{
			public class client1
			{
				int orient;
				String piece;
				int number;
				int row;
				int col;
			}
			public class client2
			{
				int orient;
				String piece;
				int number;
				int row;
				int col;
			}
		}
		String[] queue;
	}
	
	
	
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
		Gson gson = new Gson();
		Field[] fields = null;
		boolean success = true;
		// Deciding what type of message this is
		String commType = getCommType(JSON);
		System.out.println(commType);
		if(commType.equals("MatchConnectResp"))
		{
			MatchConnect MC = gson.fromJson(JSON, MatchConnect.class);
			fields = MatchConnect.class.getDeclaredFields();
			msg = convertToMap(fields, MC);
		}
		else if(commType.equals("GameMoveResp"))
		{
			GameMove GM = gson.fromJson(JSON, GameMove.class);
			fields = GameMove.class.getDeclaredFields();
			msg = convertToMap(fields, GM);
		}
		else if(commType.equals("GameBoardState"))
		{
			GameBoardState GBS = gson.fromJson(JSON, GameBoardState.class);
			fields = GameBoardState.states.client1.class.getDeclaredFields();
			msg = convertToMap(fields, GBS);
		}
		else if(commType.equals("GamePieceState"))
		{
			GamePieceState GPS = gson.fromJson(JSON, GamePieceState.class);
			fields = GamePieceState.states.client1.class.getDeclaredFields();
			msg = convertToMap(fields, GPS);
		}
		else
			success = false;
		
		
		return success;
	}
	
	public Map<String, String> convertToMap(Field[] fields, Object o)
	{
		Map<String,String> tempMap = new HashMap<String, String>();
		try
		{
			for(int i = 0; fields[i] != null; i++)
			{
				
				System.out.println(fields[i].getName() + " " + (String)fields[i].get(o) + " " + i);
				
				String name = fields[i].getName();
				String value = (String)fields[i].get(o);
				tempMap.put(name, value);
			}
		}
		catch(IllegalAccessException e)
		{
			System.out.println(e);
		}
		catch(ArrayIndexOutOfBoundsException ai)
		{
			
		}
		return tempMap;
	}
	
	public String getCommType(String JSON)
	{
		String messageType = JSON.replaceAll("\\s", "");
		String tokens[] = messageType.split(",");
		tokens[0] = tokens[0].replaceAll("\"", "");
		tokens[0] = tokens[0].replace("{", "");
		tokens[0] = tokens[0].replace("}", "");
		String commType[] = tokens[0].split(":");
		return commType[1];
	}
	
	public Map<String,String> getMessage()
	{
		return msg;
	}
	
	/**
	 * Takes in a map containing information to be formatted into JSON, and a string telling what
	 * sort of message it is
	 * @param toSend A map containing the variables to be formatted
	 * @param type A string that identifies the type of message
	 * @return JSON The formatted string
	 */
	public String formatMessage(Map<String, String> toSend, String type)
	{
		String JSON = "";
		return JSON;
	}
}