import org.zeromq.ZMQ;
import java.util.Scanner;
//import Parser.java;

public class Tetris {
	private static String clientToken;
	
	public static void main(String[] args) {	
		String server = args[0];
		String matchToken = args[1];
		
		//create req/resp connection
		String reqRespServer = "tcp://" + server + ":5557";
		ZMQ.Context reqRespContext = ZMQ.context(1);
		ZMQ.Socket reqRespSocket = reqRespContext.socket(ZMQ.REQ);
		
		reqRespSocket.connect(reqRespServer);
		
		//generate connect message
		String matchConnectMessage = "{\"comm_type\" : \"MatchConnect\", \"match_token\" : \"" + matchToken + "\", \"team_name\" : \"Team 70\",\"password\" : \"password\"}";
		
		//send connect message over req/resp channel
		byte[] connectionRequest = matchConnectMessage.getBytes();
		reqRespSocket.send(connectionRequest, 0);
		byte[] connectionResponseByteArray = reqRespSocket.recv(0);
        String connectionResponse = new String(connectionResponseByteArray);
        System.out.println(connectionResponse );
		
        //create pub/sub connection
        String pubSubServer = "tcp://" + server + ":5556";
  		String streamName = "";
  		ZMQ.Context pubSubContext = ZMQ.context(1);
  		ZMQ.Socket pubSubSocket = pubSubContext.socket(ZMQ.SUB);
  		
  		pubSubSocket.connect(pubSubServer);
  		pubSubSocket.subscribe((matchToken).getBytes());
        
  		connectionResponse = connectionResponse.replaceAll("\\s", "");
  		Scanner scanner = new Scanner(connectionResponse);
  		//scanner.useDelimiter("\"client_token\":\"");
  		//System.out.println(scanner.next());
  		scanner.useDelimiter("\"");
  		clientToken = scanner.next();
  		clientToken = scanner.next();
  		clientToken = scanner.next();
  		clientToken = scanner.next();
  		clientToken = scanner.next();
  		clientToken = scanner.next();
  		clientToken = scanner.next();
  		clientToken = scanner.next();
  		System.out.println(clientToken);
  		
        
		//parse connection response to get client token
		//Parser parser = new Parser();
		//boolean success = parser.parseMessage(connectionResponse);
		
		//clientToken = parser.getClientToken();

      	int i = 0;
		//monitor pub/sub waiting for game to start
		while (true) {
			System.out.println();
			System.out.println();
			System.out.println();
		      // Read envelope with match token, do not do anything
		      String matchTokenMessage = new String(pubSubSocket.recv(0));
		      //read envelope with message
		      String message = new String(pubSubSocket.recv(0));
		      System.out.println(message);
		      
		      if(i == 10) {
			      try{
			    	  i=0;
				      String moveString = "{ \"comm_type\" : \"GameMove\", \"client_token\" : \"" + clientToken + "\", \"move\" : \"rrotate\" }"; 
				      System.out.println(moveString);
				      reqRespSocket.send(moveString.getBytes(), 0);
				      byte[] response = reqRespSocket.recv(0);
				      System.out.println("response: " + new String(response));
			      }
			      catch(org.zeromq.ZMQException e) {
			    	  System.out.println(e);
			      }
		      }
		      i++;
		    }
		//loop until game ended
//		while(true) {
//			if(newMessage==true) {
//				//read state channel information (pub/sub)
//				
//				//determine move
//				
//				//create and send move message
//			}
//		}
	}
}
