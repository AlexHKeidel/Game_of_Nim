package RMIGameOfNim;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * GameServer which implements the remote interface GameEngineInterface It
 * controls the main flow of the game and takes input from the players
 *
 * @author Alexander.Keidel@go.edgehill.ac.uk
 */
public class GameServer extends UnicastRemoteObject implements GameEngineRemoteInterface {

    private List<String> commands = new LinkedList<>(); //array of legal commands that user may use to interface with the game
    private int playerCount = 0; //player count, used to give players their number, which is used to identify them later (ID)
    private int matchCount = 0; //match count, used to give unique ids to matches
    private List<Player> registeredPlayers = new LinkedList<>(); //list of all players, including their IDs, chosen modes and if they are ready to play
    private List<GameLogicEngine> matches = new LinkedList<>(); //list of all matches registered on this server
    private static int port = 1099; //standart port
    private static GameServerJFrame myFrame;

    /**
     * Standart comstructor Do not use, as this does not pass a reference to the
     * JFrame needed to update the Server GUI.
     *
     * @throws RemoteException
     * @deprecated
     */
    @Deprecated
    public GameServer() throws RemoteException {
        try {
            initCommandsList(); //initialise the list of legal commands
            LocateRegistry.createRegistry(port); //locate and create the registry
            Naming.bind(FIXEDURL, this); //bind the server, using the fixed url provided from the interface
            System.out.println("RMI registry on port " + port + "\n");
        } catch (Exception e) {
            System.out.println("Error when starting the RMI registry");
            e.printStackTrace();
        }
    }

    /**
     * Use this constructor as it passes a reference to the JFrame, which is
     * needed to update the server's GUI.
     *
     * @param myFrame
     * @throws RemoteException
     */
    public GameServer(GameServerJFrame myFrame) throws RemoteException {
        try {
            initCommandsList(); //initialise the list of legal commands
            LocateRegistry.createRegistry(port); //locate and create the registry
            Naming.bind(FIXEDURL, this); //bind the server, using the fixed url provided from the interface
            System.out.println("RMI registry on port " + port + "\n");
            this.myFrame = myFrame;
        } catch (Exception e) {
            System.out.println("Error when starting the RMI registry");
            e.printStackTrace();
        }
    }

    /**
     * Makes the move for a player using their playerID and the amount of
     * marbles they wish to pick Then determines which match they are in and
     * makes the move and adds the messages to their message queue.
     *
     * @param playerNumber
     * @param move
     * @return Message to both players once the move has been successfully made,
     * otherwise tells the user that their move was invalid
     */
    @Override
    public String makeMove(int playerNumber, int move) {
        try {
            for (GameLogicEngine match : matches) {
                if (match.getPlayerOne().getPlayerID() == playerNumber) { //found the match we have to make the move in
                    if (match.pickMarbles(playerNumber, move)) {
                        match.getPlayerOne().addMessage(move + " marbles picked.\nIt is now the other players turn.");
                        match.getPlayerTwo().addMessage("The other player has taken " + move + " marbles.\nThere are now " + match.getCurrentMarbles() + " marbles left.\nMake your move!");
                        return "";
                    } else {
                        return "Illegal move, try again";
                    }
                } else if (match.getPlayerTwo().getPlayerID() == playerNumber) {
                    if (match.pickMarbles(playerNumber, move)) {
                        match.getPlayerTwo().addMessage(move + " marbles picked.\nIt is now the other players turn.");
                        match.getPlayerOne().addMessage("The other player has taken " + move + " marbles.\nThere are now " + match.getCurrentMarbles() + " marbles left.\nMake your move!");
                        return "";
                    } else {
                        return "Illegal move, try again.";
                    }
                }
            }
            return "You are not currently in a match.";
        } catch (NullPointerException e) {
            return "Error trying to make a move. You might not be assigned to a match";
        }
    }

    @Override
    public void test() throws RemoteException {
        System.out.println("Test successful");
        myFrame.appendServerLog("Test successful");
    }

    /**
     * Registers the player with a new player number.
     *
     * @return
     * @throws RemoteException
     */
    @Override
    public int registerPlayer() throws RemoteException {
        playerCount++;
        registeredPlayers.add(new Player(playerCount)); //add the given ID to the list of players
        myFrame.appendServerLog("A new player has registered.\nGiven the player number " + playerCount);
        return playerCount;
    }

