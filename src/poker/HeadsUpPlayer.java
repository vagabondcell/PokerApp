//Players are mutable

package poker;

//import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.*;

public class HeadsUpPlayer extends Thread implements Serializable{

    private String name;
    private int money;
    private Card [] holeCards;
    private boolean folded;
    private int streetMoney;
    public boolean endAction;
    public boolean isAllIn;
    //ID is essentially their seat number
    public final int id;
    private final int otherPlayerID;
    private Socket socket;
    private Scanner in;
    private ObjectOutputStream out;
    private ArrayList<String> messages;
    private String response;
    private boolean turnToAct;
    //should we make it mutable? and allow PlayGame to modify it?
    //add more variables

    public HeadsUpPlayer(String name, int money, int id, Socket socket, int otherPlayerID) {

        this.name = name;
        this.money = money;
        this.id = id;
        holeCards = new Card[2];
        folded = false;
        isAllIn = false;
        this.socket = socket;
        messages = new ArrayList<String>();
        turnToAct = false;
        this.otherPlayerID = otherPlayerID;
        try {
            in = new Scanner(new InputStreamReader(socket.getInputStream()));
            out = new ObjectOutputStream(socket.getOutputStream());
        }catch(IOException e){
        }
        //modify this later

    }

    public void run(){
        //Runs when player thread is started
        addMessage("Game has started");
        addMessage("Enter player name");
        send();
        clearMessages();
        this.name = receive();
        //Output message and end player thread (Driver is paused until both names are inputted)
        addMessage("Waiting for other player to input name");
        send();
        clearMessages();
    }

    public String getPlayerName(){
        return name;
    }

    public Card[] getHoleCards(){
        return holeCards;
    }

    public int getMoney() {

        return money;

    }

    public void receiveHand(Card[] hand) {

        holeCards[0] = hand[0];
        holeCards[1] = hand[1];
        folded = false;

    }

    public void postBB() {

        if (money == 1) {
            money -= 1;
        } else {
            money -= HeadsUpPokerGame.BIG_BLIND;
        }

    }

    public void postSB() {

        money -= HeadsUpPokerGame.SMALL_BLIND;

    }


    private void spendMoney(int amount) {

        money -= amount;

    }

    public boolean playerFolded() {

        return this.folded;

    }

    private void fold() {

        folded = true;

    }



