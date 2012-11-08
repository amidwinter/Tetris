import org.zeromq.ZMQ;

public class Tetris {
	private String clientToken;
	
	public static void main(String[] args) {
		//create pub/sub connection
		String pubSubServer = "ec2-54-242-48-216.compute-1.amazonaws.com";
		String streamName = "";
		ZMQ.Context pubSubContext = ZMQ.context(1);
		ZMQ.Socket pubSubSocket = pubSubContext.socket(ZMQ.SUB);
		
		pubSubSocket.connect(pubSubServer);
		pubSubSocket.subscribe(streamName.getBytes());
		//create req/resp connection
		String reqRespServer = "ec2-54-242-48-216.compute-1.amazonaws.com";
		ZMQ.Context reqRespContext = ZMQ.context(1);
		ZMQ.Socket reqRespSocket = reqRespContext.socket(ZMQ.REQ);
		
		reqRespSocket.connect(reqRespServer);
		
		//generate connect message
		String matchToken = "400bca5a-6283-4355-a57a-507238d21479";
		String matchConnectMessage = "{\"comm_type\" : \"MatchConnect\", \"match_token\" : \"" + matchToken + "\", \"team_name\" : \"team70\",\"password\" : \"catz12345\"}";
		
		//send connect message over req/resp channel
		byte[] connectRequest = matchConnectMessage.getBytes();
		reqRespSocket.send(connectRequest, 0);
		
		//receive and store client token
		
		//monitor pub/sub waiting for game to start
		while (true) {
		      // Read envelope with address
		      String address = new String(pubSubSocket.recv(0));
		      // Read message contents
		      String contents = new String(pubSubSocket.recv(0));
		      System.out.println(address + " : " + contents);
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
