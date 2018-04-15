import java.util.Scanner;

public class PlayerSkeleton {

<<<<<<< HEAD
    // another State object to simulate the moves before actually playing
    private StateSimulator simulator;

    public PlayerSkeleton() {
        simulator = new StateSimulator();
    }


    //implement this function to have a working system
    public int pickMove(State s, int[][] legalMoves) {
        int moveToPlay = getBestMoveBySimulation(s, legalMoves.length);
        //System.out.println("Move to Play = " + moveToPlay);
        simulator.makeMove(moveToPlay);
        simulator.markSimulationDoneWithCurrentPiece();
        return moveToPlay;
    }

    /**
     * @param actualState the actual state with which the game runs
     * @param moveChoices the legal moves allowed
     * @return the best move
     */
    public int getBestMoveBySimulation(State actualState, int moveChoices) {
        int bestMove = 0;
        double bestUtility = Double.NEGATIVE_INFINITY;
        simulator.setNextPiece(actualState.nextPiece); // synchronize the next piece
        for (int currentMove = 0; currentMove < moveChoices; currentMove++) {
            simulator.makeMove(currentMove);
            double currentUtility = Heuristics.getInstance().getUtility(simulator);
            if (currentUtility > bestUtility) {
                bestMove = currentMove;
                bestUtility = currentUtility;
            }
            simulator.resetMove();
        }
        return bestMove;
    }

    public static final int COLS = 10;
    public static final int ROWS = 21;
    public static final int ORIENT = 0;
    public static final int SLOT = 1;
    public static final int N_PIECES = 7;

    protected static int[][][] legalMoves = new int[N_PIECES][][];

    private int[][] field = new int[ROWS][COLS];
    private int[] top = new int[COLS];
    //private int[] pOrients = new int[];
    private int[][] pWidth = State.getpWidth();
    private int[][] pHeight = State.getpHeight();
    private int[][][] pBottom = State.getpBottom();
    private int[][][] pTop = State.getpTop();
    private int nextPiece;
    private int turn = 0;
    private boolean lost;
    private int cleared = 0;

    public int[][] getField() {
        return field;
    }

    public int[] getTop() {
        return top;
    }

    //public static int[] getpOrients() { return pOrients; }

	/* public static int[][] getpWidth() {
        return pWidth;
	} */

	/* public static int[][] getpHeight() {
        return pHeight;
	} */

	/* public static int[][][] getpBottom() {
		return pBottom;
	} */

    public int[] getpTop() {
        return top;
    }

    public boolean hasLost() {
        return lost;
    }

    public int getRowsCleared() {
        return cleared;
    }

    public int getTurnNumber() {
        return turn;
    }

    public int getNextPiece() {
        return nextPiece;
    }

    public void setNextPiece(int nextPiece) {
        this.nextPiece = nextPiece;
    }

    public void resetState(State state) {
        int[][] field = state.getField();
        int stateNextPiece = state.getNextPiece();
        boolean stateLost = state.hasLost();
        int stateCleared = state.getRowsCleared();
        int stateTurn = state.getTurnNumber();
        Arrays.fill(this.top, 0);
    }

    public boolean makeMove(int orient, int slot) {
        turn++;
        //height if the first column makes contact
        int height = top[slot] - pBottom[nextPiece][orient][0];
        //for each column beyond the first in the piece
        for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
            height = Math.max(height, top[slot + c] - pBottom[nextPiece][orient][c]);
        }

        //check if game ended
        if (height + pHeight[nextPiece][orient] >= ROWS) {
            lost = true;
            return false;
        }


        //for each column in the piece - fill in the appropriate blocks
        for (int i = 0; i < pWidth[nextPiece][orient]; i++) {

            //from bottom to top of brick
            for (int h = height + pBottom[nextPiece][orient][i]; h < height + pTop[nextPiece][orient][i]; h++) {
                field[h][i + slot] = turn;
            }
        }

        //adjust top
        for (int c = 0; c < pWidth[nextPiece][orient]; c++) {
            top[slot + c] = height + pTop[nextPiece][orient][c];
        }

        int rowsCleared = 0;