    //This method gets called from a method in the Hand object (startStreet())
    //In that method, each player is looped through to act();
    public synchronized int act(int minimumBet, int pot, HeadsUpHand hand, HeadsUpPokerGame game, int streetIn) {

        boolean isCorrect = false;
        String action;
        int betSize = minimumBet;
        if(!this.folded && !this.isAllIn) {
            while(!isCorrect){
                //Output board
                hand.printBoard(game, streetIn, game.handNumber, this);
                // Output hand and player stats
                addMessage(this.toString(game));
                addMessage("Bet/Check/Call/Fold");
                send();
                clearMessages();
                action = receive();
                // Checks what action user inputs
                if(action.equalsIgnoreCase("Bet")) {
                    while(true){
                        //Output board
                        hand.printBoard(game, streetIn, game.handNumber, this);
                        // Output hand and player stats
                        addMessage(this.toString(game));
                        try{
                            addMessage("Size");
                            send();
                            clearMessages();
                            //Requires exception?
                            try{
                                betSize = Integer.parseInt(receive());
                            }catch(NumberFormatException e){
                                e.printStackTrace();
                            }

                            //For headsup this is fine since there isn't a scenario where your betsize is
                            //larger than how much the other player has and also less that what you have
                            if (betSize > game.players.get(otherPlayerID).getMoney()){
                                //Only allow player to bet how much other play has
                                betSize = game.players.get(otherPlayerID).getMoney();
                                //Any additional bet is total (don't have to remember previous bet)
                                this.spendMoney(betSize - streetMoney);
                                hand.addToPot(betSize-streetMoney);
                                //Total streetmoney becomes betsize
                                streetMoney = betSize;
                                isCorrect = true;
                                game.players.get(otherPlayerID).addMessage(name + " puts you all in");
                                //Increase all in counter so that
                                //when other player calls further actions are skipped
                                hand.increaseAllInCounter();
                            }
                            else if (money <= betSize) {
                                //If betsize is greater than money, player is all in
                                betSize = money;
                                this.spendMoney(betSize);
                                hand.addToPot(betSize);
                                streetMoney = betSize;
                                isAllIn = true;
                                hand.increaseAllInCounter();
                                isCorrect = true;
                                if(streetIn!=12){
                                    game.players.get(otherPlayerID).addMessage(name + " is all in");
                                }
                            } else if(betSize < 2*minimumBet || betSize == 0) {
                                addMessage("Illegal bet size");
                                //Reset betsize to what was previously bet (miniumum bet)
                                betSize = minimumBet;
                            } else {
                                //Any additional bet is total (don't have to remember previous bet)
                                this.spendMoney(betSize - streetMoney);
                                hand.addToPot(betSize-streetMoney);
                                //Total streetmoney becomes betsize
                                streetMoney = betSize;
                                isCorrect = true;
                                game.players.get(otherPlayerID).addMessage(name + " bet " + betSize);
                            }
                            break;
                        }
                        catch(InputMismatchException e) {
                            addMessage("Not a number");
                            in.next();
                            continue;
                        }
                    }
                }
                //we need a way for BB to check b/c minbet is still > 0 for him
                else if(action.equalsIgnoreCase("Check")) {
                    if(minimumBet - streetMoney > 0){
                        addMessage("You cannot check when the pot is raised");
                    } else{
                        isCorrect = true;
                        betSize = 0;
                        game.players.get(otherPlayerID).addMessage(name + " checked");
                    }
                }
                else if(action.equalsIgnoreCase("Call")) {
                    if(minimumBet == 0 || minimumBet - streetMoney == 0){
                        addMessage("You cannot call when there is no bet");
                    }
                    else if(money <= minimumBet){
                        betSize = money;
                        this.spendMoney(betSize);
                        hand.addToPot(betSize);
                        isAllIn = true;
                        //If you call all in then hand must end
                        //Add 1 to AllInCounter in order to skip further actions == players.size() in Hand class
                        //Betting all in is different since other player still has to act
                        hand.increaseAllInCounter();
                        isCorrect = true;
                        if(streetIn!=12){
                            game.players.get(otherPlayerID).addMessage(name + " is all in");
                        }
                    }
                    else{
                        this.spendMoney(minimumBet - streetMoney);
                        hand.addToPot(minimumBet - streetMoney);
                        isCorrect = true;
                        betSize = minimumBet;
                        game.players.get(otherPlayerID).addMessage(name + " called " + betSize);
                    }
                }
                else if(action.equalsIgnoreCase("Fold")) {
                    this.fold();
                    isCorrect = true;
                    betSize = 0;
                }
                else {
                    addMessage("Incorrect action, please try again");
                }
            }
        }
        else{
            betSize = 0;
        }
        return betSize;
    }

    public synchronized void spectate(HeadsUpHand hand, HeadsUpPokerGame game, 
    									int streetIn, String message) {
        clearMessages();
        addMessage(message);
        //Output board
        hand.printBoard(game, streetIn, game.handNumber, this);
        // Output hand and player stats
        addMessage(this.toString(game));
        send();
        clearMessages();

    }

        public void winPot(int amount) {

        money += amount;

    }

    public void setStreetMoney(int amount){
        streetMoney += amount;
    }

    public void resetStreetMoney(){
        streetMoney = 0;
    }

    public void setEndAction(boolean bool){
        endAction = bool;
    }

    public void endGameMessage(){
        addMessage("Game is over");
        send();
        clearMessages();
    }

    public void addMessage(String message){
        //Add message to queue
        messages.add(message);
    }

    private void clearMessages(){
        //Clear messages (usually called after send)
        messages.clear();
    }

    private void send(){
        try {
            //Reset objectOutputStream
            out.reset();
            //Write arraylist of messages to outputStream
            out.writeObject(messages);
            //Flushes outputStream
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private String receive(){
        response = null;
        try {
            while(response == null){
                if(in.hasNextLine()){
                    //Continue trying to recieve message until response is stored
                    response = in.nextLine();
                }
            }
        }catch(NoSuchElementException e){
            e.printStackTrace();
        }
        return response;
    }

    public void setTurnToAct(boolean bool){
        turnToAct = bool;
    }

    public String toString(HeadsUpPokerGame game) {
        String retVal = "";
        String position;
        if(id==game.bbIndex){
            position = "BB";
        }
        else{
            position = "SB/D";
        }
        retVal += name + ": " + "$" + money + "--" +
                holeCards[0] + holeCards[1] + "--" + position;
        return retVal;

    }

}
