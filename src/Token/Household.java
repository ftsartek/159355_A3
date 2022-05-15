package Token;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Household extends Thread {

    // Class variables
    private boolean hasToken;
    CopyOnWriteArrayList<Integer> requested;
    CopyOnWriteArrayList<Integer> granted;
    private int requestNumber;
    private boolean inCS;
    boolean wantedShopsDone;
    private int id;
    private int shopsComplete;
    protected LinkedBlockingQueue<Message> receiveQueue = new LinkedBlockingQueue<>();
    protected LinkedBlockingQueue<Message> tokenQueue = new LinkedBlockingQueue<>();
    Messenger messenger;
    Shop shop;
    HouseholdListener listener;

    // Constructor
    public Household(boolean token, int id, Messenger messenger, Shop shop) {
        this.hasToken = token;
        this.id = id;
        this.messenger = messenger;
        this.shop = shop;
        requested = new CopyOnWriteArrayList<>();
        granted = new CopyOnWriteArrayList<>();
        requestNumber = 0;
        inCS = token;
        shopsComplete = 0;
        listener = new HouseholdListener(this);
    }

    // Indicate that a shop has been completed, iterating the counter and if this household has completed all of its
    // shops for a week, ending the processes.
    public void completedShop() {
        shopsComplete++;
        if (shopsComplete == 5) {
            listener.interrupt();
            // Remove this household from the messenger
            messenger.removeHousehold(this);
            wantedShopsDone = true;
            System.out.println("Household " + id + " has completed all their shopping for the week.");
        } else {
            System.out.println("Household " + id + " has completed " + shopsComplete + " shops this week.");
        }
    }

    // Fills the 'requested' and 'granted' arrays with placeholder 0 values up to the household count
    public void prefillArrays() {
        for (int i = 0; i < messenger.getHouseholdCount(); i++) {
            requested.add(0);
            granted.add(0);
        }
    }

    // Creates and sends a message (via the messenger) of the requested type to the destination given
    public void send(MessageType type, int destination) {
        // Send the token, ensuring this household already has it
        if (type == MessageType.TOKEN) {
            if (hasToken) {
                Message message = new Message(MessageType.TOKEN, destination, this.id, this.granted);
                this.hasToken = false;
                messenger.forwardMessage(message);
            } else {
                System.out.println("Household " + this.id + " tried to send a token they don't have!");
            }
        }
        // Send a request message to the destination with this household's ID and request number
        else if (type == MessageType.REQUEST) {
            Message message = new Message(MessageType.REQUEST, destination, this.id, this.requestNumber);
            messenger.forwardMessage(message);
        }
    }

    private void receive() throws InterruptedException {
        // Wait until the token queue is not empty
        while (tokenQueue.size() == 0) { Thread.sleep(50); };
        // Take the token message from the queue
        Message message = tokenQueue.take();
        // Overwrite granted with the one in the message
        granted = message.getGrantedArray();
        // If the message has the token, take it
        this.hasToken = message.containsToken();
    }

    public void sendToken() {
        Random randomiser = new Random();
        // Loop as long as this household has the token && still has shops to do.
        while (this.hasToken()) {
            // If there's nobody left to shop, end the loop and kill this thread
            if (messenger.getHouseholdCount() == 0) {
                System.out.println("");
                System.out.println("Everyone has completed their shops for the week!");
                // Finalise and kill this household's threads
                this.hasToken = false;
            }
            // If there's only one household left, send the token there (this also allows the household to return it
            // to itself if it's the only one left.)
            else if (messenger.getHouseholdCount() == 1) {
                // Send to the last household
                send(MessageType.TOKEN, messenger.getLastHousehold().getID());
                this.hasToken = false;
            }
            // Handles normal situations, picking a random household (excluding itself) to pass the token to,
            else {
                int randomSelector = randomiser.nextInt(0, messenger.getHouseholdCount());
                int destinationID = messenger.getHouseholds().get(randomSelector).getID();
                // Skip this household's ID, and block the send() method if the token is already gone
                if (destinationID != this.id && this.hasToken()) {
                    if (requested.get(destinationID) > granted.get(destinationID)) {
                        if (messenger.getHouseholdByID(destinationID) != null) {
                            // Send to the randomly selected household
                            send(MessageType.TOKEN, destinationID);
                        }
                    }
                }
            }
            // Small delay to avoid saturating the CPU
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
            // Break the loop if the shops are done and the token is gone (avoids the break if for some reason the
            // token was unable to be sent)
            if (this.wantedShopsDone && !this.hasToken) {
                break;
            }
        }
    }

    // Send request messages to each household
    public void sendRequests() {
        for (int householdID : messenger.getHouseholdIDs()) {
            send(MessageType.REQUEST, householdID);
        }
    }

    // Return whether this household has the token or not
    public boolean hasToken() {
        return this.hasToken;
    }

    // Return whether this household is in CS or not
    public boolean isInCS() {
        return inCS;
    }

    // Returns this household's ID
    public int getID() {
        return this.id;
    }

    // Run method
    @Override
    public void run() {
        // Prefill the arrays and start the listener
        prefillArrays();
        listener.start();
        // Loop until wanted shops are done
        while (!wantedShopsDone) {
            try {
                Thread.sleep(50);
                // If this doesn't have the token, increment requestNumber and send the requests, then wait for responses
                if (!hasToken()) {
                    requestNumber++;
                    sendRequests();
                    receive();
                }
                inCS = true;
                shop.doShop(this);
                granted.set(id, requestNumber);
                inCS = false;
                sendToken();
            } catch (InterruptedException ignored) {}
        }
    }
}


class HouseholdListener extends Thread {

    // Class variable
    private Household household;

    // Constructor
    HouseholdListener(Household household) {
        this.household = household;
    }

    // Receive method (for the receieve queue, NOT the token queue)
    private void receive() throws InterruptedException {
        // Takes the message from the start of the queue
        Message message = household.receiveQueue.take();
        // Update highest number if the ticketnumber is higher than the current value
        household.requested.set(message.getSenderID(), message.getRequestedNumber());
        // Backup method of sending the token if it's held by this household and not yet sent
        if (household.hasToken() && !household.isInCS()) {
            household.sendToken();
        }
    }

    // Run method
    @Override
    public void run() {
        // Loop until shops are done, running receive
        while (!household.wantedShopsDone) {
            try {
                receive();
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }
    }
}