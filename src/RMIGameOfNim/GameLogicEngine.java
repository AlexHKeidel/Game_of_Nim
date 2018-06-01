package RMIGameOfNim;

import java.util.List;
import java.util.Random;

/**
 * Core logic for the Game of Nim Keeps track of the total marbles for the game,
 * the current count of marbles, a list of the comlpeted moves for debugging or
 * error tracking
 *
 * @author Alexander.Keidel@go.edgehill.ac.uk
 */
public class GameLogicEngine implements Runnable {
    
    private GameServer myServer;
    private int matchID;
    private Player playerOne = null, playerTwo = null;
    private int nextTurn;
    private boolean playerOneTurn;
    private final int DEFAULT_MARBLE_COUNT = 20;
    private int chosenMode, chosenDifficulty;
    private final Random randy = new Random();
    private int totalMarbles; //total marble count
    private int currentMarbles; //current marble count
    private List<Integer> completedMoves; //a list of all the consecutive moves
    private volatile boolean gameOver = false;

    /**
     * Use this constructor when creating a new {@link GameLogicEngine} (match)
     * and assign the first player and choose the mode A second player must be
     * assigned before one may start the game
     *
     * @param matchID
     * @param playerOneID
     * @param myServer
     * @param hard
     * @param cpu
     */
    public GameLogicEngine(int matchID, GameServer myServer, int playerOneID, boolean hard, boolean cpu) {
        this.matchID = matchID;
        playerOne = new Player(playerOneID);
        selectDifficulty(hard);
        if (cpu) {
            playerOne.setChosenMode(GameEngineRemoteInterface.CPU_MODE);
        }
    }

    /**
     * @param matchID
     * @param myServer
     * @deprecated Default constructor, defaulting to a value of 20 for
     * {@link GameLogicEngine#totalMarbles}
     * @param playerOneID
     * @param playerTwoID
     */
    public GameLogicEngine(int matchID, GameServer myServer, int playerOneID, int playerTwoID) {
        this.matchID = matchID;
        playerOne = new Player(playerOneID);
        playerTwo = new Player(playerTwoID);
        pickPlayerToStart();
        totalMarbles = DEFAULT_MARBLE_COUNT; //default value
    }

    /**
     * Constructor specific to the requirements of the coursework: If hard is
     * chosen a random integer between 2 and 100 will be chosen for
     * {@link GameLogicEngine#totalMarbles}. Otherwise the game defaults to an
     * integer value between 2 and 20 for {@link GameLogicEngine#totalMarbles}.
     *
     * @param matchID
     * @param myServer
     * @param playerOneID
     * @param playerTwoID
     * @param hard
     */
    public GameLogicEngine(int matchID, GameServer myServer, int playerOneID, int playerTwoID, boolean hard) {
        this.matchID = matchID;
        playerOne = new Player(playerOneID);
        playerTwo = new Player(playerTwoID);
        pickPlayerToStart();
        selectDifficulty(hard);
    }

    /**
     * Initialises the logic engine with a set amount of marbles Make sure you
     * choose a positive integer above 2 or it will default to 20
     *
     * @param matchID
     * @param myServer
     * @param playerOneID
     * @param playerTwoID
     * @param amount Chosen amount of total marbles for the game
     */
    public GameLogicEngine(int matchID, GameServer myServer, int playerOneID, int playerTwoID, int amount) {
        this.matchID = matchID;
        playerOne = new Player(playerOneID);
        playerTwo = new Player(playerTwoID);
        pickPlayerToStart();
        if (amount >= 2) {
            totalMarbles = amount;
        } else {
            totalMarbles = DEFAULT_MARBLE_COUNT;
        }
    }

    /**
     *
     * @param hard
     */
    private void selectDifficulty(boolean hard) {
        if (hard) {
            totalMarbles = randy.nextInt(99) + 2;
            
            setChosenDifficulty(GameEngineRemoteInterface.HARD_DIFFICULTY);
        } else {
            totalMarbles = randy.nextInt(19) + 2;
            setChosenDifficulty(GameEngineRemoteInterface.EASY_DIFFICULYT);
        }
        currentMarbles = totalMarbles; //udpate current marbles
    }

    /**
     * This method initialises the core values for the game. This should only be
     * called once totalMarbles has been set to a valid value.
     */
    private void initialiseCoreGame() {
        setCurrentMarbles(totalMarbles); //set the initial value for currentMarbles
        completedMoves.clear(); //clears the completed moves list
    }

    /**
     * Reset the GameLogicEngine without having to destroy the object and
     * construct a new one. Makes a call to
     * {@link GameLogicEngine#initialiseCoreGame()}
     *
     * @deprecated Do not use, instantitate a new {@link GameLogicEngine}
     * instead!
     * @param hard Boolean to choose hard mode (true) or normal mode (false)
     */
    public void resetGameLogicEngine(boolean hard) {
        selectDifficulty(hard);
        initialiseCoreGame(); //initialise a new game with the new totalMarbles value
    }
    
