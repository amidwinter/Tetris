import org.zeromq.ZMQ;

public class Tetris {
	private String clientToken;
	
	public static void main(String[] args) {
		//create pub/sub connection
		String pubSubServer = "tcp://ec2-54-242-48-216.compute-1.amazonaws.com:5557";
		String streamName = "";
		ZMQ.Context pubSubContext = ZMQ.context(1);
		ZMQ.Socket pubSubSocket = pubSubContext.socket(ZMQ.SUB);
		
		pubSubSocket.connect(pubSubServer);
		pubSubSocket.subscribe("B".getBytes());
		//create req/resp connection
		String reqRespServer = "tcp://ec2-54-242-48-216.compute-1.amazonaws.com:5557";
		ZMQ.Context reqRespContext = ZMQ.context(1);
		ZMQ.Socket reqRespSocket = reqRespContext.socket(ZMQ.REQ);
		
		reqRespSocket.connect(reqRespServer);
		
		//generate connect message
		String matchToken = "ae63b992-9d28-4ccf-98d4-a1d37cb95595";
		String matchConnectMessage = "{\"comm_type\" : \"MatchConnect\", \"match_token\" : \"" + matchToken + "\", \"team_name\" : \"Just Two Guys\",\"password\" : \"catz54321\"}";
		
		//send connect message over req/resp channel
		byte[] connectRequest = matchConnectMessage.getBytes();
		reqRespSocket.send(connectRequest, 0);
		byte[] reply = reqRespSocket.recv(0);
        String replyValue = new String(reply);
        System.out.println("Received reply: " + replyValue );
		
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
