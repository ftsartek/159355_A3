package Permission;

public class Message {

    private MessageType messageType;
    private int senderID;
    private int destinationID;
    private int ticketNumber;

    public Message(MessageType messageType, int destinationID, int senderID, int ticketNumber) {
        this.messageType = messageType;
        this.destinationID = destinationID;
        this.senderID = senderID;
        this.ticketNumber = ticketNumber;
    }

    public Message(MessageType messageType, int destinationID, int senderID) {
        this.messageType = messageType;
        this.destinationID = destinationID;
        this.senderID = senderID;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }

    public int getSenderID() {
        return this.senderID;
    }

    public int getDestinationID() {
        return this.destinationID;
    }

    public int getTicketNumber() {
        return this.ticketNumber;
    }

}


