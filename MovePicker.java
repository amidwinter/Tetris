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
			
			int[][] array = boardStateToArray("0000000000000000000000000000000000000000000000c030");
			
			for(int i = 0; i < 20; i++) {
				for(int j = 0; j < 10; j++)
					System.out.print(": " + array[i][j] + " :");
				System.out.println();
			}
			
		}
		catch(org.zeromq.ZMQException e) {
			System.out.println(e);
			return;
		}

	}
	
	/*
	 * Breaks hexadecimal boardState string into a binary string, and then into a 2D array of booleans
	 * 
	 * @parameters boardState - the hexadecimal representation of the board state
	 * @return a 2D boolean array representation of the board state
	 */
	private int[][] boardStateToArray(String boardState) {		
		long boardStateIntValue = Long.parseLong(boardState, 16);
		System.out.println("int: " + boardStateIntValue);
		String boardStateBinaryValue = String.format("%200s", Long.toBinaryString(boardStateIntValue)).replace(' ', '0');
//		String boardStateBinaryValue = Integer.toBinaryString(boardStateIntValue);
		System.out.println("bin: " + boardStateBinaryValue);
		char[] boardStateBinaryValueCharArray = boardStateBinaryValue.toCharArray();
		System.out.println("char: " + new String(boardStateBinaryValueCharArray));
		
		int[][] boardStateArray = new int[20][10];
		
		for(int i = 0; i < 20; i++) {
			for(int j = 0; j < 10; j++) {
				boardStateArray[i][j] = Character.getNumericValue(boardStateBinaryValueCharArray[i + j]);
			}
		}
		
		return boardStateArray;
	}
}
