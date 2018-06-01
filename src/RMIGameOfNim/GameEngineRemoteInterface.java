package RMIGameOfNim;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The remote interface used both by the server and clients Includes shared
 * variables and methods that allow the client to execute commands on the server
 * and receive update messages, for example used to tell the user the status of
 * their operation or updates on the status of their current match
 *
 * @author Alexander.Keidel@go.edgehill.ac.uk
 */
public interface GameEngineRemoteInterface extends Remote {

    public static final String FIXEDURL = "rmi://localhost/RMIGameServer"; //fixed URL used with the Naming lookup library

    public static final int HUMAN_MODE = 0; //chosen when wanting to play against a human player

    public static final int CPU_MODE = 1; //chosen when wanting to play against the server cpu

    public static final int EASY_DIFFICULYT = 0; //chosen when wanting to play with between 2 and 20 marbles

    public static final int HARD_DIFFICULTY = 1; //chosen when wanting to play with between 2 and 100 marbles

    public static final String[] COMMANDS = {"help", "start", "human", "cpu", "exit", "hard", "easy"};

    public static final String[] COMMAND_DESCRIPTIONS = {"help - shows a list of the available commands", "start - tells the server that you are ready to play", "human - tells the server that you wish to play against a human player", "cpu - tells the server that you wish to play against a computer controlled opponent", "exit - exit the current game session", "hard - chooses hard mode: 2 to 100 marbles", "easy - chooses easy mode: 2 to 20 marbles"};

    /**
     * Registers a player with the server
     *
     * @return Integer value of the unique identifier then used by the client to
     * make commands
     * @throws RemoteException
     */
    public int registerPlayer() throws RemoteException;

    /**
     * @deprecated Tries to make a move in the match that the player has been
     * assigned to using their unique identifier and the move they wish to make
     * @param playerNumber
     * @param move
     * @return
     * @throws RemoteException
     */
    public String makeMove(int playerNumber, int move) throws RemoteException;

    /**
     * @deprecated Test method used to test the client-server connection
     *
     * @throws RemoteException
     */
    public void test() throws RemoteException;

    /**
     * Execute a command on the server by sending the unique player ID and the
     * command the user wishes to execute. The server will then evaluate the
     * command, execute it if it is verified and returns a string back to the
     * coresponding client telling them the status of their operation
     *
     * @param playerNumber Unique player ID
     * @param command Command to be executed by the server
     * @return String explaining the status of the operation, i.e. success or
     * failure
     * @throws RemoteException
     */
    public String executeCommand(int playerNumber, String command) throws RemoteException;

    /**
     * Method used to receive update messages from the server by telling the
     * server the assigned unique player number and then receiving a message
     * from the list / queue of messages for this specific client
     *
     * @param playerNumber Unique player ID
     * @return Message to be displayed in the client interface
     * @throws RemoteException
     */
    public String communicateWithServer(int playerNumber) throws RemoteException;
}