    /**
     * Executes the command sent by the player
     *
     * @param playerNumber
     * @param command
     * @return
     * @throws RemoteException
     */
    @Override
    public String executeCommand(int playerNumber, String command) throws RemoteException {
        if (commands.contains(command)) {
            myFrame.appendServerLog(command + " received from Player #" + playerNumber);
            switch (command) {
                case "help":
                    return getFormattedCommands();
                case "start":
                    //Give the ready to start command from the player, should wait for both players to be ready
                    if (assignPlayerToMatch(playerNumber)) {
                        return "You will be matched.";
                    }
                    return "Error assigning you to a match";

                case "human":
                    /**
                     * The player has chosen to play against another human
                     * player so they should be matched to the next player
                     * waiting to play (needs a queue system)
                     */
                    if (setGameModeChoice(playerNumber, HUMAN_MODE)) {
                        return "Successfully selected Human mode for your next game.";
                    }

                    return "Error when selecting Human mode.";

                case "cpu":
                    /**
                     * The player has chosen to play against the cpu. They
                     * should immediatly get a cpu match!
                     */
                    if (setGameModeChoice(playerNumber, CPU_MODE)) {
                        return "Successfully selected CPU mode for your next game.";
                    }
                    return "Error when selecting CPU mode.";

                case "exit":
                    /**
                     * The player has chosen to exit the game, immediatly
                     * forfeiting and disconnecting from the server
                     */
                    makeMove(playerNumber, Integer.MAX_VALUE); //call the make move method with the code to forfeit
                    return "Exited from game.";

                case "hard":
                    /**
                     * The player wants to change his chosen game mode to hard
                     */
                    if (setDifficultyChoice(playerNumber, HARD_DIFFICULTY)) {
                        return "Hard mode chosen.";
                    }
                    return "Error choosing hard mode.";

                case "easy":
                    /**
                     * The player wants to change to the easy (default) mode
                     */
                    if (setDifficultyChoice(playerNumber, EASY_DIFFICULYT)) {
                        return "Easy mode chosen.";
                    }
                    return "Error choosing easy mode.";

                default:
                    myFrame.appendServerLog("Unsupported command: " + command + " received from Player #" + playerNumber + "\n Please implement this command is it is on the supported commands list!");
                    return command + " should be implemented, but is not. Please contact the support about this.";
            }
        }
        //Presume that the command is the chosen number for the next move.
        myFrame.appendServerLog("Player #" + playerNumber + " made move: " + command);
        try {
            return makeMove(playerNumber, Integer.parseInt(command));
        } catch (NumberFormatException e) {
            findPlayerById(playerNumber).addMessage("Not a valid command.");
            return "Try typing \"help\"";
        }
        //something must have gone wrong.
        //myFrame.appendServerLog("Illegal command \"" + command + "\" received from Player #" + playerNumber);
        //return "\"" + command + "\"" + " is is not a legal command. Try \"help\" for a list of commands.";
    }

    /**
     * Initialise the list of legal commands taken from the remote interface
     */
    private void initCommandsList() {
        commands.addAll(Arrays.asList(COMMANDS));
    }

