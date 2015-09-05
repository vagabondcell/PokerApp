package poker;

import java.util.Scanner;

public class GameDriver {

	public static void main(String[] args) {
		
		PokerGame pokerGame;

		Scanner in = new Scanner(System.in);
		
		do {
		
		System.out.println("\t\t\tTexas Holdem Game\n\t\t\t   created by\n"
				+ "\t\t Brian Lee & Dennis Deng 2015(c)\n\n");

		pokerGame = new PokerGame(2);

		System.out.print("Would you like to play another poker game?: ");
		String askUserIfWantToPlay;
		askUserIfWantToPlay = in.nextLine().toLowerCase();
		if (askUserIfWantToPlay.startsWith("n")) {  //no more play
			break;
		} // else if ("y") { play again(go back to top of loop }
		
		} while (true); 
		
		
		
		in.close();
		
	}

}
