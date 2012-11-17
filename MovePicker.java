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

			if(currentBoard.isSet() && currentPiece.isSet()) {
				System.out.println("\n\nRUNNING\n\n");
				String boardStateString = currentBoard.getBoardState();
				int[][] boardStateArray = boardStateToArray(boardStateString);
				String move = determineMoveBasic(boardStateArray);
				String moveString = "{ \"comm_type\" : \"GameMove\", \"client_token\" : \"" + clientToken + "\", \"move\" : \"" + move + "\" }"; 
				System.out.println(moveString);
				reqRespSocket.send(moveString.getBytes(), 0);
				byte[] response = reqRespSocket.recv(0);
				System.out.println("response: " + new String(response));
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
								
								if(boardDeltaY < 0 || boardDeltaY > 19 || boardDeltaX < 0 || boardDeltaX > 9) {
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
	
	
	public String determineMoveWeighted(int[][]boardState)
	{
		//default move
		String move = "drop";
		//Note: focus on left/right justification initially (left before right)
			//Remember to check legality of every move
		int[][] possibleMove = new int[20][10]; 
		int[][] currentPieceMask = currentPiece.getMask();
		
		//Determine weighted board
		double [][]weightedBoard = getWeightedBoard(boardState);
		//First, look at bottom available row
			//TODO: Determine availability: Check if row covered, check holes(not sure if needed yet) - method below should do this
		int lowestBoardRow = determineLowestAvailableBoardRow(boardState); // (actually the highest board #)
		
		//Second, determine what moves can be made on bottom row
		//Determine first row of pieceMask containing a 1
		int lowestPieceRow = determineLowestAvailablePieceRow(currentPieceMask);
		//Determine first column of pieceMask containing a 1
		int lowestPieceCol = determineLowestAvailablePieceCol(currentPieceMask);
		
		int pieceRow = lowestPieceRow;
		int pieceCol = lowestPieceCol;
		int boardRow = lowestBoardRow;
		
		boolean feasibleMove = false;
		int i = 0;
		
		//Creating an 
		int []possibleMoves = new int[10];
		
		
		
		
		while(i < 10)
		{
			if(currentPieceMask[pieceRow][pieceCol] + boardState[boardRow][i] == 1)
			{
				//Save the cell this move was made in
				possibleMove[lowestBoardRow][i] = 1;
				feasibleMove = true;
				//check if the piece has a 1 to its right
				if(currentPieceMask[pieceRow][pieceCol + 1] == 1)
				{
					if(currentPieceMask[pieceRow][pieceCol + 1] + boardState[boardRow][i + 1] == 1)
					{
						//Save the cell this move was made in
						possibleMove[lowestBoardRow][i + 1] = 1;
						//both cells on this row fit...move up a row
						
					}
					else
					{
						feasibleMove = false;
						//There was something in the way. This move will not work
					}
				}
				//If "if" statement not triggered, only 1 cell on this bottom row of piece...move up a row
				
			}
		}
		
			//Third, determine what moves (in relation to first move) can be made on second row
		
			//Next row (if applicable)
		
			//Next row (if applicable)
		
		//Whenever a valid move is determined, save it. Give it a score using weightedBoard, then continue until all possible moves are made
		
		//Reorient piece, repeat
		
		
		return move;
	}
	
	public double[][] getWeightedBoard(int[][] boardState)
	{
		double [][] weightedBoard = new double[20][10];
		double wallWeight = 1;
		double holeWeight = -1;
		double bordersBlockWeight = 1;
		double potentialHoleWeight = 1;
		double wouldCreateHoleWeight = -1;
		double heightWeight = 0.25;
		
		for(int row = 19; row >= 0; row--) {
			for(int col = 0; col < 10; col++) {
				double totalWeight = 0;
				
				//if it borders a wall
				if(bordersWall(col)) {
					totalWeight += wallWeight;
				}
				
				int isHoleOrPotentialHole = isHoleOrPotentialHole(row, col, boardState);
				
				//if is hole
				if(isHoleOrPotentialHole == 1) {
					totalWeight += holeWeight;
				}
				//if is potential hole
				else if(isHoleOrPotentialHole == 2) {
					totalWeight += potentialHoleWeight;
				}
				//else if borders block
				else if(bordersBlock(row, col, boardState)) {
					totalWeight += bordersBlockWeight;
				}
				
				//if filling it would create a hole
				if(row < 19) //can't create hole if it is in the bottom row
					if(wouldCreateHole(row, col, boardState))
						totalWeight += wouldCreateHoleWeight;
				
				//calculate height weight
				double cellHeightWeight = (row - 10) * heightWeight;
				totalWeight += cellHeightWeight;
				
				weightedBoard[row][col] = totalWeight;
			}
		}
	}
	
	/*
	 * Determines if a cell borders a wall
	 * @returns true if yes, false if not
	 */
	private boolean bordersWall(int col) {
		if(col == 0 || col == 9) {
			return true;
		}
		else
			return false;
	}
	
	private boolean bordersBlock(int row, int col, int[][] boardState) {
		boolean cellRightFilled = false;
		boolean cellLeftFilled = false;
		
		//left
		if(col > 0) {
			int cellLeftRow = row;
			int cellLeftCol = col - 1;
			int cellLeft = boardState[cellLeftRow][cellLeftCol];
			if(cellLeft == 1) 
				cellLeftFilled = true;
		}
		
		//right
		if(col < 9) {
			int cellRightRow = row;
			int cellRightCol = col + 1;
			int cellRight = boardState[cellRightRow][cellRightCol];
			if(cellRight == 1)
				cellRightFilled = true;
		}
		
		if(cellLeftFilled || cellRightFilled)
			return true;
		else
			return false;
		
	}
	
	/*
	 * determines if a cell is a hole, or will potentially become a hole
	 * 
	 * @returns 0 if not hole or potential hole, 1 if hole, 2 if potential hole (both sides filled, but not above
	 */
	private int isHoleOrPotentialHole(int row, int col, int[][] boardState) {
		boolean aboveFilled = false;
		boolean leftFilled = false;
		boolean rightFilled = false;
		
		//above
		if(row != 0) {
			int cellAboveRow = row - 1;
			int cellAboveCol = col;
			int cellAbove = boardState[cellAboveRow][cellAboveCol];
			if(cellAbove == 1)
				aboveFilled = true;
		}
		else
			aboveFilled = true;
		
		//left
		if(col != 0) {
			int cellLeftRow = row;
			int cellLeftCol = col - 1;
			int cellLeft = boardState[cellLeftRow][cellLeftCol];
			if(cellLeft == 1)
				leftFilled = true;
		}
		else
			leftFilled = true;
		
		//right
		if(col != 9) {
			int cellRightRow = row;
			int cellRightCol = col + 1;
			int cellRight = boardState[cellRightRow][cellRightCol];
			if(cellRight == 1)
				rightFilled = true;
		}
		else
			rightFilled = true;
		
		if(aboveFilled && leftFilled && rightFilled)
			return 1;
		else if(!aboveFilled && leftFilled && rightFilled)
			return 2;
		else
			return 0;
	}
	
	/*
	 * determines if filling the selected cell would create a hole
	 * @returns true if yes, false if no.
	 */
	private boolean wouldCreateHole(int row, int col, int[][] boardState) {
		boolean rightFilled = false;
		boolean leftFilled = false;
		
		int cellBelowRow = row + 1;
		int cellBelowCol = col;
		
		//cell to the right of cell below
		if(cellBelowCol < 9) {
			int cellBelowRightCol = cellBelowCol + 1;
			int cellBelowRightRow = cellBelowRow;
			int cellBelowRight = boardState[cellBelowRightRow][cellBelowRightCol];
			if(cellBelowRight == 1) {
				rightFilled = true;
			}
		}
		else 
			rightFilled = true;
		
		//cell to the left of cell below
		if(cellBelowCol > 0) {
			int cellBelowLeftCol = cellBelowCol - 1;
			int cellBelowLeftRow = cellBelowRow;
			int cellBelowLeft = boardState[cellBelowLeftRow][cellBelowLeftCol];
			if(cellBelowLeft == 1) {
				leftFilled = true;
			}
		}
		else 
			leftFilled = true;
		
		if(rightFilled && leftFilled)
			return true;
		else
			return false;
	}
	
	public int determineLowestAvailableBoardRow(int[][] boardState)
	{
		int lowestRow = 19;
		// Start at bottom(highest #) row, check if there are empty cells
			//if there are empty cells, check if they are blocked above ie check whether it is possible to put something there
				//TODO: determine criteria for "possible to put something there"
		
		//This method should take into account the spacing for the bottom row in relation to current piece + its orientation
			//Only return a lowest row that is a possible fit for the current piece
		
		return lowestRow;
	}
	
	public int determineLowestAvailablePieceRow(int[][] pieceMask)
	{
		/*	
		* 	0 1 2 3
		*	1   X
		*	2
		*	3
		*
		*	[row][column] ==> [1][2]
		*/
		int lowestRow = 0;
		
		return lowestRow;
	}
	
	public int determineLowestAvailablePieceCol(int[][] pieceMask)
	{
		/*	
		* 	0 1 2 3
		*	1
		*	2
		*	3
		*/
		int lowestCol = 0;
		
		return lowestCol;
	}
}
