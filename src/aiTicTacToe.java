import java.util.*;
public class aiTicTacToe {
    public static final int DEPTH = 6;

	public int player; //1 for player 1 and 2 for player 2
    List<List<positionTicTacToe>> winningLines; // winning Lines of the 3D board
	private int getStateOfPositionFromBoard(positionTicTacToe position, List<positionTicTacToe> board)
	{
		//a helper function to get state of a certain position in the Tic-Tac-Toe board by given position TicTacToe
		int index = position.x*16+position.y*4+position.z;
		return board.get(index).state;
	}

	public positionTicTacToe myAIAlgorithm(List<positionTicTacToe> board, int player)
	{
		//TODO: this is where you are going to implement your AI algorithm to win the game. The default is an AI randomly choose any available move.
        positionTicTacToe myNextMove = null;
		long start = System.currentTimeMillis();
		myNextMove = alphaBeta(board, DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true);	// Run the alpha-beta pruning algorithm
		long end = System.currentTimeMillis();
		System.out.println("(" + myNextMove.x + "," + myNextMove.y + "," + myNextMove.z + ")");
		System.out.println("this step takes: "+ (end - start)/1000.0 + "s");
		return myNextMove;
	}

	// This is the default random Algorithm
	public positionTicTacToe randomPosition(List<positionTicTacToe> board)
	{
		positionTicTacToe myNextMove = new positionTicTacToe(0,0,0);

		do
		{
			Random rand = new Random();
			int x = rand.nextInt(4);
			int y = rand.nextInt(4);
			int z = rand.nextInt(4);
			myNextMove = new positionTicTacToe(x,y,z);
		}while(getStateOfPositionFromBoard(myNextMove,board)!=0);
		return myNextMove;
	}

	// The first step of alpha-beta pruning

	/**
	 * The first step of alpha-beta pruning algorithm
	 * @param board
	 * @param depth
	 * @param alpha
	 * @param beta
	 * @param maximizer
	 * @return
	 */
	public positionTicTacToe alphaBeta(List<positionTicTacToe> board, int depth, int alpha, int beta, boolean maximizer)
    {
        positionTicTacToe nextMove = new positionTicTacToe(0,0,0);
        List<positionTicTacToe> selectableMove = getEmptyPosition(board);
        List<positionTicTacToe> tempBoard = deepCopyATicTacToeBoard(board);

        int value = Integer.MIN_VALUE;

        for(positionTicTacToe item : selectableMove)
        {
            int index = item.x * 16 + item.y * 4 + item.z;

            tempBoard.get(index).state = player;

            // Start next pruning
            int currentValue = alphaBetaPruning(tempBoard, depth - 1, alpha, beta, false);

            if(currentValue > value)
            {
                value = currentValue;
                nextMove = item;
            }
            alpha = Math.max(alpha, value);

            tempBoard.get(index).state = 0;
        }

        return nextMove;
    }

	/**
	 * The alpha-beta pruning algorithm with int as return
	 * @param board	the current board situation
	 * @param depth	remain depth
	 * @param alpha
	 * @param beta
	 * @param maximizer
	 * @return	the evaluation of this node
	 */
	public int alphaBetaPruning(List<positionTicTacToe> board, int depth, int alpha, int beta, boolean maximizer)
	{
		List<positionTicTacToe> selectableMove = getEmptyPosition(board);
		List<positionTicTacToe> tempBoard = deepCopyATicTacToeBoard(board);
		int value = 0;
		int opponent = player == 1 ? 2 : 1;
		int now = maximizer ? player : opponent;

		if(depth == 0 || selectableMove.isEmpty())
		{
            value = heuristicValue(board);
            return value;	// return the evaluation of the leaf node
		}
		if(maximizer)
        {
            value = Integer.MIN_VALUE;
            for(positionTicTacToe item : selectableMove)	// traverse all the possible next step
            {
                int index = item.x * 16 + item.y * 4 + item.z;
                tempBoard.get(index).state = now;

                value = Math.max(value, alphaBetaPruning(tempBoard,depth - 1, alpha, beta,false));	// pass the board to next layer and get the evaluation
                alpha = Math.max(alpha, value);

                tempBoard.get(index).state = 0;
                if(beta <= alpha)	// pruning
                {
                    break;
                }
            }
            return value;
        }
		else
        {
            value = Integer.MAX_VALUE;
            for(positionTicTacToe item : selectableMove)	// traverse all the possible next step
            {
                int index = item.x * 16 + item.y * 4 + item.z;
                tempBoard.get(index).state = now;

                value = Math.min(value, alphaBetaPruning(tempBoard, depth - 1, alpha, beta, true));	// pass the board to next layer and get the evaluation
                beta = Math.min(beta, value);

                tempBoard.get(index).state = 0;
                if(beta <= alpha)	// pruning
                {
                    break;
                }
            }
            return value;
        }
	}

