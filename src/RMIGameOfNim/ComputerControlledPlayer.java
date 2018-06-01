package RMIGameOfNim;

import java.util.LinkedList;
import java.util.List;

/**
 * The computer controlled player extends the player class and implements
 * runable, so it can be assigned to a thread. It will always make the best
 * possible move given any number of marbles.
 *
 * @author Alexander.Keidel@go.edgehill.ac.uk
 */
public class ComputerControlledPlayer extends Player implements Runnable {

    private final GameLogicEngine myMatch;
    private int lastMove;
    private final List<Double> smartMoves = new LinkedList<>();

    /**
     * Default construtor used to populate the list
     * {@link ComputerControlledPlayer#smartMoves}
     *
     * @param totalMarbles
     * @param myMatch
     */
    public ComputerControlledPlayer(int totalMarbles, GameLogicEngine myMatch) {
        this.myMatch = myMatch;
        super.setPlayerID(Integer.MIN_VALUE); //setting default ID to something the server would never assign
        for (int i = 1; i < 10; i++) { //find 10 smart moves
            smartMoves.add((Math.pow(2, i) - 1)); //finding values of i to the power of 2 minus 1 and adding them to the smart moves list
        }
        System.out.println("Smart moves: " + smartMoves.toString());
    }

    /**
     * Make a smart move based upon the winning strategy of producing marble
     * counts of values of 2 to ther power of n minus 1, i.e. 1, 3, 7, 15, 31,
     * 63, etc.
     *
     * @param currentMarbles Count of the current marbles to select from
     * @return Integer value of a smart move that will be based upon the winning
     * strategy for this game of nim
     */
    public int makeSmartMove(int currentMarbles) {
        if (currentMarbles == 1) {
            System.out.println("Cpu making smart move: " + 1);
            lastMove = 1;
            return 1; //pick the last marble
        }
        int smartMove = 1; //default move
        lastMove = smartMove;
        for (double d : smartMoves) {
            if ((int) d >= (currentMarbles / 2) && (int) d <= (currentMarbles - 1)) { //the smart move result must be greater than half of the marbles, but no larger than the current Marbles - 1
                smartMove = (currentMarbles - (int) d); //the smart move is the result
                lastMove = smartMove;
            }
        }
        System.out.println("Cpu making smart move: " + smartMove);
        return smartMove;
    }

    /**
     * Thread start / run method
     */
    @Override
    public void run() {
        waitForTurn();
    }

    /**
     * The CPU controlled player will wait for their turn, checking endlessly.
     * Once it's their turn they will pick a smart move according to the winning
     * strategy.
     */
    private void waitForTurn() {
        System.out.println("CPU started waiting for it's turn.");
        while (!myMatch.isGameOver()) {
            float busy = 1; //this enables the thread to run, if you take this line out this while-true loop will not be executed
            if (myMatch.checkIfItIsMyTurn(super.getPlayerID())) {
                System.out.println("Cpu making a move.");
                if (!myMatch.pickMarbles(super.getPlayerID(), makeSmartMove(myMatch.getCurrentMarbles()))) { //make a smart move pased upon the current marbles 
                    System.out.println("Trying to make move: " + lastMove);
                } else {
                    myMatch.getPlayerOne().addMessage("The CPU has taken " + lastMove + " marbles.\nThe total is now " + myMatch.getCurrentMarbles());
                }
            }
        }
        System.out.println("CPU-Enemy: The game has ended, terminating my thread.");
        Thread.currentThread().interrupt(); //terminate this thread
        return;
    }
}
