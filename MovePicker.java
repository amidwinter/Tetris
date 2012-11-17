import org.zeromq.ZMQ;

public class MovePicker extends Thread{
	private String clientToken;
	private ZMQ.Socket reqRespSocket;
	private Board currentBoard;
	private Piece currentPiece;

	public MovePicker(String clientToken, ZMQ.Socket reqRespSocket, Board currentBoard, Piece currentPiece) {
		this.clientToken = clientToken;
		this.reqRespSocket = reqRespSocket;
		this.currentBoard = currentBoard;
		this.currentPiece = currentPiece;
	}

	/*	Generates move, and sends it to the server.
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try{
			String moveString = "{ \"comm_type\" : \"GameMove\", \"client_token\" : \"" + clientToken + "\", \"move\" : \"rrotate\" }"; 
			System.out.println(moveString);
			reqRespSocket.send(moveString.getBytes(), 0);
			byte[] response = reqRespSocket.recv(0);
			System.out.println("response: " + new String(response));
		}
		catch(org.zeromq.ZMQException e) {
			System.out.println(e);
			return;
		}

	}
}
