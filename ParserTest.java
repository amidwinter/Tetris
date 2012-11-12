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
	public void testConnectMessage() {
		String connectMessage = "{\"comm_type\" : \"MatchConnectResp\",\"resp\" : \"ok\",\"client_token\" : \"dc8d75a6-448d-4108-8bfa-52470b97f35f\"}";
		if(!testParser.parseMessage(connectMessage))
			fail("Parsing failed");

		msg = testParser.getMessage();
		System.out.println(msg.get("comm_type"));
		assertEquals("MatchConnectResp", msg.get("comm_type"));
		assertEquals("ok", msg.get("resp"));
		assertEquals("dc8d75a6-448d-4108-8bfa-52470b97f35f", msg.get("client_token"));
	}
	
	@Test
	public void testGameMoveRespMessage(){
		String moveMessage = "{\"comm_type\" : \"GameMoveResp\",\"resp\" : \"ok\"}";
		if(!testParser.parseMessage(moveMessage))
			fail("Parsing failed");

		msg = testParser.getMessage();
		System.out.println(msg.get("comm_type"));
		assertEquals("GameMoveResp", msg.get("comm_type"));
		assertEquals("ok", msg.get("resp"));
	}
}
