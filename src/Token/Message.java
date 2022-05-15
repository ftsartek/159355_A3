package Token;

import java.util.concurrent.CopyOnWriteArrayList;

public class Message {

    // Class variables for passing along message details
    private MessageType messageType;
    private int senderID;
    private int destinationID;
    private int requestedNumber;
    private CopyOnWriteArrayList<Integer> granted;

    // Constructor for Request type messages
    public Message(MessageType messageType, int destinationID, int senderID, int requestedNumber) {
        this.messageType = messageType;
        this.destinationID = destinationID;
        this.senderID = senderID;
        this.requestedNumber = requestedNumber;
    }

    // Constructor for Token type messages
    public Message(MessageType messageType, int destinationID, int senderID, CopyOnWriteArrayList<Integer> granted) {
        this.messageType = messageType;
        this.destinationID = destinationID;
        this.senderID = senderID;
        this.granted = granted;
    }

    // Returns the message type
    public MessageType getMessageType() {
        return this.messageType;
    }

    // Returns the sender household's ID
    public int getSenderID() {
        return this.senderID;
    }

    // Returns the destination household's ID
    public int getDestinationID() {
        return this.destinationID;
    }

    // Returns the sender's ticket number
    public int getRequestedNumber() {
        return this.requestedNumber;
    }

    // Returns the 'granted' array held by this message.
    public CopyOnWriteArrayList<Integer> getGrantedArray() { return this.granted; }

    // Returns true if this message contains the token, else false
    public boolean containsToken() {
        if (this.messageType == MessageType.TOKEN) {
            return true;
        }
        return false;
    }

}


