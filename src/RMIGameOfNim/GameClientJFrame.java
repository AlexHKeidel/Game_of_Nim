package RMIGameOfNim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * User interface for the Client
 *
 * @author Alexander.Keidel@go.edgehill.ac.uk
 */
public class GameClientJFrame extends javax.swing.JFrame {

    private GameClient myGameClient;
    private int interfaceState;
    public final int CONNECTED_STATE = 0;
    public final int DISCONNECTED_STATE = 1;
    private final String GAME_INSTRUCTIONS = "In order to start a game please first select \"easy\" or \"hard\", then \"human\" or \"cpu\" \nto choose your\n type of enemy. Typing \"start\" will let the server know that you are ready to play!";
    private final String WELCOME_MESSAGE = "Welcome to the Game of Nim\nPress the Connect button to establish a connection to the server.";

    /**
     * Creates new form GameClientJFrame
     *
     * @throws java.rmi.RemoteException
     */
    public GameClientJFrame() throws RemoteException {
        initComponents();
        initCommandline();
        updateMessageLog(WELCOME_MESSAGE); //Display the welcome message
    }

    /**
     * Initialise the commandline by adding an Actionlistener to
     * {@link GameClientJFrame#ClientInputTextField}
     */
    private void initCommandline() {
        System.out.println("in initCommandline()");
        ClientInputTextField.addActionListener(new ActionListener() { //set up an action listener for the command line so it reponds to enter presses
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    processCommand();
                } catch (RemoteException ex) {
                    Logger.getLogger(GameClientJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    /**
     * Process the text currently displayed in
     * {@link GameClientJFrame#ClientInputTextField}
     *
     * @throws RemoteException
     */
    private void processCommand() throws RemoteException {
        String command = ClientInputTextField.getText(); //copy the text from the command line into a string
        if (command.isEmpty()) { //if there is no command ignore it
            return;
        }
        try {
            updateMessageLog(myGameClient.getInterface().executeCommand(myGameClient.getPlayerNumber(), command)); //send the command to be executed to the server, which will process it and return a message back to the user about the status of the command
            ClientInputTextField.setText(null); //reset the content of the command line
        } catch (ConnectException e) {
            updateMessageLog("The connection to the server has been lost.\nClick connect to try and reconnect.");
            setInterfaceState(DISCONNECTED_STATE);
        }
    }

    /**
     * Set the interface state, disconnected state will have
     * {@link GameClientJFrame#ClientInputTextField} and
     * {@link GameClientJFrame#SendButton} disabled, whilst the
     * {@link GameClientJFrame#ConnectButton} is enabled. Connected state is the
     * exact opposite with the conntect button being disabled, the text field
     * and send button being enabled.
     *
     * @param state
     */
    public void setInterfaceState(final int state) {
        switch (state) {
            case DISCONNECTED_STATE:
                interfaceState = DISCONNECTED_STATE;
                ConnectButton.setEnabled(true);
                SendButton.setEnabled(false);
                ClientInputTextField.setEnabled(false);
                break;

            case CONNECTED_STATE:
                interfaceState = CONNECTED_STATE;
                ConnectButton.setEnabled(false);
                SendButton.setEnabled(true);
                ClientInputTextField.setEnabled(true);
                break;
        }
    }

    /**
     * Initialise the client and receives a myGameClient number
     *
     * @throws RemoteException
     */
    private boolean connectClient() throws RemoteException {
        try {
            myGameClient = new GameClient(this);
            //create a new myGameClient object
            myGameClient.setPlayerNumber(myGameClient.getInterface().registerPlayer());//register with the server and set the myGameClient number

            if (myGameClient.getPlayerNumber() == -1) { //error code has been found, the server is full
                updateMessageLog("Connection failed: server is full.");
                return false;
            }
            setInterfaceState(CONNECTED_STATE);
            updateMessageLog("Connection initialised.\nRegistered as Player #" + myGameClient.getPlayerNumber() + "\n" + GAME_INSTRUCTIONS);
            Thread t = new Thread(myGameClient);
            t.start();
            return true;
        } catch (Exception e) {
            updateMessageLog("Connection failed.");
            setInterfaceState(DISCONNECTED_STATE);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the message log with a message detailing what is happening on the
     * server
     *
     * @param message
     */
    public void updateMessageLog(final String message) {
        if (message.isEmpty()) { //don't spam empty messages
            return;
        }
        System.out.println("Client Message Log update:\n" + message);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ClientMessageLog.append("\n" + getTheTime() + "-> " + message);
            }
        });

    }

    /**
     * Get the current time in a HH:MM format
     *
     * @return String containing the current time of day in HH:MM format
     */
    private String getTheTime() {
        String time = "" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //add the hour of day to a string
        if (time.length() == 1) { //if time is a single digit, i.e. from 1 to 9 o'clock
            time = new StringBuilder(time).insert(0, "0").toString(); //append a 0 before the hour if it is a single digit
        }
        time += ":" + Calendar.getInstance().get(Calendar.MINUTE); //append a colon and the minutes of the hour
        if (time.length() == 4) { //if the time is four digits, i.e. 10:1
            time = new StringBuilder(time).insert(3, "0").toString(); //insert the 0, so 10:1 would become 10:01
        }
        return time;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        ClientMessageLog = new javax.swing.JTextArea();
        SendButton = new javax.swing.JButton();
        ConnectButton = new javax.swing.JButton();
        ClientInputTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        ClientMessageLog.setEditable(false);
        ClientMessageLog.setColumns(20);
        ClientMessageLog.setRows(5);
        ClientMessageLog.setName("ClientMessageLog"); // NOI18N
        jScrollPane1.setViewportView(ClientMessageLog);
        ClientMessageLog.getAccessibleContext().setAccessibleName("ClientMessageLog");

        SendButton.setText("Send");
        SendButton.setEnabled(false);
        SendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendButtonActionPerformed(evt);
            }
        });

        ConnectButton.setText("Connect");
        ConnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConnectButtonActionPerformed(evt);
            }
        });

        ClientInputTextField.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(ClientInputTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(ConnectButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ClientInputTextField)
                    .addComponent(SendButton, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ConnectButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void SendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SendButtonActionPerformed
        try {
            processCommand();
        } catch (RemoteException ex) {
            Logger.getLogger(GameClientJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_SendButtonActionPerformed

    private void ConnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConnectButtonActionPerformed
        try {
            ConnectButton.setEnabled(!connectClient()); //disable the button if connect client fails
        } catch (RemoteException ex) {
            Logger.getLogger(GameClientJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_ConnectButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GameClientJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GameClientJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GameClientJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GameClientJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new GameClientJFrame().setVisible(true);
                } catch (RemoteException ex) {
                    Logger.getLogger(GameClientJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField ClientInputTextField;
    private javax.swing.JTextArea ClientMessageLog;
    private javax.swing.JButton ConnectButton;
    private javax.swing.JButton SendButton;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
