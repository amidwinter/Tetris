import org.zeromq.ZMQ;
import java.math.*;

public class MovePicker extends Thread{
	
	public class Move
	{
		public int []row;
		public int []col;
		public int orient;
		public int centerCol;
		public int centerRow;
		double score;
		public Move(int []Arow, int []Acol, int Aorient)
		{
			row = Arow;
			col = Acol;
			orient = Aorient;
			score = 0;
		}
	}
	
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
				String move = determineMoveWeightedNew(boardStateArray);
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
	
	public String determineMoveWeightedNew(int[][] boardState) {
		String move = "drop";
		
		int[][] moves = determineLowestAvailableMoves(boardState, currentPiece);
		double[] moveScores = new double[100];
		double[][] weightedBoard = getWeightedBoard(boardState);
		
		int[][] pieceDeltas = getPieceCellDeltas(currentPiece);
		int[] pieceDeltasRow = pieceDeltas[0];
		int[] pieceDeltasCol = pieceDeltas[1];
		
		for(int i = 0; i < moves.length; i++) {
			//if(moves[0][i] == null || moves[1][i] == null) 
			//	break;
			//else {
				for(int j = 0; j < 3; j++) {
					int pieceOnBoardRow = moves[0][i] + pieceDeltasRow[j];
					int pieceOnBoardCol = moves[1][i] + pieceDeltasCol[j];
					
					if(pieceOnBoardRow < 0 || pieceOnBoardRow > 19 || pieceOnBoardCol < 0 || pieceOnBoardCol > 9) {
						break;
					}
					else {
						int pieceOnBoard = boardState[pieceOnBoardRow][pieceOnBoardCol];
						moveScores[i] = weightedBoard[pieceOnBoardRow][pieceOnBoardCol];
					}
				}
			//}
		}
		
		double bestScore = 0;
		int bestMove = 0;
		for(int i = 0; i < moves.length; i++) {
			//if(moves[0][i] == null || moves[1][i] == null || moveScores[i] == null)
			//	break;
			if(moveScores[i] > bestScore) {
				bestScore = moveScores[i];
				bestMove = i;
			}
		}
		String moveString = "down";
		int currentPieceCol = currentPiece.getCol();
		if(moves[1][bestMove] < currentPieceCol)
			//move is left of piece
			moveString = "left";
		else if(moves[1][bestMove] > currentPieceCol)
			//move is right of piece
			moveString = "right";
		else if(moves[1][bestMove] == currentPieceCol)
			moveString = "drop";
		
		return moveString;
	}
	
	
	
	public boolean checkLegality(int boardRow, int boardCol, int[][] boardState)
	{
		if(boardRow > 19 || boardRow < 0)
			return false;
		else if(boardCol > 9 || boardCol < 0)
			return false;
		else if(boardState[boardRow][boardCol] != 0)
			return false;
		else
			return true;
	}
	