        //check for full rows - starting at the top
        for (int r = height + pHeight[nextPiece][orient] - 1; r >= height; r--) {
            //check all columns in the row
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (field[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            //if the row was full - remove it and slide above stuff down
            if (full) {
                rowsCleared++;
                cleared++;
                //for each column
                for (int c = 0; c < COLS; c++) {

                    //slide down all bricks
                    for (int i = r; i < top[c]; i++) {
                        field[i][c] = field[i + 1][c];
                    }
                    //lower the top
                    top[c]--;
                    while (top[c] >= 1 && field[top[c] - 1][c] == 0) top[c]--;
                }
            }
        }

        //pick a new piece
        //nextPiece = randomPiece();

        return true;

        //return super.makeMove(orient, slot);
    }

    //gives legal moves for
    public int[][] legalMoves() {
        return legalMoves[nextPiece];
    }

    //make a move based on the move index - its order in the legalMoves list
    public void makeMove(int move) {
        makeMove(legalMoves[nextPiece][move]);
    }

    //make a move based on an array of orient and slot
    public void makeMove(int[] move) {
        makeMove(move[ORIENT], move[SLOT]);
    }

    public static void main(String[] args) {
        State s = new State();
        new TFrame(s);
        PlayerSkeleton p = new PlayerSkeleton();

        while (!s.hasLost()) {
            s.makeMove(p.pickMove(s, s.legalMoves()));
            s.draw();
            s.drawNext(0, 0);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("You have completed " + s.getRowsCleared() + " rows.");
    }

    public static int run() {
        State s = new State();
        //new TFrame(s);
        PlayerSkeleton p = new PlayerSkeleton();

        while (!s.hasLost()) {
            s.makeMove(p.pickMove(s, s.legalMoves()));
            //	s.draw();
            //	s.drawNext(0,0);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("You have completed " + s.getRowsCleared() + " rows.");
        return s.getRowsCleared();
    }

    /// features

    /**
     * @param s: represents the state of the game
     * @return number of holes in the field
     */
    public static int numHoles(State s) {
        int[] topRow = s.getTop();
        int[][] field = s.getField();
        int numHoles = 0;
        boolean hasHole;
        for (int col = 0; col < State.COLS; col++) {
            // for each column, find if there is a hole
            hasHole = false;
            int topCell = topRow[col] - 1; // since s.getTop() contains topRow + 1
            for (int row = topCell - 1; row > 0; row--) {
                if (field[row][col] == 0) {
                    hasHole = true;
                    break;
                }
            }

            if (hasHole) { // if this column has a hole
                numHoles++;
            }
        }

        return numHoles;
    }

    /**
     * @param s: State
     * @return sum of total height difference between neighbouring columns abs(height(k) - height(k+1))
     */
    public static int heightDiff(State s) {
        int[] topRow = s.getTop(); // no need to minus 1 since it gets cancelled
        int totalHeightDiff = 0;
        for (int currentCol = 0; currentCol < topRow.length - 1; currentCol++) {
            int nextCol = currentCol + 1;
            totalHeightDiff += Math.abs(currentCol - nextCol);
        }
        return totalHeightDiff;
    }


    /**
     * @param s: State
     * @return the maximum height among all the columns
     */
    public static int maxHeight(State s) {
        int maxHeight = -1;
        for (int top : s.getTop()) {
            maxHeight = Math.max(maxHeight, top - 1);
        }
        return maxHeight;
    }
=======
	// another State object to simulate the moves before actually playing
	protected StateSimulator sim;
	private double[] weights;
	private static final boolean DEBUG_FEATURES = true;

	private static Scanner sc = new Scanner(System.in);

	public PlayerSkeleton() {
			sim = new StateSimulator();
	}

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		int moveToPlay = getBestMoveBySimulation(s, legalMoves.length);
		this.sim.makeMove(moveToPlay); // update it since you do end up making this move
		this.sim.markSimulationDoneWithCurrentPiece();
		return moveToPlay;
	}

	/**
	 * @param actualState the actual state with which the game runs
	 * @param moveChoices the legal moves allowed
	 * @return the best move
	 */
	private int getBestMoveBySimulation(State actualState, int moveChoices) {
		int bestMove = 0;
		double bestUtility = Double.NEGATIVE_INFINITY;
		sim.setNextPiece(actualState.nextPiece); // synchronize the next piece
		for (int currentMove = 0; currentMove < moveChoices; currentMove++) {
			double currentUtility = getUtility(sim, currentMove);
			if (currentUtility > bestUtility) {
				bestMove = currentMove;
				bestUtility = currentUtility;
			}
		}
		return bestMove;
	}
	
	public static void main(String[] args) {
		State state = new State();
		new TFrame(state);
		PlayerSkeleton p = new PlayerSkeleton();
		double[] sampleWeight = {-0.01, -0.02, -0.03, -0.05, -0.3, -0.1, -0.3, -0.5};
		p.setWeights(sampleWeight);

		while(!state.hasLost()) {
			state.makeMove(p.pickMove(state,state.legalMoves()));
			state.draw();
			state.drawNext(0,0);
			if (DEBUG_FEATURES) {
				// wait for a signal in the terminal to move to the next state
				printFeatures(p.sim.getFeaturesArray());
				System.out.println("Press ENTER to apply next move");
				sc.nextLine();
			} else {
//				try {
//					Thread.sleep(300);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
		}
		System.out.println("You have completed "+state.getRowsCleared()+" rows.");
		System.exit(0);
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
	}

	private double getUtility(StateSimulator s, int move) {
		if (this.weights == null) {
			System.out.println("PlayerSkeleton: Weight array is not set. Exiting");
			System.exit(1);
		}

		sim.makeMove(move);
		if (sim.hasLost()) {
			sim.resetMove();
			return Double.NEGATIVE_INFINITY;
		}

		int[] features = s.getFeaturesArray();

		double utility = 0;
		for (int i = 0; i < StateSimulator.NUM_FEATURES; i++){
			utility += features[i] * weights[i];
		}

		sim.resetMove();
		return utility;
	}
>>>>>>> 4ea54376b1946c884b5e67f7fcad0d893a428254

	private static void printFeatures(int[] features) {
		System.out.println("--------------------------------------------------");
		System.out.println("#holes = " + 			features[StateSimulator.INDEX_NUMHOLES]);
		System.out.println("Col Transition = " + 	features[StateSimulator.INDEX_COL_TRANSITIONS]);
		System.out.println("Row Transition = " + 	features[StateSimulator.INDEX_ROW_TRANSITIONS]);
		System.out.println("holes depth = " + 		features[StateSimulator.INDEX_HOLE_DEPTH]);
		System.out.println("Cumulative well = " + 	features[StateSimulator.INDEX_CUMULATIVE_WELLS]);
		System.out.println("Landing height = " + 	features[StateSimulator.INDEX_LANDING_HEIGHT]);
		System.out.println("#rows with hole = " +	features[StateSimulator.INDEX_NUM_ROWS_WITH_HOLE]);
		System.out.println("Eroded piece cells " +  features[StateSimulator.INDEX_ERODED_PIECE_CELLS]);
		System.out.println("--------------------------------------------------");
	}
}

