package RMIGameOfNim;

import java.util.LinkedList;
import java.util.List;

/**
 * Player class used to hold the information relevant to them, such as their ID,
 * their settings and if they are ready or in a match. Further this includes a
 * list of messages that is being used as a queue by the server to tell send
 * messages to the client GUI
 *
 * @author Alexander.Keidel@go.edgehill.ac.uk
 */
public class Player {

    private int playerID;
    private int chosenMode = GameEngineRemoteInterface.HUMAN_MODE;
    private int chosenDifficulty = GameEngineRemoteInterface.EASY_DIFFICULYT;
    private boolean ready = false;
    private boolean inMatch = false;
    private List<String> messages = new LinkedList<>();

    public Player() {
    }

    public Player(int playerID) {
        this.playerID = playerID;
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
     * @return the ready
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * @param ready the ready to set
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * @return the inMatch
     */
    public boolean isInMatch() {
        return inMatch;
    }

    /**
     * @param inMatch the inMatch to set
     */
    public void setInMatch(boolean inMatch) {
        this.inMatch = inMatch;
    }

    /**
     * @return the playerID
     */
    public int getPlayerID() {
        return playerID;
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
     * @return the messages
     */
    public List<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    /**
     * @param playerID the playerID to set
     */
    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }
}
