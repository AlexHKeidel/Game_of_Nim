package RMIGameOfNim;

import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GameClient class that communicates with the server via RMI
 *
 * @author Alexander.Keidel@go.edgehill.ac.uk
 */
public class GameClient extends UnicastRemoteObject implements Runnable {

    private int playerNumber;
    private static GameEngineRemoteInterface gei; //Interface used by the GameServer
    private GameClientJFrame myFrame;

    /**
     * @return the gei
     */
    public static GameEngineRemoteInterface getGei() {
        return gei;
    }

    /**
     * @param aGei the gei to set
     */
    public static void setGei(GameEngineRemoteInterface aGei) {
        gei = aGei;
    }

    /**
     * Default constructor used to connect the client to the server via the
     * naming lookup tool, with the help of a String specified in @see
     * RMIGameOfNim.GameEngineInterface
     *
     * @throws RemoteException
     */
    public GameClient(GameClientJFrame myFrame) throws RemoteException {
        try {
            gei = (GameEngineRemoteInterface) Naming.lookup(GameEngineRemoteInterface.FIXEDURL); //Connecting to the server with the help of the naming lookup library
            this.myFrame = myFrame;
        } catch (Exception e) {
            System.out.println("Error creating Client");
            e.printStackTrace();
        }
    }

    public GameEngineRemoteInterface getInterface() {
        return gei;
    }

    /**
     * @return the playerNumber
     */
    public int getPlayerNumber() {
        return playerNumber;
    }

    /**
     * @param playerNumber the playerNumber to set
     */
    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    /**
     * Update the client interface with messages from the server passed via a
     * String
     *
     * @throws RemoteException
     */
    private void listenForServerMessages() throws RemoteException {
        while (true) {
            try {
                myFrame.updateMessageLog(gei.communicateWithServer(playerNumber));
            } catch (NullPointerException e) {
                System.out.println("Error: no such player found on the server");
            } catch (ConnectException e) {
                myFrame.updateMessageLog("Connection to the server has been lost.");
                myFrame.setInterfaceState(myFrame.DISCONNECTED_STATE);
            }
        }
    }

    @Override
    public void run() {
        try {
            listenForServerMessages();
        } catch (RemoteException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
