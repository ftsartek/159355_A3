package Permission;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Household extends Thread {

    protected int ticketNumber;
    protected int highestNumber;
    protected int id;
    protected ArrayList<Integer> deferred;
    protected ArrayList<Integer> awaitingReply;
    protected boolean requestCS = false;
    Messenger messenger;
    protected boolean stillPandemic = true;
    protected LinkedBlockingQueue<Message> receiveQueue = new LinkedBlockingQueue<>();
    private HouseholdListener listener;


    public Household (int id, Messenger messenger) {
        this.id = id;
        deferred = new ArrayList<>();
        awaitingReply = new ArrayList<>();
        ticketNumber = 0;
        highestNumber = 0;
        this.messenger = messenger;
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


    public void pandemicOver() {
        this.stillPandemic = false;
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
        ticketNumber = highestNumber + 1;
    }

    @Override
    public void run() {
        System.out.println("Main thread for " + id + " is running");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}
        while (stillPandemic) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {}
            requestCS = true;
            updateTicketNumber();
            for (int id : messenger.getHouseholdIDs()) {
                if (id != this.id) {
                    send(MessageType.REQUEST, id);
                }
            }
            while (awaitCount() > 0) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {}
            }
            System.out.println("Household " + id + " in and out of the critical section...");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            requestCS = false;
            for (Integer householdID : deferred) {
                send(MessageType.REPLY, householdID);
                deferred.remove((Integer) householdID);
            }
        }
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
                System.out.println("Thread " + household.id + " has just found a new highest number");
            }
            if (!household.requestCS || (message.getTicketNumber() < household.ticketNumber ||
                    (message.getTicketNumber() == household.ticketNumber && message.getSenderID() < household.id))) {
                System.out.println("Thread " + household.id + " is replying to " + message.getSenderID());
                household.send(MessageType.REPLY, message.getSenderID());
            } else {
                System.out.println("Thread " + household.id + " is deferring reply to " + message.getSenderID());
                household.deferred.add(message.getSenderID());

            }
        } else if (message.getMessageType() == MessageType.REPLY) {
            System.out.println("Thread " + household.id + " is no longer waiting for a reply from " + message.getSenderID());
            household.removeAwaiting(message.getDestinationID());
        }
    }

    @Override
    public void run() {
        System.out.println("Receiver for " + household.id + " is now listening...");
        while (household.stillPandemic) {
            try {
                receive();
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }
    }
}
