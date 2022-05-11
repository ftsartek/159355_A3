package Permission;


import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

// Main household class
public class Household extends Thread {

    // Class variables
    protected int ticketNumber;
    protected int highestNumber;
    protected int id;
    protected CopyOnWriteArrayList<Integer> deferred;
    protected CopyOnWriteArrayList<Integer> awaitingReply;
    protected boolean requestCS = false;
    protected boolean wantedShopsDone = false;
    protected LinkedBlockingQueue<Message> receiveQueue = new LinkedBlockingQueue<>();
    private HouseholdListener listener;
    private int shopsComplete;
    Messenger messenger;
    Shop shop;
    Random rng = new Random();


    // Constructor, requiring an ID, messenger and shop
    public Household (int id, Messenger messenger, Shop shop) {
        this.id = id;
        deferred = new CopyOnWriteArrayList<>();
        awaitingReply = new CopyOnWriteArrayList<>();
        ticketNumber = 0;
        highestNumber = 0;
        shopsComplete = 0;
        this.messenger = messenger;
        this.shop = shop;
        // Create and start the listener.
        listener = new HouseholdListener(this);
    }


    public void send(MessageType msgType, int destination) {
        Message message;
        if (msgType == MessageType.REQUEST) {
            //System.out.println("Household " + id + " sending a request message to household " + destination);
            message = new Message(msgType, destination, this.id, this.ticketNumber);
            addAwaiting(destination);
            messenger.forwardMessage(message);
        } else if (msgType == MessageType.REPLY) {
            //System.out.println("Household " + id + " sending a reply message to household " + destination);
            message = new Message(msgType, destination, this.id);
            messenger.forwardMessage(message);
        }
    }

    // Called when the household has completed however many shops they want to do
    public void wantedShopsComplete() {
        this.wantedShopsDone = true;
        messenger.removeHousehold(this);
        System.out.println("Household " + this.id + " has completed their shopping for the week.");
    }

    // Increments the shops complete count
    public void completedShop() {
        shopsComplete++;
    }

    // Returns this household's ID
    public int getID() {
        return this.id;
    }

    // Synchronised method to add a household to the awaitingReply array
    synchronized public void addAwaiting(int i) {
        awaitingReply.add(i);
    }

    // Synchronised method to remove a household from the awaitingReply array
    synchronized public void removeAwaiting(int i) {
        awaitingReply.remove((Integer) i);
    }

    // Synchronised method returning the number of households this one is waiting for a reply from
    synchronized public int awaitCount() {
        return awaitingReply.size();
    }

    // Synchronised method to update the highest number seen yet
    synchronized protected void updateHighestNumber(int i) {
        highestNumber = i;
    }

    // Synchronised method to update the household's ticket number
    synchronized protected void updateTicketNumber() {
        // First round, highestNumber is 0 so we'll randomise the ticket number to start with
        if (highestNumber == 0) {
            ticketNumber = rng.nextInt(1, 26);
        } else {
            ticketNumber = highestNumber + 1;
        }
    }

    // Synchronised method to trigger a reply to all households in the deferred array
    synchronized protected void replyToAllDeferred() {
        for (Integer householdID : deferred) {
            send(MessageType.REPLY, householdID);
            deferred.remove((Integer) householdID);
        }
    }

    // Synchronised method to add a household to the deferred array
    synchronized protected void addToDeferred(int id) {
        deferred.add(id);
    }

    // Run method
    @Override
    public void run() {
        // Starts the listener
        listener.start();
        // Loop until the household has completed as many shops as they want to
        while (!wantedShopsDone) {
            try {
                Thread.sleep(50);
                // Request the critical section
                requestCS = true;
                // Update the ticket number
                updateTicketNumber();
                // Send a request to all other households
                for (int id : messenger.getHouseholdIDs()) {
                    if (id != this.id) {
                        send(MessageType.REQUEST, id);
                    }
                }
                // Await all replies, loop until they're all received
                while (awaitCount() > 0) {
                    Thread.sleep(50);
                }
                // Enter critical section
                shop.doShop(this);
                // Leave critical section and indicate as such
                requestCS = false;
                // Reply to all deferred households
                replyToAllDeferred();
            } catch (InterruptedException ignored) {}
            // End the loop if 5 shops have been completed
            if (shopsComplete >= 5) {
                this.wantedShopsComplete();
            } else {
                // Indicate how many shops have already been completed
                System.out.println("Household " + this.id + " has completed " + this.shopsComplete + " shops so far.");
            }
        }
        // Ensure the listener closes
        this.listener.interrupt();
    }
}


// Listener class used to receive messages
class HouseholdListener extends Thread {

    // The household object that owns this listener
    private Household household;

    //Constructor
    public HouseholdListener(Household household) {
        this.household = household;
    }

    // Receive method
    private void receive() throws InterruptedException {
        // Takes the message from the start of the queue
        Message message = household.receiveQueue.take();
        // Handle REQUEST type messages
        if (message.getMessageType() == MessageType.REQUEST) {
            // Update highest number if the ticketnumber is higher than the current value
            if (message.getTicketNumber() > household.highestNumber) {
                household.updateHighestNumber(message.getTicketNumber());
            }
            // Send a reply if the other household should go first
            if (!household.requestCS || (message.getTicketNumber() < household.ticketNumber ||
                    (message.getTicketNumber() == household.ticketNumber && message.getSenderID() < household.id))) {
                household.send(MessageType.REPLY, message.getSenderID());
            } else { // Otherwise add the household to the deferred array
                household.addToDeferred(message.getSenderID());
            }
        // Handle REPLY type messages
        } else if (message.getMessageType() == MessageType.REPLY) {
            // Remove households from the awaiting reply array once received
            household.removeAwaiting(message.getSenderID());
        }
    }

    // Run the listener until the owner household has completed all their shops
    @Override
    public void run() {
        while (!household.wantedShopsDone) {
            try {
                receive();
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }
    }
}
