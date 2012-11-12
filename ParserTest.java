import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ParserTest {

	@Test
	public void testParseMessage() {
		Parser testParser = new Parser();
		Map<String,String> msg;
		String connectMessage = "{\"comm_type\" : \"MatchConnectResp\",\"resp\" : \"ok\",\"client_token\" : \"dc8d75a6-448d-4108-8bfa-52470b97f35f\"}";
		if(!testParser.parseMessage(connectMessage))
			fail("Parsing failed");

		msg = testParser.getMessage();
		
		
		
	}

}
