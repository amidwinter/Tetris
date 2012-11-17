import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class ParserTest {

	Parser testParser;
	Map<String,String> msg;
	
	@Before
	public void before(){
		testParser = new Parser();
	}
	
	@Test
	public void testGetConnectMessage() {
		String connectMessage = "{\"comm_type\" : \"MatchConnectResp\",\"resp\" : \"ok\",\"client_token\" : \"dc8d75a6-448d-4108-8bfa-52470b97f35f\"}";
		if(!testParser.parseMessage(connectMessage))
			fail("Parsing failed");

		msg = testParser.getMessage();
		assertEquals("MatchConnectResp", msg.get("comm_type"));
		assertEquals("ok", msg.get("resp"));
		assertEquals("dc8d75a6-448d-4108-8bfa-52470b97f35f", msg.get("client_token"));
	}
	
	@Test
	public void testGetGameMoveRespMessage(){
		String moveMessage = "{\"comm_type\" : \"GameMoveResp\",\"resp\" : \"ok\"}";
		if(!testParser.parseMessage(moveMessage))
			fail("Parsing failed");

		msg = testParser.getMessage();
		assertEquals("GameMoveResp", msg.get("comm_type"));
		assertEquals("ok", msg.get("resp"));
	}
	
	@Test
	public void testGetGameBoardStateMessage(){
		String boardMessage = "{\"comm_type\" : \"GameBoardState\",\"game_name\" : \"My New Game\",\"sequence\" : \"1\",\"timestamp\" : 1349298294.465858,\"states\":{\"Team 70\": {\"board_state\" :\"00000000000000000000000000000000000000000000000000\",\"piece_number\" : 3,\"cleared_rows\" : [7,8,9]},\"client2\": {\"board_state\" :\"00000000000000000000000000000000000000000000000000\",\"piece_number\" : 4,\"cleared_rows\" : []}}}";
		if(!testParser.parseMessage(boardMessage))
			fail("Parsing failed");

		msg = testParser.getMessage();
		assertEquals("GameBoardState", msg.get("comm_type"));
		assertEquals("MyNewGame", msg.get("game_name"));
		assertEquals("1", msg.get("sequence"));
		//assertEquals("1349298294.465858", msg.get("timestamp"));
		assertEquals("00000000000000000000000000000000000000000000000000", msg.get("board_state"));
		assertEquals("3", msg.get("piece_number"));
		assertEquals("[7, 8, 9]", msg.get("cleared_rows"));
	}
	
	@Test
	public void testGetGamePieceStateMessage()
	{
		String boardMessage = "{\"comm_type\" : \"GamePieceState\",\"game_name\" : \"My New Game\",\"sequence\" : \"2\",\"timestamp\" : 1349298294.465858,\"states\" :{\"Team70\":{\"orient\" : 3,\"piece\" : \"T\", \"number\" : 3,\"row\" : 1, \"col\" : 2},\"client2\":{\"orient\" : 1,\"piece\" : \"I\",\"number\" : 4,\"row\" : 4, \"col\" : 2}},\"queue\": [ \"Z\",\"I\",\"Z\",\"J\",\"L\"]}";
		if(!testParser.parseMessage(boardMessage))
			fail("Parsing failed");

		msg = testParser.getMessage();
		assertEquals("GamePieceState", msg.get("comm_type"));
		assertEquals("MyNewGame", msg.get("game_name"));
		assertEquals("2", msg.get("sequence"));
		//assertEquals("1349298294.465858", msg.get("timestamp"));
		assertEquals("3", msg.get("orient"));
		assertEquals("T", msg.get("piece"));
		assertEquals("3", msg.get("number"));
		assertEquals("1", msg.get("row"));
		assertEquals("2", msg.get("col"));
		assertEquals("[Z, I, Z, J, L]", msg.get("queue"));
	}
	
	@Test
	public void testGameEndMessage()
	{
		String boardMessage = " {\"comm_type\": \"GameEnd\",\"game_name\": \"My New Game\",\"sequence\": 352,\"timestamp\": 1350082490.358995,\"scores\": {\"Team70\": 0,\"client2\": 0},\"winner\": \"client2\",\"match_token\": \"d0d39280-5b95-45ee-aa27-69946547118d\"}";
		if(!testParser.parseMessage(boardMessage))
			fail("Parsing failed");
		msg = testParser.getMessage();
		assertEquals("GameEnd", msg.get("comm_type"));
		assertEquals("MyNewGame", msg.get("game_name"));
		assertEquals("352", msg.get("sequence"));
		//assertEquals("1350082490.358995", msg.get("timestamp"));
		assertEquals("0", msg.get("Team70"));
		assertEquals("client2", msg.get("winner"));
		assertEquals("d0d39280-5b95-45ee-aa27-69946547118d", msg.get("match_token"));		
	}
	
	
}
