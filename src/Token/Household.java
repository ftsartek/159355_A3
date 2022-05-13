package Token;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Household extends Thread {

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

    public void completedShop() {
        shopsComplete++;
        if (shopsComplete == 5) {
            listener.interrupt();
            messenger.removeHousehold(this);
            wantedShopsDone = true;
            System.out.println("Household " + id + " has completed all their shopping for the week.");
        } else {
            System.out.println("Household " + id + " has completed " + shopsComplete + " shops this week.");
        }
    }

    public void prefillArrays() {
        for (int i = 0; i < messenger.getHouseholdCount(); i++) {
            requested.add(0);
            granted.add(0);
        }
    }

    public void send(MessageType type, int destination) {
        if (type == MessageType.TOKEN) {
            if (hasToken) {
                Message message = new Message(MessageType.TOKEN, destination, this.id, this.granted);
                this.hasToken = false;
                messenger.forwardMessage(message);
            } else {
                System.out.println("Household " + this.id + " tried to send a token they don't have!");
            }
        } else if (type == MessageType.REQUEST) {
            Message message = new Message(MessageType.REQUEST, destination, this.id, this.requestNumber);
            messenger.forwardMessage(message);
        }
    }

    private void receive() throws InterruptedException {
        // Wait until the token queue is not empty
        while (tokenQueue.size() == 0) { Thread.sleep(50); };
        // Take the token message from the queue
        Message message = tokenQueue.take();
        granted = message.getGrantedArray();
        this.hasToken = message.containsToken();
    }

    public void sendToken() {
        Random randomiser = new Random();
        while(this.hasToken()) {
            if (messenger.getHouseholdCount() == 1) {
                System.out.println("Nobody left to pass the token to!");
                if (!this.wantedShopsDone) {
                    send(MessageType.TOKEN, this.id);
                }
                this.hasToken = false;
            } else {

                int randomSelector = randomiser.nextInt(0, messenger.getHouseholdCount());
                int destinationID = messenger.getHouseholds().get(randomSelector).getID();
                // Skip this household's ID, and block the send() method if the token is already gone
                if (destinationID != this.id && this.hasToken()) {
                    if (requested.get(destinationID) > granted.get(destinationID)) {
                        if (messenger.getHouseholdByID(destinationID) != null) {
                            send(MessageType.TOKEN, destinationID);
                        }
                    }
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }
    }

    public void sendRequests() {
        for (int householdID : messenger.getHouseholdIDs()) {
            send(MessageType.REQUEST, householdID);
        }
    }

    public boolean hasToken() {
        return this.hasToken;
    }

    public int getRequestedNumber() {
        return this.requestNumber;
    }

    public boolean isInCS() {
        return inCS;
    }

    public int getID() {
        return this.id;
    }

    @Override
    public void run() {
        prefillArrays();
        listener.start();
        while (!wantedShopsDone) {
            try {
                Thread.sleep(50);
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

    private int sender;
    private int requestedNumber;
    private Household household;

    HouseholdListener(Household household) {
        this.household = household;
    }

    private void receive() throws InterruptedException {
        // Takes the message from the start of the queue
        Message message = household.receiveQueue.take();
        // Update highest number if the ticketnumber is higher than the current value
        household.requested.set(message.getSenderID(), message.getRequestedNumber());
        if (household.hasToken() && !household.isInCS()) {
            household.sendToken();
        }
    }

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