    private boolean determineLegalMove(int move) {
        if (move == 1 && currentMarbles == 1) { //taking 1 is valid even when there are only 2 marbles left
            return true;
        }
        return (move >= 1 && (move <= (currentMarbles / 2))); //if the move greater or equal to 1 and is smaller or equal to half of the current marbles
    }

    /**
     * Getter for {@link GameLogicEngine#totalMarbles}
     *
     * @return Integer representing the total marbles chosen for this game
     */
    public int getTotalMarbles() {
        return totalMarbles;
    }

    /**
     * Setter for {@link GameLogicEngine#totalMarbles}
     *
     * @param amount
     * @return true if successfully set
     */
    public boolean setTotalMarbles(int amount) {
        if (amount <= 2) {
            totalMarbles = amount;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Getter for {@link GameLogicEngine#currentMarbles}
     *
     * @return
     */
    public int getCurrentMarbles() {
        return currentMarbles;
    }

    /**
     * Setter for {@link GameLogicEngine#currentMarbles}
     *
     * @param amount, can't ever be lower than 0
     * @return true if successfully set, i.e. a positive integer
     */
    private boolean setCurrentMarbles(int amount) {
        currentMarbles = amount;
        System.out.println("Updated current marbles to " + currentMarbles);
        return true;
    }

    /**
     * Getter for {@link GameLogicEngine#completedMoves}
     *
     * @return
     */
    public List<Integer> getCompletedMoves() {
        return completedMoves;
    }

    /**
     * Determines one of the two player IDs to have the first turn using
     * pseudo-random numbers Warning: Only use this once
     * {@link GameLogicEngine#playerOneID} and
     * {@link GameLogicEngine#playerTwoID} have been set!
     */
    private void pickPlayerToStart() {
        if (randy.nextInt(2) == 0) {
            //nextTurn = playerTwo.getPlayerID();
            playerOneTurn = true;
        } else {
            //nextTurn = playerOne.getPlayerID();
            playerOneTurn = false;
        }
    }

    /**
     * Running the core game logic. It waits for a second player to be assigned
     * to the match before starting It then waits until the marble count has
     * reached zero and will declare the winner
     *
     * @param humanMode boolean declaring if {@link GameLogicEngine#playerOne}
     * is playing against a human (true) or a cpu (false).
     */
    private void runCoreGameLogic(boolean humanMode) {
        System.out.println("runCoreGameLogic started");
        if (humanMode) { //Human player mode
            while (true) {
                //System.out.println("Searching for a second player!"); //this line is needed in order for the true-while loop to run, otherwise the workload is too little and the priority of the thread is so low it won't be executed (?)
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (playerTwo != null) { //wait for a second player to be assigned
                    System.out.println("Found a second player!");
                    pickPlayerToStart(); //pick a player to start
                    updateNextTurn(); //update the variable that the players ID will be checked against
                    break;
                }
            }
            playerOne.addMessage("Match found!\nThe total amount of marbles is " + totalMarbles);
            playerTwo.addMessage("Match found!\nThe total amount of marbles is " + totalMarbles);
            if (playerOne.getPlayerID() == nextTurn) { //tell the players who is starting the game
                playerOne.addMessage("It is your turn.");
                playerTwo.addMessage("It is the other players turn.");
            } else {
                playerTwo.addMessage("It is your turn.");
                playerOne.addMessage("It is the other players turn.");
            }
            while (!isGameOver()) {
                float busy = 1;
                if (currentMarbles <= 0) {
                    System.out.println("A winner has been found.");
                    //the player who's turn it was loses, in other words if we update the next turn one more time we have the ID of the winning player
                    //updateNextTurn();
                    if (playerOne.getPlayerID() == nextTurn) {
                        System.out.println("Player One has won.");
                        playerOne.addMessage("You have won the game!");
                        playerTwo.addMessage("You have lost the game!");
                    } else {
                        System.out.println("Player Two has won.");
                        playerTwo.addMessage("You have won the game!");
                        playerOne.addMessage("You have lost the game!");
                    }
                    //tell the player with playerID == nextTurn that they have won, and the other player that they have lost and then exit the match
                    gameOver = true;
                    String message = "The match has ended, you can now start a new game!";
                    playerOne.addMessage(message);
                    playerTwo.addMessage(message);
                    //myServer.removeMatch(matchID); //removes iteself from the list of matches
                    Thread.currentThread().interrupt(); //close this thread
                    return;
                    //END the game here
                }
            }
        } else { //Cpu game mode
            System.out.println("Starting a game against the computer.");
            playerOne.addMessage("Match with the computer has started.");
            playerOne.addMessage("Match found!\nThe total amount of marbles is " + totalMarbles);
            ComputerControlledPlayer cpu = new ComputerControlledPlayer(totalMarbles, this); //assign new computer
            playerTwo = cpu; //set player 2 as the cpu
            Thread t = new Thread(cpu); //new therad with the cpu on it
            t.start(); //start the thread
            pickPlayerToStart(); //pick a player to start
            updateNextTurn(); //update the variable that the players ID will be checked against
            if (playerOne.getPlayerID() == nextTurn) { //tell the players who is starting the game
                playerOne.addMessage("It is your turn.");
            } else {
                playerOne.addMessage("It is the other players turn.");
            }
            while (!isGameOver()) {
                float busy = 1;
                if (currentMarbles <= 0) {
                    System.out.println("A winner has been found.");
                    //the player who's turn it was loses, in other words if we update the next turn one more time we have the ID of the winning player
                    //updateNextTurn();
                    if (playerOne.getPlayerID() == nextTurn) {
                        System.out.println("Player One has won.");
                        playerOne.addMessage("You have won the game!");
                    } else {
                        System.out.println("Computer has won.");
                        playerOne.addMessage("You have lost the game!");
                    }
                    //tell the player with playerID == nextTurn that they have won, and the other player that they have lost and then exit the match
                    gameOver = true;
                    String message = "The match has ended, you can now start a new game!";
                    playerOne.addMessage(message);
                    playerTwo = null;
                    Thread.currentThread().interrupt(); //close this thread
                    return;
                    //myServer.removeMatch(matchID); //removes iteself from the list of matches
                    //END the game here
                }
            }
        }
    }

    /**
     * Update the value holding who's the next turn it is to make a move
     */
    private void updateNextTurn() {
        if (playerOneTurn) { //if it is the first players turn
            nextTurn = playerTwo.getPlayerID();
            playerOneTurn = false;
        } else {
            nextTurn = playerOne.getPlayerID();
            playerOneTurn = true;
        }
    }

    /**
     * Used to check if it is the players turn
     *
     * @param playerID
     * @return
     */
    public boolean checkIfItIsMyTurn(int playerID) {
        return nextTurn == playerID;
    }

    /**
     * Lets a player, defined by the playerID, pick an amount of marbles. First
     * determines if the chosen amount is a legal move and then if the playerID
     * matches with the player who is up next
     *
     * @param playerID
     * @param amount
     * @return
     */
    public boolean pickMarbles(int playerID, int amount) {
        if (amount == Integer.MAX_VALUE) { //the player is giving up
            gameOver = true;
            getPlayerByID(playerID).addMessage("You have given up!");
            currentMarbles = 0;
            return true;
        }
        try {
            if (playerID == nextTurn) { //it is the correct player making a move
                if (determineLegalMove(amount)) {
                    System.out.print("The move was valid and the correct player made it.");
                    setCurrentMarbles(currentMarbles - amount); //deduct the selected amount
                    updateNextTurn(); //update the value expected for the next player ID
                    return true;
                } else { //correct player making a wrong move
                    System.out.println("Correct player made an invalid move");
                    getPlayerByID(playerID).addMessage("This is an invalid move.\nYou may only pick a number greater than one and smaller than half of the marbles left!\nThe total amount of marbles is " + currentMarbles);
                    return false;
                }
            } else { //wrong player
                System.out.println("Wrong player trying to make a move.");
                getPlayerByID(playerID).addMessage("It is not currently your turn. Please wait for the other player to make their move!");
                return false;
            }
        } catch (NullPointerException e) {
            System.out.println("Player not found");
            return false;
        }
    }

    /**
     * Thread.start()
     */
    @Override
    public void run() {
        if (playerOne.getChosenMode() == GameEngineRemoteInterface.HUMAN_MODE) {
            runCoreGameLogic(true);
        } else {
            runCoreGameLogic(false);
        }
    }

    /**
     * Find the player by their unique identifier
     *
     * @param playerID
     * @return
     */
    private Player getPlayerByID(int playerID) {
        try {
            if (playerOne.getPlayerID() == playerID) {
                return playerOne;
            } else if (playerTwo.getPlayerID() == playerID) {
                return playerTwo;
            } else {
                return null;
            }
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * @return the playerOne
     */
    public Player getPlayerOne() {
        return playerOne;
    }

    /**
     * @param playerOne the playerOne to set
     */
    public void setPlayerOne(Player playerOne) {
        this.playerOne = playerOne;
    }

    /**
     * @return the playerTwo
     */
    public Player getPlayerTwo() {
        return playerTwo;
    }

    /**
     * @param playerTwo the playerTwo to set
     */
    public void setPlayerTwo(Player playerTwo) {
        this.playerTwo = playerTwo;
    }

    /**
     *
     * @param playerID playerTwo ID
     */
    public void setPlayerTwo(int playerID) {
        this.playerTwo = new Player(playerID);
    }

    /**
     * @return the chosenMode
     */
    public int getChosenMode() {
        return chosenMode;
    }

    /**
     * @param chosenMode the chosenMode to set
     */
    public void setChosenMode(int chosenMode) {
        this.chosenMode = chosenMode;
    }

    /**
     * @return the chosenDifficulty
     */
    public int getChosenDifficulty() {
        return chosenDifficulty;
    }

    /**
     * @param chosenDifficulty the chosenDifficulty to set
     */
    public void setChosenDifficulty(int chosenDifficulty) {
        this.chosenDifficulty = chosenDifficulty;
    }

    /**
     * @return the gameOver
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * @return the matchID
     */
    public int getMatchID() {
        return matchID;
    }

    /**
     * @param matchID the matchID to set
     */
    public void setMatchID(int matchID) {
        this.matchID = matchID;
    }
}