	public double[][] getWeightedBoard(int[][] boardState)
	{
		double [][] weightedBoard = new double[20][10];
		double wallWeight = 1;
		double holeWeight = -25;
		double bordersBlockWeight = 2;
		double potentialHoleWeight = 1;
		double wouldCreateHoleWeight = -5;
		double heightWeight = 1;
		double floorWeight = 3;
		
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
				
				if(bordersFloor(col)) {
					totalWeight += floorWeight;
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
		return weightedBoard;
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
	
	private boolean bordersFloor(int row) {
		if(row == 19)
			return true;
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
	
	public int[][] determineLowestAvailableMoves(int[][] boardState, Piece piece)
	{
		int[][] moves = new int[2][100];
		int movesIndex = 0;
		// Start at bottom(highest #) row, check if there are empty cells
		
		int[][] pieceCellDeltas = getPieceCellDeltas(piece);
		int[] pieceCellDeltasRow = pieceCellDeltas[0];
		int[] pieceCellDeltasCol = pieceCellDeltas[1];
		
		boolean foundLowestRow = false;
		
		for(int row = 19; row >= 0 && !foundLowestRow; row--) {
			for(int col = 0; col < 10; col++) {
				if(boardState[row][col] == 0) {					
					int i;
					for(i = 0; i < 3; i++) {
						int pieceCellOnBoardRow = row + pieceCellDeltasRow[i];
						int pieceCellOnBoardCol = col + pieceCellDeltasCol[i];
						
						if(pieceCellOnBoardRow < 0 || pieceCellOnBoardRow > 19 || pieceCellOnBoardCol < 0 || pieceCellOnBoardCol > 9) {
							foundLowestRow = false;
							break;
						}
						else {
							int pieceCellOnBoard = boardState[pieceCellOnBoardRow][pieceCellOnBoardCol];
							if(pieceCellOnBoard == 1) {
								foundLowestRow = false;
								break;
							}
						}
					}
					if(i == 3) {
						//if it gets here, it means that this is the lowest available row
						moves[0][movesIndex] = row;
						moves[1][movesIndex] = col;
						movesIndex++;
					}
				}
			}
		}
			//if there are empty cells, check if they are blocked above ie check whether it is possible to put something there
				//TODO: determine criteria for "possible to put something there"
		
		//This method should take into account the spacing for the bottom row in relation to current piece + its orientation
			//Only return a lowest row that is a possible fit for the current piece
		
		return moves;
	}
	
	private int[][] getOrigin(Piece piece) {
		int[] origin = new int[2];
		if(piece.getPiece().equals("O"))
		{
			origin[0] = -1;
			origin[1] = 1;
		}
		else if(piece.getPiece().equals("I"))
		{
			if(piece.getOrientation() == 0)
			{			
				origin[0] = 0;
				origin[1] = 2;
			}
			else if(piece.getOrientation() == 1)
			{
				origin[0] = -2;
				origin[1] = 0;
			}
		}
		else if(piece.getPiece().equals("S"))
		{
			if(piece.getOrientation()== 0)
			{
				origin[0] = -1;
				origin[1] = 1;
			}
			else if(piece.getOrientation() == 1)
			{
				origin[0] = -1;
				origin[1] = -1;
			}
		}
		else if(piece.getPiece().equals("Z"))
		{
			if(piece.getOrientation() == 0)
			{
				origin[0] = 0;
				origin[1] = 1;
			}
			else if(piece.getOrientation() == 1)
			{
				origin[0] = -1;
				origin[1] = 1;
			}
		}
		else if(piece.getPiece().equals("L"))
		{
			if(piece.getOrientation() == 0)
			{
				origin[0] = -1;
				origin[1] = 1;
			}
			else if(piece.getOrientation() == 1)
			{
				origin[0] = -1;
				origin[1] = 0;
			}
			else if(piece.getOrientation() == 2)
			{
				origin[0] = 0;
				origin[1] = 1;
			}
			else if(piece.getOrientation() == 3)
			{
				origin[0] = -1;
				origin[1] = 0;
			}
		}
		else if(piece.getPiece().equals("J"))
		{
			if(piece.getOrientation() == 0)
			{
				origin[0] = -1;
				origin[1] = -1;
			}
			else if(piece.getOrientation() == 1)
			{
				origin[0] = -1;
				origin[1] = 0;
			}
			else if(piece.getOrientation() == 2)
			{
				origin[0] = 0;
				origin[1] = 1;
			}
			else if(piece.getOrientation() == 3)
			{
				origin[0] = -1;
				origin[1] = 1;
			}
		}
		else if(piece.getPiece().equals("T"))
		{
			if(piece.getOrientation() == 0)
			{
				origin[0] = -1;
				origin[1] = 0;
			}
			else if(piece.getOrientation() == 1)
			{
				origin[0] = -1;
				origin[1] = 0;
			}
			else if(piece.getOrientation() == 2)
			{
				origin[0] = 0;
				origin[1] = 1;
			}
			else if(piece.getOrientation() == 3)
			{
				origin[0] = -1;
				origin[1] = 0;
			}
		}
		
		return origin;
	}
	
	private int[][] getPieceCellDeltas(Piece piece) {
		int[] pieceCellDeltasRow = new int[3];	//array of ints that specifies the positions of the filled cells of the piece relative to the bottom-left-most cell
		int[] pieceCellDeltasCol = new int[3];
		if(piece.getPiece().equals("O"))
		{
			pieceCellDeltasRow[0] = -1;
			pieceCellDeltasCol[0] = 0;
			pieceCellDeltasRow[1] = -1;
			pieceCellDeltasCol[1] = 1;
			pieceCellDeltasRow[2] = 0;
			pieceCellDeltasCol[2] = 1;
		}
		else if(piece.getPiece().equals("I"))
		{
			if(piece.getOrientation() == 0)
			{
				pieceCellDeltasRow[0] = 0;
				pieceCellDeltasCol[0] = 1;
				pieceCellDeltasRow[1] = 0;
				pieceCellDeltasCol[1] = 2;
				pieceCellDeltasRow[2] = 0;
				pieceCellDeltasCol[2] = 3;				
			}
			else if(piece.getOrientation() == 1)
			{
				pieceCellDeltasRow[0] = -3;
				pieceCellDeltasCol[0] = 0;
				pieceCellDeltasRow[1] = -2;
				pieceCellDeltasCol[1] = 0;
				pieceCellDeltasRow[2] = -1;
				pieceCellDeltasCol[2] = 0;
			}
		}
		else if(piece.getPiece().equals("S"))
		{
			if(piece.getOrientation()== 0)
			{
				pieceCellDeltasRow[0] = -1;
				pieceCellDeltasCol[0] = 1;
				pieceCellDeltasRow[1] = -1;
				pieceCellDeltasCol[1] = 2;
				pieceCellDeltasRow[2] = 0;
				pieceCellDeltasCol[2] = 1;
			}
			else if(piece.getOrientation() == 1)
			{
				pieceCellDeltasRow[0] = -1;
				pieceCellDeltasCol[0] = -1;
				pieceCellDeltasRow[1] = -2;
				pieceCellDeltasCol[1] = -1;
				pieceCellDeltasRow[2] = -1;
				pieceCellDeltasCol[2] = 0;
			}
		}
		else if(piece.getPiece().equals("Z"))
		{
			if(piece.getOrientation() == 0)
			{
				pieceCellDeltasRow[0] = -1;
				pieceCellDeltasCol[0] = -1;
				pieceCellDeltasRow[1] = -1;
				pieceCellDeltasCol[1] = 0;
				pieceCellDeltasRow[2] = 0;
				pieceCellDeltasCol[2] = 1;
			}
			else if(piece.getOrientation() == 1)
			{
				pieceCellDeltasRow[0] = -1;
				pieceCellDeltasCol[0] = 0;
				pieceCellDeltasRow[1] = -1;
				pieceCellDeltasCol[1] = 1;
				pieceCellDeltasRow[2] = -2;
				pieceCellDeltasCol[2] = 1;
			}
		}
		else if(piece.getPiece().equals("L"))
		{
			if(piece.getOrientation() == 0)
			{
				pieceCellDeltasRow[0] = -1;
				pieceCellDeltasCol[0] = 0;
				pieceCellDeltasRow[1] = -1;
				pieceCellDeltasCol[1] = 1;
				pieceCellDeltasRow[2] = -1;
				pieceCellDeltasCol[2] = 2;
			}
			else if(piece.getOrientation() == 1)
			{
				pieceCellDeltasRow[0] = -1;
				pieceCellDeltasCol[0] = 0;
				pieceCellDeltasRow[1] = -2;
				pieceCellDeltasCol[1] = 0;
				pieceCellDeltasRow[2] = 0;
				pieceCellDeltasCol[2] = 1;
			}
			else if(piece.getOrientation() == 2)
			{
				pieceCellDeltasRow[0] = 0;
				pieceCellDeltasCol[0] = 1;
				pieceCellDeltasRow[1] = 0;
				pieceCellDeltasCol[1] = 2;
				pieceCellDeltasRow[2] = -1;
				pieceCellDeltasCol[2] = 2;
			}
			else if(piece.getOrientation() == 3)
			{
				pieceCellDeltasRow[0] = -2;
				pieceCellDeltasCol[0] = -1;
				pieceCellDeltasRow[1] = -2;
				pieceCellDeltasCol[1] = 0;
				pieceCellDeltasRow[2] = -1;
				pieceCellDeltasCol[2] = 0;
			}
		}
		else if(piece.getPiece().equals("J"))
		{
			if(piece.getOrientation() == 0)
			{
				pieceCellDeltasRow[0] = -1;
				pieceCellDeltasCol[0] = -2;
				pieceCellDeltasRow[1] = -1;
				pieceCellDeltasCol[1] = -1;
				pieceCellDeltasRow[2] = -1;
				pieceCellDeltasCol[2] = 0;
			}
			else if(piece.getOrientation() == 1)
			{
				pieceCellDeltasRow[0] = -1;
				pieceCellDeltasCol[0] = 0;
				pieceCellDeltasRow[1] = -2;
				pieceCellDeltasCol[1] = 0;
				pieceCellDeltasRow[2] = -2;
				pieceCellDeltasCol[2] = 1;
			}
			else if(piece.getOrientation() == 2)
			{
				pieceCellDeltasRow[0] = -1;
				pieceCellDeltasCol[0] = 0;
				pieceCellDeltasRow[1] = 0;
				pieceCellDeltasCol[1] = 1;
				pieceCellDeltasRow[2] = 0;
				pieceCellDeltasCol[2] = 2;
			}
			else if(piece.getOrientation() == 3)
			{
				pieceCellDeltasRow[0] = 0;
				pieceCellDeltasCol[0] = 1;
				pieceCellDeltasRow[1] = -1;
				pieceCellDeltasCol[1] = 1;
				pieceCellDeltasRow[2] = -2;
				pieceCellDeltasCol[2] = 1;	
			}
		}
		else if(piece.getPiece().equals("T"))
		{
			if(piece.getOrientation() == 0)
			{
				pieceCellDeltasRow[0] = -1;
				pieceCellDeltasCol[0] = -1;
				pieceCellDeltasRow[1] = -1;
				pieceCellDeltasCol[1] = 0;
				pieceCellDeltasRow[2] = -1;
				pieceCellDeltasCol[2] = 1;
			}
			else if(piece.getOrientation() == 1)
			{
				pieceCellDeltasRow[0] = -1;
				pieceCellDeltasCol[0] = 0;
				pieceCellDeltasRow[1] = -2;
				pieceCellDeltasCol[1] = 0;
				pieceCellDeltasRow[2] = -1;
				pieceCellDeltasCol[2] = 1;
			}
			else if(piece.getOrientation() == 2)
			{
				pieceCellDeltasRow[0] = 0;
				pieceCellDeltasCol[0] = 1;
				pieceCellDeltasRow[1] = 0;
				pieceCellDeltasCol[1] = 2;
				pieceCellDeltasRow[2] = -1;
				pieceCellDeltasCol[2] = 1;
			}
			else if(piece.getOrientation() == 3)
			{
				pieceCellDeltasRow[0] = -1;
				pieceCellDeltasCol[0] = -1;
				pieceCellDeltasRow[1] = -1;
				pieceCellDeltasCol[1] = 0;
				pieceCellDeltasRow[2] = -2;
				pieceCellDeltasCol[2] = 0;
			}
		}
		
		int[][] pieceCellDeltas = new int[2][3];
		pieceCellDeltas[0] = pieceCellDeltasRow;
		pieceCellDeltas[1] = pieceCellDeltasCol;
		
		return pieceCellDeltas;
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
