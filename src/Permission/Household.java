package Permission;


import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Household extends Thread {

    // Class variables -
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


    public Household (int id, Messenger messenger, Shop shop) {
        this.id = id;
        deferred = new CopyOnWriteArrayList<>();
        awaitingReply = new CopyOnWriteArrayList<>();
        ticketNumber = 0;
        highestNumber = 0;
        shopsComplete = 0;
        this.messenger = messenger;
        this.shop = shop;
        listener = new HouseholdListener(this);
        listener.start();
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


    public void wantedShopsComplete() {
        this.wantedShopsDone = true;
        messenger.removeHousehold(this);
    }

    public void completedShop() {
        shopsComplete++;
    }

    public int getID() {
        return this.id;
    }

    synchronized public void addAwaiting(int i) {
        awaitingReply.add(i);
    }

    synchronized public void removeAwaiting(int i) {
        awaitingReply.remove((Integer) i);
    }

    synchronized public int awaitCount() {
        return awaitingReply.size();
    }

    synchronized protected void updateHighestNumber(int i) {
        highestNumber = i;
    }

    synchronized protected void updateTicketNumber() {
        // First round, highestNumber is 0 so we'll randomise the ticket number to start with
        if (highestNumber == 0) {
            ticketNumber = rng.nextInt(1, 26);
        } else {
            ticketNumber = highestNumber + 1;
        }
    }

    synchronized protected void replyToAllDeferred() {
        for (Integer householdID : deferred) {
            send(MessageType.REPLY, householdID);
            deferred.remove((Integer) householdID);
        }
    }

    synchronized protected void addToDeferred(int id) {
        deferred.add(id);
    }

    @Override
    public void run() {
        while (!wantedShopsDone) {
            try {
                Thread.sleep(1500);
                requestCS = true;
                updateTicketNumber();
                for (int id : messenger.getHouseholdIDs()) {
                    if (id != this.id) {
                        send(MessageType.REQUEST, id);
                    }
                }
                while (awaitCount() > 0) {
                    Thread.sleep(50);
                }
                shop.doShop(this);
                requestCS = false;
                replyToAllDeferred();
            } catch (InterruptedException ignored) {}
            if (shopsComplete >= 5) {
                this.wantedShopsComplete();
            }
        }
        this.listener.interrupt();
        System.out.println("Household " + this.id + " has completed their shopping for the week.");
    }
}


class HouseholdListener extends Thread {

    private Household household;

    public HouseholdListener(Household household) {
        this.household = household;
    }

    private void receive() throws InterruptedException {
        Message message = household.receiveQueue.take();
        if (message.getMessageType() == MessageType.REQUEST) {
            if (message.getTicketNumber() > household.highestNumber) {
                household.updateHighestNumber(message.getTicketNumber());
            }
            if (!household.requestCS || (message.getTicketNumber() < household.ticketNumber ||
                    (message.getTicketNumber() == household.ticketNumber && message.getSenderID() < household.id))) {
                household.send(MessageType.REPLY, message.getSenderID());
            } else {
                household.addToDeferred(message.getSenderID());

            }
        } else if (message.getMessageType() == MessageType.REPLY) {
            household.removeAwaiting(message.getSenderID());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                receive();
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }
    }
}
