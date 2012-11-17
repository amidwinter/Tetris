/*
 * Stores information about the current piece (supplied by pubSub connection)
 */
public class Piece {
	private int orientation;
	private String piece;
	private int number;
	private int row;
	private int col;
	private boolean isSet = false;
	
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
		this.isSet = true;
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
	
	public int[][] getMask()
	{
		int[][] pieceMask = new int[4][4];
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				pieceMask[i][j] = 0;
		
		if(this.piece.equals("O"))
		{
			pieceMask[1][1] = 1;
			pieceMask[1][2] = 1;
			pieceMask[2][1] = 1;	
			pieceMask[2][2] = 1;	
		}
		else if(this.piece.equals("I"))
		{
			if(this.orientation == 0)
			{
				pieceMask[1][0] = 1;
				pieceMask[1][1] = 1;
				pieceMask[1][2] = 1;
				pieceMask[1][3] = 1;
				
			}
			else if(this.orientation == 1)
			{
				pieceMask[0][2] = 1;
				pieceMask[1][2] = 1;
				pieceMask[2][2] = 1;
				pieceMask[3][2] = 1;
			}
		}
		else if(this.piece.equals("S"))
		{
			if(this.orientation == 0)
			{
				pieceMask[1][2] = 1;
				pieceMask[1][3] = 1;
				pieceMask[2][1] = 1;
				pieceMask[2][2] = 1;
			}
			else if(this.orientation == 1)
			{
				pieceMask[0][2] = 1;
				pieceMask[1][2] = 1;
				pieceMask[1][3] = 1;
				pieceMask[2][3] = 1;
			}
		}
		else if(this.piece.equals("Z"))
		{
			if(this.orientation == 0)
			{
				pieceMask[1][1] = 1;
				pieceMask[1][2] = 1;
				pieceMask[2][2] = 1;
				pieceMask[2][3] = 1;
			}
			else if(this.orientation == 1)
			{
				pieceMask[0][3] = 1;
				pieceMask[1][2] = 1;
				pieceMask[1][3] = 1;
				pieceMask[2][2] = 1;
			}
		}
		else if(this.piece.equals("L"))
		{
			if(this.orientation == 0)
			{
				pieceMask[1][1] = 1;
				pieceMask[1][2] = 1;
				pieceMask[1][3] = 1;
				pieceMask[2][3] = 1;
			}
			else if(this.orientation == 1)
			{
				pieceMask[0][2] = 1;
				pieceMask[1][2] = 1;
				pieceMask[2][2] = 1;
				pieceMask[2][3] = 1;
			}
			else if(this.orientation == 2)
			{
				pieceMask[0][3] = 1;
				pieceMask[1][1] = 1;
				pieceMask[1][2] = 1;
				pieceMask[1][3] = 1;
			}
			else if(this.orientation == 3)
			{
				pieceMask[0][1] = 1;
				pieceMask[0][2] = 1;
				pieceMask[1][2] = 1;
				pieceMask[2][2] = 1;
			}
		}
		else if(this.piece.equals("J"))
		{
			if(this.orientation == 0)
			{
				pieceMask[1][1] = 1;
				pieceMask[1][2] = 1;
				pieceMask[1][3] = 1;
				pieceMask[2][3] = 1;
			}
			else if(this.orientation == 1)
			{
				pieceMask[0][2] = 1;
				pieceMask[0][3] = 1;
				pieceMask[1][2] = 1;
				pieceMask[2][2] = 1;
			}
			else if(this.orientation == 2)
			{
				pieceMask[0][1] = 1;
				pieceMask[1][1] = 1;
				pieceMask[1][2] = 1;
				pieceMask[1][3] = 1;
			}
			else if(this.orientation == 3)
			{
				pieceMask[0][2] = 1;
				pieceMask[1][2] = 1;
				pieceMask[2][2] = 1;
				pieceMask[2][1] = 1;		
			}
		}
		else if(this.piece.equals("T"))
		{
			if(this.orientation == 0)
			{
				pieceMask[1][1] = 1;
				pieceMask[1][2] = 1;
				pieceMask[1][3] = 1;
				pieceMask[2][2] = 1;
			}
			else if(this.orientation == 1)
			{
				pieceMask[0][2] = 1;
				pieceMask[1][2] = 1;
				pieceMask[1][3] = 1;
				pieceMask[2][2] = 1;
			}
			else if(this.orientation == 2)
			{
				pieceMask[0][2] = 1;
				pieceMask[1][1] = 1;
				pieceMask[1][2] = 1;
				pieceMask[1][3] = 1;
			}
			else if(this.orientation == 3)
			{
				pieceMask[0][2] = 1;
				pieceMask[1][1] = 1;
				pieceMask[1][2] = 1;
				pieceMask[2][2] = 1;
			}
		}
		
		return pieceMask;
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
	
	public boolean isSet() {
		return this.isSet;
	}
}
