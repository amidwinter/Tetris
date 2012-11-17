import java.lang.reflect.Field;
import java.util.Arrays;
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
		states states;
		String[] queue;
	}
	
	public class GamePieceState
	{
		String comm_type;
		String game_name;
		String sequence;
		float timestamp;
		states states;
		String[] queue;
	}
	
	public class states
	{
		Team70 Team70;
		client2 client2;
	}
	
	public class Team70
	{
		String board_state;
		int piece_number;
		int[] cleared_rows;
		int orient;
		String piece;
		int number;
		int row;
		int col;
	}
	
	public class client2
	{
		String board_state;
		int piece_number;
		int[] cleared_rows;
		int orient;
		String piece;
		int number;
		int row;
		int col;
	}
	
	public class GameEnd
	{
		String comm_type;
		String game_name;
		int sequence;
		float timestamp;
		scores scores;
		String winner;
		String match_token;
	}
	
	public class scores
	{
		int Team70;
		int client2;
	}
	
	public class MatchEnd
	{
		String comm_type;
		String match_name;
		String match_token;
		String status;
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
		JSON = JSON.replaceAll("\\s", "");
		Field[] fields = null;
		boolean success = true;
		// Deciding what type of message this is
		String commType = getCommType(JSON);
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
			fields = Team70.class.getDeclaredFields();
			Map<String, String> map1 = convertToMap(fields, GBS.states.Team70);
			fields = GameBoardState.class.getDeclaredFields();
			Map<String, String> map2 = convertToMap(fields, GBS);
			msg.putAll(map1);
			msg.putAll(map2);
			
		}
		else if(commType.equals("GamePieceState"))
		{
			GamePieceState GPS = gson.fromJson(JSON, GamePieceState.class);
			fields = Team70.class.getDeclaredFields();
			Map<String, String> map1 = convertToMap(fields, GPS.states.Team70);
			fields = GamePieceState.class.getDeclaredFields();
			Map<String, String> map2 = convertToMap(fields, GPS);
			msg.putAll(map1);
			msg.putAll(map2);
		}
		else if(commType.equals("GameEnd"))
		{
			GameEnd GE = gson.fromJson(JSON, GameEnd.class);
			fields = scores.class.getDeclaredFields();
			Map<String, String> map1 = convertToMap(fields, GE.scores);
			fields = GameEnd.class.getDeclaredFields();
			Map<String, String> map2 = convertToMap(fields, GE);
			msg.putAll(map1);
			msg.putAll(map2);
		}
		else if(commType.equals("MatchEnd"))
		{
			MatchEnd ME = gson.fromJson(JSON, MatchEnd.class);
			fields = GameMove.class.getDeclaredFields();
			msg = convertToMap(fields, ME);
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
			for(int i = 0; i < fields.length; i++)
			{
				
				//System.out.println(fields[i].getName() + " " + (String)fields[i].get(o) + " " + i);
				String value = "";
				String name = fields[i].getName();
				if(fields[i].get(o) == null)
				{
					System.out.println("null!");
				}
				else if(fields[i].get(o).getClass() == states.class)
				{
					System.out.println("states!");
				}
				else if(fields[i].get(o).getClass() == scores.class)
				{
					System.out.println("scores!");
				}
				else if(fields[i].get(o).getClass() == int[].class)
				{
					int[] valueIntArr = (int[])fields[i].get(o);
					
					value = Arrays.toString(valueIntArr);
					System.out.println("int[]!   " + value);
				}
				else if(fields[i].get(o).getClass() == String[].class)
				{
					String[] valueStringArr = (String[])fields[i].get(o);
					value = Arrays.toString(valueStringArr);
					System.out.println("string[]!   " + value);
				}
				else
				{
					value = fields[i].get(o).toString();
					System.out.println("string!   " + value);
				}
				
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
		for(int i = 0; i < tokens.length; i++) {
			tokens[i] = tokens[i].replaceAll("\"", "");
			tokens[i] = tokens[i].replace("{", "");
			tokens[i] = tokens[i].replace("}", "");
			String tokenSplit[] = tokens[i].split(":");
			
			if(tokenSplit[0].equals("comm_type"))
				return tokenSplit[1];
		}
		return "error";
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