    /**
     * Finds the player via their playerID and sets their chosen game mode
     *
     * @param playerID
     * @param mode selected from final values in the remote interface
     * @return if the mode has been successfully set, if this returns false the
     * playerID has not been found
     */
    private boolean setGameModeChoice(int playerID, int mode) {
        if (mode == CPU_MODE) { //set choice to cpu for this player
            for (Player p : registeredPlayers) {
                if (p.getPlayerID() == playerID) {
                    p.setChosenMode(mode);
                    return true;
                }
            }
        } else if (mode == HUMAN_MODE) {
            for (Player p : registeredPlayers) {
                if (p.getPlayerID() == playerID) {
                    p.setChosenMode(mode);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sets the difficulty choice for a specific player
     *
     * @param playerID
     * @param difficulty
     * @return true if successful, otherwise false
     */
    private boolean setDifficultyChoice(int playerID, int difficulty) {
        for (Player p : registeredPlayers) {
            if (p.getPlayerID() == playerID) {
                p.setChosenDifficulty(difficulty);
                return true;
            }
        }
        return false;
    }

    private int assignNewMatchID() {
        matchCount++;
        return matchCount;
    }

    /**
     * Checks the list of matches for a match where the second player has not
     * been set yet
     *
     * @return a match missing a second player, with matching difficulty level
     */
    private boolean assignPlayerToMatch(int playerID) {
        try {
            Player requestee = null;
            for (Player p : registeredPlayers) { //find and assign the player using their ID
                if (p.getPlayerID() == playerID) {
                    requestee = p;
                    break;
                }
            }
            //Check if the player is already assigned to a match as player one
            for (GameLogicEngine match : matches) {
                if (match.getPlayerOne().getPlayerID() == playerID) { //the player is already assigned to a match
                    if (match.isGameOver()) {
                        break; //the match is already over, let the create a new one
                    }
                    findPlayerById(playerID).addMessage("You are already in the queue for a match.");
                    return false;
                }
            }
            //Check if the player would like to play against the cpu
            if (requestee.getChosenMode() == GameEngineRemoteInterface.CPU_MODE) {
                GameLogicEngine match = new GameLogicEngine(assignNewMatchID(), this, playerID, determineChosenDifficulty(requestee), true);
                Thread t = new Thread(match);
                t.start();
                matches.add(match);
                findPlayerById(playerID).addMessage("Computer controlled game started!");
                return true;
            }

            //Check if there are any matches with players waiting to play
            if (matches.isEmpty()) { //there are no matches
                GameLogicEngine match = new GameLogicEngine(assignNewMatchID(), this, playerID, determineChosenDifficulty(requestee), false);
                Thread t = new Thread(match);
                t.start();
                matches.add(match);
                findPlayerById(playerID).addMessage("A new match lobby has been created for you.");
                return true;
            }

            //Check if they have the same settings as another player and can be matched with them
            for (GameLogicEngine match : matches) {
                if (match.getPlayerTwo() == null && match.getPlayerOne().getChosenDifficulty() == requestee.getChosenDifficulty()) { //a second player is missing and
                    match.setPlayerTwo(playerID); //assign the player to the match with the correct 
                    findPlayerById(playerID).addMessage("You have been assigned to an existing match lobby!");
                    return true;
                }
            }

            //No available matches, create a new one
            GameLogicEngine match = new GameLogicEngine(assignNewMatchID(), this, playerID, determineChosenDifficulty(requestee), false);
            Thread t = new Thread(match);
            t.start();
            matches.add(match);
            findPlayerById(playerID).addMessage("A new match lobby has been created for you.");
            return true;

        } catch (NullPointerException e) {
            System.out.println("PlayerID had no match when checking for available matches." + "PlayerID = " + playerID);
            e.printStackTrace();
            System.out.println("registeredPlayers = " + registeredPlayers.toString());
            return false;
        }
    }

    /**
     * @deprecated Does not work! Do not use! Remove a match via a reference
     * @param match
     */
    public void removeMatch(GameLogicEngine match) {
        try {
            System.out.println("Removing match #" + match.getMatchID());
            matches.remove(match);
        } catch (Exception e) {
            System.out.println("Could not remove match.");
            e.printStackTrace();
        }
    }

    /**
     * Remove a match via its ID.
     *
     * @param matchID
     */
    public void removeMatch(int matchID) {
        try {
            for (GameLogicEngine match : matches) {
                if (match.getMatchID() == matchID) {
                    matches.remove(match);
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("Could not remove match.");
            e.printStackTrace();
        }
    }

    /**
     *
     * @param player
     * @return true for hard mode, false for easy mode
     */
    private boolean determineChosenDifficulty(Player player) {
        if (player.getChosenDifficulty() == HARD_DIFFICULTY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Applies the player to the match making queue
     *
     * @param playerID
     * @return
     */
    private boolean applyForMatchmaking(int playerID) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @deprecated @param playerOneID
     * @param playerTwoID
     * @param difficulty
     */
    private void startGameSession(int playerOneID, int playerTwoID, int difficulty) {
        GameLogicEngine engine = new GameLogicEngine(assignNewMatchID(), this, playerOneID, playerTwoID, difficulty);
        Thread t = new Thread(engine);
        t.start();
    }

    /**
     *
     * @return String containing all valid commands supported by the server,
     * seperated by new lines
     */
    private String getFormattedCommands() {
        String formattedCommands = "";
        for (String command : COMMAND_DESCRIPTIONS) {
            formattedCommands += command + "\n";
        }
        return formattedCommands;
    }

    /**
     * finds a player in the list of players contained on the server
     *
     * @param playerID
     * @return
     */
    private Player findPlayerById(int playerID) {
        try {
            for (Player p : registeredPlayers) {
                if (p.getPlayerID() == playerID) {
                    return p;
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Player could not be found by using their ID");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a reference of the player in a match
     *
     * @param playerID
     * @return
     */
    private Player findPlayerInMatchByID(int playerID) {
        try {
            for (GameLogicEngine match : matches) {
                if (match.getPlayerOne().getPlayerID() == playerID) {
                    return match.getPlayerOne();
                }
                if (match.getPlayerTwo().getPlayerID() == playerID) {
                    return match.getPlayerTwo();
                }
            }
        } catch (NullPointerException e) {
        }
        return null;
    }

    /**
     * Communicate with the client using their playernumber Finds the message
     * associated with the player and removes it from the list and then returns
     * it
     *
     * @param playerNumber
     * @return a String containing a message for this player
     * @throws RemoteException
     */
    @Override
    public synchronized String communicateWithServer(int playerNumber) throws RemoteException {
        String message = "";
        try {
            Player player = findPlayerInMatchByID(playerNumber);
            if (!(player.getMessages() == null) && !player.getMessages().isEmpty()) {
                message = player.getMessages().get(0); //get the first item of the list and assign it to the String
                player.getMessages().remove(0); //remove the item from the list
                System.out.println("Sending message \"" + message + "\" to Player#" + playerNumber);
                myFrame.appendServerLog("Sending message \"" + message + "\" to Player#" + playerNumber);
            }
        } catch (NullPointerException e) {
            Player player = findPlayerById(playerNumber);
            if (!(player.getMessages() == null) && !player.getMessages().isEmpty()) {
                message = player.getMessages().get(player.getMessages().size() - 1); //get the last item of the list and assign it to the String
                player.getMessages().remove(player.getMessages().size() - 1); //remove the item from the list
                System.out.println("Sending message \"" + message + "\" to Player#" + playerNumber);
                myFrame.appendServerLog("Sending message \"" + message + "\" to Player#" + playerNumber);
            }
            return message;
        }
        return message;
    }
}
