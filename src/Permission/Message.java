package Permission;

public class Message {

    // Class variables for passing along message details
    private MessageType messageType;
    private int senderID;
    private int destinationID;
    private int ticketNumber;

    // Constructor used to create request type methods
    public Message(MessageType messageType, int destinationID, int senderID, int ticketNumber) {
        this.messageType = messageType;
        this.destinationID = destinationID;
        this.senderID = senderID;
        this.ticketNumber = ticketNumber;
    }

    // Constructor used to create reply type methods
    public Message(MessageType messageType, int destinationID, int senderID) {
        this.messageType = messageType;
        this.destinationID = destinationID;
        this.senderID = senderID;
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
    public int getTicketNumber() {
        return this.ticketNumber;
    }

}