	/**
	 *
	 * @param board	the situation of board needed to be evalate
	 * @return return the evaluate score of the board
	 */
	public int heuristicValue(List<positionTicTacToe> board)
	{
        int totalScore = 0;
        int opponent = player == 1 ? 2 : 1;

        for(int i = 0; i < winningLines.size(); i++)    // evaluate the score of each winning line to get the total score of the board
        {
            int playerCount = 0;	// the number of player's piece on this line
            int opponentCount = 0;	// the number of opponent's piece on this line

            List<positionTicTacToe> oneLine = winningLines.get(i);
            for(int j = 0; j < oneLine.size(); j++)	// traverse the line to get the number of each kind of pieces
            {
                int state = getStateOfPositionFromBoard(oneLine.get(j), board);

                if(state == player)
                {
                    playerCount++;
                }
                if(state == opponent)
                {
                    opponentCount++;
                }
            }

            // If one winning line has both player's and opponent's, this line will always has no value in the future, so delete it
            if(playerCount > 0 && opponentCount > 0)
            {
                winningLines.remove(i);
            }

            int oneLineScore = oneLineScore_2(playerCount, opponentCount);	// evaluate the score of this line
			totalScore += oneLineScore;	// add the line score to total score of board
        }

		return totalScore;
	}

	/**
	 * almost the same as oneLineScore_2, but the line has both player's and opponent's pieces also has value
	 * @param playerCount
	 * @param opponentCount
	 * @return
	 */
	public int oneLineScore_1(int playerCount, int opponentCount)
    {
        int playerScore = 0;
        int opponentScore = 0;
        int totalSore = 0;

        playerScore = (int)Math.pow(10, playerCount);
        opponentScore = (int)(Math.pow(10, opponentCount) + opponentCount + 2);
        totalSore = playerScore - opponentScore;

        if(playerCount == 4)
        {
            return Integer.MAX_VALUE;
        }
        if(opponentCount == 4)
        {
            return Integer.MIN_VALUE;
        }

        return totalSore;
    }

	/**
	 * 	evaluate the score of one possible winning line
	 * @param playerCount
	 * @param opponentCount
	 * @return
	 */
	public int oneLineScore_2(int playerCount, int opponentCount)
    {
        int playerScore = 0;
        int opponentScore = 0;
        int totalSore = 0;

        if(playerCount!=0&&opponentCount!=0)	// if the line has both player's and opponent's pieces, this line has no value
        {
            return 0;
        }

        // the line with n+1 pieces has 10 times value of the line with n pieces
        playerScore = (int)Math.pow(10, playerCount);
        // the opponent' pieces has slightly higher value than player's pieces
        opponentScore = (int)(Math.pow(10, opponentCount) + opponentCount + 2);
        totalSore = playerScore - opponentScore;	// calculate the total value of the board

        /*if(playerCount == 4)
        {
            return Integer.MAX_VALUE;
        }
        if(opponentCount == 4)
        {
            return Integer.MIN_VALUE;
        }*/

        return totalSore;
    }

	// Find the empty position on the board
	public List<positionTicTacToe> getEmptyPosition(List<positionTicTacToe> board)
	{
		List<positionTicTacToe> emptyPositions = new ArrayList<positionTicTacToe>();
		for(positionTicTacToe item : board)
		{
			if(item.state==0)
			{
				emptyPositions.add(item);
			}
		}
		return emptyPositions;
	}

