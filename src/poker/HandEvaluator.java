package poker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

//Hand strength will be stored in an Integerarray of size 5.
// index 0 = strength rank
// index 1 = value of quad,highest straight, 
// index 2 = depends. if has2pair, lower of the 2 pair
// index 3 = high card
// index 4 = high card


public class HandEvaluator {

	public static final int STRAIGHTFLUSH = 22;
	public static final int QUAD = 21;
	public static final int FULLHOUSE = 20;
	public static final int FLUSH = 19;
	public static final int STRAIGHT = 18;
	public static final int SET = 17;
	public static final int TWOPAIR = 16;
	public static final int PAIR = 15;
	//This method will eventually evaluate the hand strengths of each player
	//in the array players. These players are the activePlayers. 
	//Inside this method, a heirarchy of method calls to:
	//Royal Flush --> Straight Flush --> 4 of a kind ...---> Pair
	//will be made
	public static Player evaluateHands(Player[] players, Card[] board) {

		int[] playerStrengths = new int[players.length];
		
		ArrayList <Card> allCards = new ArrayList <Card> ();
		for (int i = 0; i < players.length; i++) {
			
			allCards.addAll(Arrays.asList(players[i].holeCards[0], players[i].holeCards[1],
					board[0], board[1], board[2], board[3], board[4]));

			//playerStrengths[i] = determineStrength(allCards);
			}
		
		
		
		return players[determineWinner(playerStrengths)];

	}

	private static int determineWinner(int[] playerStrengths) {
		return 0;
	}

//	public static int hasStraightFlush(Card[] cards) {
//
//		return false;
//
//	}

	//the passed in cards must be sorted
	public static int[] hasFourOfAKind(ArrayList<Card> cards) {

		int quadCounter = 0;
		int quadValue = 0;
		int kickerValue = 0;
		//if you have 4 of a kind, it must be in indicies 0-3 through 3-6
		outerloop : for (int i = 0; i <= 3; i++) {
			quadCounter = 0; //reset quadCounter;
			for (int a = i+1; a < cards.size(); a++) {
				if (cards.get(i).getRank().equals(cards.get(a).getRank())) {
					quadCounter++;
					if (quadCounter == 3) { //if you have quads
						//retVal[1]
						quadValue = cards.get(i).getRank().getNumeral();
						//delete all cards that are included in the quad (4 cards)
						//sublist(inclusive, exclusive)
						cards.subList(i, i+4).clear();
						//last value in this array is high card
						kickerValue = cards.get(2).getRank().getNumeral();
						break outerloop;
					}
				} else {
					break;
				}
			}
		}
		int [] retVal = new int [5];
		if (quadCounter == 3) { //if it is quads {
			retVal[0] = QUAD;
			retVal[1] = quadValue;
			retVal[2] = kickerValue;
			retVal[3] = 0;
			retVal[4] = 0;
			return retVal;
		} else {
			return retVal;
		}

}

	public static int[] hasFullHouse(ArrayList <Card> cards) {
		int tripsCounter = 0;
		int pairCounter = 0;
		//Fullhouse is comprised of trips + pair
		int tripsValue = 0;
		int pairValue = 0;
		//Find highest 3 of a kind
		//Uses same method as quads, but starts from the end (finds highest trips possible)
		outerloop : for(int i = cards.size()-1; i >= 3; i--){
			tripsCounter = 0;
			for (int a = i-1; a >= 0; a--){
				if(cards.get(i).getRank().equals(cards.get(a).getRank())){
					tripsCounter++;
					if (tripsCounter == 2){
						tripsValue = cards.get(i).getRank().getNumeral();
						cards.subList(i - 2, i+1).clear();
						break outerloop;
					}
				} else{
					break;
				}
			}
		}
		//Find highest remaining pair
		outerloop : for(int i = cards.size()-1; i >= 3; i--) {
			pairCounter = 0;
			for (int a = i - 1; a >= 0; a--) {
				if (cards.get(i).getRank().equals(cards.get(a).getRank())) {
					pairCounter++;
					//Must be greater or equal to one (if two trips are present lower one is counted as a pair)
					if (pairCounter >= 1) {
						pairValue = cards.get(i).getRank().getNumeral();
						break outerloop;
					}
				} else {
					break;
				}
			}
		}
		int [] retVal = new int [5];
		if(tripsValue != 0 && pairValue != 0){
			retVal[0] = FULLHOUSE;
			retVal[1] = tripsValue;
			retVal[2] = pairValue;
			return retVal;
		}
		else{
			return retVal;
		}
	}

	public static int[] hasFlush(ArrayList <Card> cards) {
		int flushCounter = 0;
		int flushValue = 0;
		//Same method as fullhouse, starts from end (highest flush)
		//Requires sort by suit
		outerloop : for (int i = cards.size() - 1; i >= 3; i--) {
			flushCounter = 0;
			for (int a = i - 1; a >= 0; a--) {
				if (cards.get(i).getSuit().equals(cards.get(a).getSuit())) {
					flushCounter++;
					if (flushCounter == 4) {
						flushValue = cards.get(i).getRank().getNumeral();
						break outerloop;
					}
				} else {
					break;
				}
			}
		}
		int[] retVal = new int[5];
		if (flushCounter == 4) {
			retVal[0] = FLUSH;
			retVal[1] = flushValue;
			return retVal;
		} else {
			return retVal;
		}
	}
//
//	public static int hasStraight(Card[] cards) {
//
//		return false;
//
//	}
//
//	public static int hasThreeOfAKind(Card[] cards) {
//
//		return false;
//
//	}
//
//	public static int hasTwoPair(Card[] cards) {
//
//		return false;
//
//	}
//
//	public static int hasOnePair(Card[] cards) {
//
//		return false;
//
//	}
//
//	public static int[] determineStrength(ArrayList<Card> cards) {
//
//
//		if (hasFourOfAKind(cards)[0] == QUAD) {
//			return hasFourOfAKind(cards);
//		} else { //if high card
//			return hasHighCard(cards);
//		}
//
//
//
//	}



	
	
	
	
}
