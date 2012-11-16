/*
 * Stores information about the current piece (supplied by pubSub connection)
 */
public class Piece {
	private int orientation;
	private String piece;
	private int number;
	private int row;
	private int col;
	
	public Piece() {
		orientation = 0;
		piece = " ";
		number = 0;
		row = 0;
		col = 0;
	}
	
	public Piece(int orientation, String piece, int number, int row, int col) {
		this.orientation = orientation;
		this.piece = piece;
		this.number = number;
		this.row = row;
		this.col = col;
	}
	
	/*	Compares existing values to values passed as parameters. 
	 * 	@params int orientation: orientation passed as parameter
	 * 			char piece: piece passed as parameter
	 * 			int number: number passed as parameter
	 * 			int row: row passed as parameter
	 * 			int col: col passed as parameter  		
	 * 	@returns True, if piece data has changed
	 * 	@returns False, if piece data has not changed
	 */
	public boolean hasChanged(int orientation, String piece, int number, int row, int col) {
		if(orientation != this.orientation)
			return true;
		else if(!piece.equals(this.piece)) 
			return true;
		else if(number != this.number)
			return true;
		else if(row != this.row)
			return true;
		else if(col != this.col)
			return true;
		else
			return false;
	}
	
	public void setPiece(int orientation, String piece, int number, int row, int col) {
		this.orientation = orientation;
		this.piece = piece;
		this.number = number;
		this.row = row;
		this.col = col;
	}
	
	public int getOrientation() {
		return this.orientation;
	}
	
	public String getPiece() {
		return this.piece;
	}
	
	public int getNumber() {
		return this.number;
	}
	
	public int getRow() {
		return this.row;
	}
	
	public int getCol() {
		return this.col;
	}
}