	private List<List<positionTicTacToe>> initializeWinningLines()
	{
		//create a list of winning line so that the game will "brute-force" check if a player satisfied any 	winning condition(s).
		List<List<positionTicTacToe>> winningLines = new ArrayList<List<positionTicTacToe>>();
		
		//48 straight winning lines
		//z axis winning lines
		for(int i = 0; i<4; i++)
			for(int j = 0; j<4;j++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,j,0,-1));
				oneWinCondtion.add(new positionTicTacToe(i,j,1,-1));
				oneWinCondtion.add(new positionTicTacToe(i,j,2,-1));
				oneWinCondtion.add(new positionTicTacToe(i,j,3,-1));
				winningLines.add(oneWinCondtion);
			}
		//y axis winning lines
		for(int i = 0; i<4; i++)
			for(int j = 0; j<4;j++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,0,j,-1));
				oneWinCondtion.add(new positionTicTacToe(i,1,j,-1));
				oneWinCondtion.add(new positionTicTacToe(i,2,j,-1));
				oneWinCondtion.add(new positionTicTacToe(i,3,j,-1));
				winningLines.add(oneWinCondtion);
			}
		//x axis winning lines
		for(int i = 0; i<4; i++)
			for(int j = 0; j<4;j++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,i,j,-1));
				oneWinCondtion.add(new positionTicTacToe(1,i,j,-1));
				oneWinCondtion.add(new positionTicTacToe(2,i,j,-1));
				oneWinCondtion.add(new positionTicTacToe(3,i,j,-1));
				winningLines.add(oneWinCondtion);
			}
		
		//12 main diagonal winning lines
		//xz plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,i,0,-1));
				oneWinCondtion.add(new positionTicTacToe(1,i,1,-1));
				oneWinCondtion.add(new positionTicTacToe(2,i,2,-1));
				oneWinCondtion.add(new positionTicTacToe(3,i,3,-1));
				winningLines.add(oneWinCondtion);
			}
		//yz plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,0,0,-1));
				oneWinCondtion.add(new positionTicTacToe(i,1,1,-1));
				oneWinCondtion.add(new positionTicTacToe(i,2,2,-1));
				oneWinCondtion.add(new positionTicTacToe(i,3,3,-1));
				winningLines.add(oneWinCondtion);
			}
		//xy plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,0,i,-1));
				oneWinCondtion.add(new positionTicTacToe(1,1,i,-1));
				oneWinCondtion.add(new positionTicTacToe(2,2,i,-1));
				oneWinCondtion.add(new positionTicTacToe(3,3,i,-1));
				winningLines.add(oneWinCondtion);
			}
		
		//12 anti diagonal winning lines
		//xz plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,i,3,-1));
				oneWinCondtion.add(new positionTicTacToe(1,i,2,-1));
				oneWinCondtion.add(new positionTicTacToe(2,i,1,-1));
				oneWinCondtion.add(new positionTicTacToe(3,i,0,-1));
				winningLines.add(oneWinCondtion);
			}
		//yz plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,0,3,-1));
				oneWinCondtion.add(new positionTicTacToe(i,1,2,-1));
				oneWinCondtion.add(new positionTicTacToe(i,2,1,-1));
				oneWinCondtion.add(new positionTicTacToe(i,3,0,-1));
				winningLines.add(oneWinCondtion);
			}
		//xy plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,3,i,-1));
				oneWinCondtion.add(new positionTicTacToe(1,2,i,-1));
				oneWinCondtion.add(new positionTicTacToe(2,1,i,-1));
				oneWinCondtion.add(new positionTicTacToe(3,0,i,-1));
				winningLines.add(oneWinCondtion);
			}
		
		//4 additional diagonal winning lines
		List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(0,0,0,-1));
		oneWinCondtion.add(new positionTicTacToe(1,1,1,-1));
		oneWinCondtion.add(new positionTicTacToe(2,2,2,-1));
		oneWinCondtion.add(new positionTicTacToe(3,3,3,-1));
		winningLines.add(oneWinCondtion);
		
		oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(0,0,3,-1));
		oneWinCondtion.add(new positionTicTacToe(1,1,2,-1));
		oneWinCondtion.add(new positionTicTacToe(2,2,1,-1));
		oneWinCondtion.add(new positionTicTacToe(3,3,0,-1));
		winningLines.add(oneWinCondtion);
		
		oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(3,0,0,-1));
		oneWinCondtion.add(new positionTicTacToe(2,1,1,-1));
		oneWinCondtion.add(new positionTicTacToe(1,2,2,-1));
		oneWinCondtion.add(new positionTicTacToe(0,3,3,-1));
		winningLines.add(oneWinCondtion);
		
		oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(0,3,0,-1));
		oneWinCondtion.add(new positionTicTacToe(1,2,1,-1));
		oneWinCondtion.add(new positionTicTacToe(2,1,2,-1));
		oneWinCondtion.add(new positionTicTacToe(3,0,3,-1));
		winningLines.add(oneWinCondtion);	
		
		return winningLines;
		
	}

    private List<positionTicTacToe> deepCopyATicTacToeBoard(List<positionTicTacToe> board)
    {
        //deep copy of game boards
        List<positionTicTacToe> copiedBoard = new ArrayList<positionTicTacToe>();
        for(int i=0;i<board.size();i++)
        {
            copiedBoard.add(new positionTicTacToe(board.get(i).x,board.get(i).y,board.get(i).z,board.get(i).state));
        }
        return copiedBoard;
    }

	public aiTicTacToe(int setPlayer)
	{
		player = setPlayer;

		winningLines = initializeWinningLines();	// get all possible winning lines
	}
}

