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
		String boardMessage = "{\"comm_type\" : \"GameBoardState\",\"game_name\" : \"My New Game\",\"sequence\" : \"1\",\"timestamp\" : 1349298294.465858,\"states\":{\"client1\": {\"board_state\" :\"00000000000000000000000000000000000000000000000000\",\"piece_number\" : 3,\"cleared_rows\" : [7,8,9]},\"client2\": {\"board_state\" :\"00000000000000000000000000000000000000000000000000\",\"piece_number\" : 4,\"cleared_rows\" : []}}}";
		if(!testParser.parseMessage(boardMessage))
			fail("Parsing failed");

		msg = testParser.getMessage();
		assertEquals("GameMoveResp", msg.get("comm_type"));
		assertEquals("ok", msg.get("resp"));
		
	}
	
	
}
