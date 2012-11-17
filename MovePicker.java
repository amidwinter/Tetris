import org.zeromq.ZMQ;
import java.math.BigInteger;

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
			String boardStateString = currentBoard.getBoardState();
			int[][] boardStateArray = boardStateToArray(boardStateString);
			String move = determineMoveBasic(boardStateArray);
			String moveString = "{ \"comm_type\" : \"GameMove\", \"client_token\" : \"" + clientToken + "\", \"move\" : \"" + move + "\" }"; 
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

	/*
	 * Breaks hexadecimal boardState string into a binary string, and then into a 2D array of booleans
	 * 
	 * @parameters boardState - the hexadecimal representation of the board state
	 * @return a 2D boolean array representation of the board state
	 */
	private int[][] boardStateToArray(String boardState) {
		//convert hex to bigInteger
		BigInteger boardStateIntValue = new BigInteger(boardState, 16);

		//convert bigInteger to binary string
		String boardStateBinaryValue = String.format("%200s", boardStateIntValue.toString(2)).replace(' ', '0');

		//convert binary string to binary char array
		char[] boardStateBinaryValueCharArray = boardStateBinaryValue.toCharArray();

		int[][] boardStateArray = new int[20][10];

		//convert binary char array to 2D array of integer values
		for(int i = 0; i < 20; i++) {
			for(int j = 0; j < 10; j++) {
				boardStateArray[i][j] = Character.getNumericValue(boardStateBinaryValueCharArray[i*10 + j]);
			}
		}

		return boardStateArray;
	}

	private String determineMoveBasic(int[][] boardState) {	
		boolean continueCheckingBoard = true;
		String move = "drop";
		for(int boardY = 19; boardY >=0 && continueCheckingBoard; boardY--) {
			for(int boardX = 0; boardX < 10; boardX++) {
				int cell = boardState[boardY][boardX];
				if(cell == 0) {
					int[][] currentPieceMask = currentPiece.getMask();
					boolean continueCheckingMask = true;
					int maskY = 0;
					int maskX = 0;
					for(maskY = 0; maskY < 4 && continueCheckingMask; maskY++) {
						for(maskX = 0; maskX < 4 && continueCheckingMask; maskX++) {
							int maskCell = currentPieceMask[maskY][maskX];
							if(maskCell != 0) {
								//these are the coordinates relative to the origin of the cell of the mask currently being checked
								int maskDeltaY = maskY - 1;
								int maskDeltaX = maskX - 2;
								
								//these are the coordinates of the position on the board that the cell of the piece mask that is currently being checked would occupy
								int boardDeltaY = boardY + maskDeltaY;
								int boardDeltaX = boardX + maskDeltaX;
								
								if(boardDeltaY < 0 || boardDeltaY > 19 || boardDeltaX < 0 || boardDeltaX > 19) {
									//piece would be out of bounds
									continueCheckingMask = false;
									break;
								}
								else if(boardState[boardDeltaY][boardDeltaX] == 1) {
									//board cell is occupied already
									continueCheckingMask = false;
									break;
								}
							}
						}
					}
					if(maskY == 4 && maskX == 4) {
						//have gone through whole mask without stopping checking (have found an empty spot)
						
						int currentPiecePositionY = currentPiece.getRow();
						int currentPiecePositionX = currentPiece.getCol();
						
						if(currentPiecePositionX == boardX) {
							//piece is directly on top of desired position
							move = "drop";
						}
						else if(currentPiecePositionX - boardX > 0) {
							//piece is currently to the right of the desired position
							move = "left";
						}
						else if(currentPiecePositionX - boardX < 0) {
							//piece is currently to the left of the desired position
							move = "right";
						}
						
						continueCheckingBoard = false;
						break;
					}
				}
			}
		}
		return move;
	}
}
