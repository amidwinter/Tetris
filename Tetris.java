import zmq
import time
    context = zmq.context;

public class Tetris {
	private String clientToken;
	
	public static void main(String[] args) {
		//create pub/sub connection
		String pubSubServer = "";
		ZMQ.Context pubSubContext = ZMQ.Context(1);
		ZMQ.Socket pubSubSocket = ZMQ.Socket(1);
		
		pubSubSocket.connect(pubSubServer);
		
		//create req/resp connection
		String reqRespServer = "";
		ZMQ.Context reqRespContext = ZMQ.Context(1);
		ZMQ.Socket reqRespSocket = ZMQ.Socket(1);
		
		reqRespSocket.connect(reqRespServer);
		
		//generate connect message
		String matchToken = args[0];
		matchConnectMessage = "{
				\"comm_type\" : \"MatchConnect\", 
				\"match_token\" : \"" + matchToken + "\", 
				\"team_name\" : \"team70\",
				\"password\" : \"catz12345\"" +
						"}";
		
		//send connect message over req/resp channel
		byte[] connectRequest = matchConnectMessage.getBytes();
		reqRespSocket.sent(connectRequest, 0);
		
		//receive and store client token
		
		//monitor pub/sub waiting for game to start
		
		//loop until game ended
		while(true) {
			if(newMessage==true) {
				//read state channel information (pub/sub)
				
				//determine move
				
				//create and send move message
			}
		}
	}
}
