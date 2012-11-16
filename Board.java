/*
 * Stores information about the board (supplied by pubSub connection)
 */
public class Board {
	private int boardState;
	private int pieceNumber;
	private String clearedRows;
	
	public Board() {
		boardState = 0;
		pieceNumber = 0;
		clearedRows = null;
	}
	
	public Board(int boardState, int pieceNumber, String clearedRows) {
		this.boardState = boardState;
		this.pieceNumber = pieceNumber;
		this.clearedRows = clearedRows;
	}
	
	/*	Compares existing values to values passed as parameters. 
	 * 	@params int boardState: board state (hex) passed as parameter
	 * 			int pieceNumber: piece number passed as parameter
	 * 	@returns True, if board state or pieceNumber have changed
	 * 	@returns False, if board state and pieceNumber have not changed
	 */
	public boolean hasChanged(int boardState, int pieceNumber) {
		if(boardState != this.boardState)
			return true;
		else if(pieceNumber != this.pieceNumber) 
			return true;
		else
			return false;
	}
	
	// Sets the board data to data specified by the parameters
	public void setBoard(int boardState, int pieceNumber, String clearedRows) {
		this.boardState = boardState;
		this.pieceNumber = pieceNumber;
		this.clearedRows = clearedRows;
	}
}
