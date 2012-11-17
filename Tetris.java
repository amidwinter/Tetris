import org.zeromq.ZMQ;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class Tetris {	
	public static void main(String[] args) {	
		String server = args[0];
		String matchToken = args[1];
		
		String clientToken = "";
		
		Board currentBoard = new Board();
		Piece currentPiece = new Piece();
		Parser parser = new Parser();
		
		//create req/resp connection
		String reqRespServer = "tcp://" + server + ":5557";
		ZMQ.Context reqRespContext = ZMQ.context(1);
		ZMQ.Socket reqRespSocket = reqRespContext.socket(ZMQ.REQ);
		reqRespSocket.connect(reqRespServer);
		
		//generate connect message
		String matchConnectMessage = "{\"comm_type\" : \"MatchConnect\", \"match_token\" : \"" + matchToken + "\", \"team_name\" : \"Team 70\",\"password\" : \"password\"}";
		
		int i = 0;
		boolean connected = false;
		while(!connected) {
			//send connect message over req/resp channel
			byte[] connectionRequest = matchConnectMessage.getBytes();
			reqRespSocket.send(connectionRequest, 0);
			byte[] connectionResponseByteArray = reqRespSocket.recv(0);
	        String connectionResponse = new String(connectionResponseByteArray);
	        System.out.println(connectionResponse );
			
			//parse connection response to get client token
			boolean success = parser.parseMessage(connectionResponse);
	        if(success == false){
	        	System.out.println("Error connecting to match with matchToken = " + matchToken + " on server = " + server + "! Trying again!\n");
	        	
	        	if(i > 10) {
	        		System.err.println("Could not connect to server " + server + " with matchToken = " + matchToken);
	        		System.exit(1);
	        	}
	        }
	        else {
	        	connected = true;
	        	Map<String, String> connectionResponseMap = parser.getMessage();
	        	clientToken = connectionResponseMap.get("client_token");
	        }
	        
	        i++;
		}
        
        System.out.println("client token: " + clientToken);
        
        //create pub/sub connection
        String pubSubServer = "tcp://" + server + ":5556";
  		String streamName = "";
  		ZMQ.Context pubSubContext = ZMQ.context(1);
  		ZMQ.Socket pubSubSocket = pubSubContext.socket(ZMQ.SUB);
  		
  		pubSubSocket.connect(pubSubServer);
  		pubSubSocket.subscribe((matchToken).getBytes());  		

  		
  		MovePicker movePicker = null;
      	boolean matchEnded = false;
		//monitor pub/sub waiting for game to start
		while (!matchEnded) {
			System.out.println();
			System.out.println();
			// Read envelope with match token, do not do anything
			String matchTokenMessage = new String(pubSubSocket.recv(0));
			//read envelope with message
			String pubSubMessage = new String(pubSubSocket.recv(0));
			System.out.println("PubSub: " + pubSubMessage);		      
//			boolean success = parser.parseMessage(pubSubMessage);
//			Map <String, String> pubSubMessageMap = parser.getMessage();

			//if comm type is GameBoardState, set new values for board state and check if changes have happened
			//else if comm type is GamePieceState, set new values for piece state and check if changes have happened
			String pubSubCommType = "GameBoardState";//pubSubMessageMap.get("comm_type");
			if(pubSubCommType.equals("GameBoardState")) {
//				int boardState = Integer.parseInt(pubSubMessageMap.get("board_state"));
//				int pieceNumber = Integer.parseInt(pubSubMessageMap.get("piece_number"));
//				String clearedRows = pubSubMessageMap.get("cleared_rows");

				//if game board has changed, kill movePicker thread (if one exists) and create a new one. 
				//Also, set new values for board
//				if(currentBoard.hasChanged(boardState, pieceNumber)) {
					//set new values for board
//					currentBoard.setBoard(boardState, pieceNumber, clearedRows);
					
					//if movePicker thread exists, kill it
					if(movePicker != null && movePicker.isAlive()) {
						movePicker.interrupt();
					}
					
					//create new movePicker thread
					movePicker = new MovePicker(clientToken, reqRespSocket, currentBoard, currentPiece);
					movePicker.run();
//				}
			}
			//else if comm type is GamePieceState, set new values for piece state and check if changes have happened
			else if(pubSubCommType.equals("GamePieceState")) {
//				int orientation = Integer.parseInt(pubSubMessageMap.get("orientation"));
//				String piece = pubSubMessageMap.get("piece");
//				int number = Integer.parseInt(pubSubMessageMap.get("number"));
//				int row = Integer.parseInt(pubSubMessageMap.get("row"));
//				int col = Integer.parseInt(pubSubMessageMap.get("col"));
//
//				//if piece has changed, kill movePicker thread (if one exists) and create new one.
//				//Also, set new values for piece
//				if(currentPiece.hasChanged(orientation, piece, number, row, col)) {
//					//set new values for piece
//					currentPiece.setPiece(orientation, piece, number, row, col);
					
					//if movePicker thread exists, kill it
					if(movePicker != null && movePicker.isAlive()) {
						movePicker.interrupt();
					}
					
					//create new movePicker thread
					movePicker = new MovePicker(clientToken, reqRespSocket, currentBoard, currentPiece);
					movePicker.run();
//				}
			}
			//else if comm type is GameEnd, kill movePicker thread (if one exists)
			else if(pubSubCommType.equals("GameEnd")) {
				//if movePicker thread exists, kill it
				if(movePicker != null && movePicker.isAlive())
					movePicker.interrupt();
				
				continue;
			}
			//else if comm type is MatchEnd, kill movePicker thread (if one exists) and set matchEnded to true;
			else if(pubSubCommType.equals("MatchEnd")) {
//				System.out.println("Match Ended! Status: " + pubSubMessageMap.get("status"));

				//if movePicker thread exists, kill it
				if(movePicker != null && movePicker.isAlive())
					movePicker.interrupt();
				
				matchEnded = true;

				continue;
			}
			//else, restart loop (there was probably some error in pubSub message)
			else {
				continue;
			}
		}
	}
}
