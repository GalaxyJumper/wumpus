// Joshua Lennon (cringe)
// Nick Lennon (less cringe)
// Aviral Mishra (the cool one)
// Toki
// 2/12/2024
// Game Control Object

//taking in inputs for all classes and running the game
//connecting the ui to the game itself
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.awt.Color;
import java.util.Arrays;

public  class GameControl {
    ///////////////////////////////////////////////
    // VARIABLES
    ///////////////////////////////////////////////

    private GameLocations gameLocs;
    private Gui gui;
    private Player player;
    private Trivia trivia;
    private HighScore scores;
    private Cave cave;

    private final String[] secrets = {
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
    };
    
    // purchasing arrows - 0
    // purchasing secrets - 1
    // saving from pit - 2
    // escaping wumpus - 3
    // escaping bats - 4

    private int questionType = -1;
    private int numRight = 0;

    ///////////////////////////////////////////////
    // CONSTRUCTORS
    ///////////////////////////////////////////////
    public GameControl() throws FontFormatException, IOException{
        cave = new Cave();
        player = new Player();
        gameLocs = new GameLocations(cave);
        trivia = new Trivia();
        scores = new HighScore(player);
        
        if (!GraphicsEnvironment.isHeadless()){
            gui = new Gui("HUNT THE WUMPUS", 2560, 1440, cave, this, gameLocs.getPlayerLoc()); 
        }

        gui.updateActionText(gameLocs.getBatLoc(0) + "", new Color(255,255,255));
        gui.updateActionText(gameLocs.getBatLoc(1) + "", new Color(255,255,255));
        gui.updateActionText(gameLocs.getWumpusLoc() + "", new Color(255,255,255));
        gui.updateActionText(gameLocs.getPitLoc(0) + "", new Color(255,255,255));
        gui.updateActionText(gameLocs.getPitLoc(1) + "", new Color(255,255,255));
    }

    ///////////////////////////////////////////////
    // METHODS
    ///////////////////////////////////////////////

    // 0 - 29 (inclusive) is a room number being moved to
    public void turn(int playerInput){
        gui.updateActionText(Arrays.toString(cave.possibleMoves(playerInput)), new Color(255,255,255));
        System.out.println("I'm here8");
        if (gameLocs.setPlayerLoc(playerInput)){
            System.out.println("I'm here9");
            gui.move(playerInput);
            System.out.println("I'm here10");
            player.addTurnsTaken();
            //gui.updateTurnCounter();
            String[] hazards = gameLocs.getHazards();
            gui.updateActionText(Arrays.toString(hazards), new Color(255,255,255));
            
            if (hazards.length > 0){
                if (hazards[0].equals("Wumpus")){
                    gui.updateActionText("You Encountered The Wumpus", new Color(255, 255, 255));
                    questionType = 3;
                    triviaTime();
                    if (hazards.length != 1){
                        hazards[0] = hazards[1];
                    }
                } else if (hazards[0].equals("Pit")){
                    gui.updateActionText("You Teeter On The Precipice Of A Bottomless Cliff!", new Color(255, 255, 255));
                    questionType = 2;
                    triviaTime();
                } else if (hazards[0].equals("Bat")){
                    gui.updateActionText("You Found Bats!", new Color(255, 255, 255));
                    questionType = 4;
                    triviaTime();
                }
            }
            //gui.updateActionText("This runs", new Color(255,255,255));
            //gameLocs.moveWumpus(player.getTurnsTaken());
            gui.updateActionText("Turn completed", new Color(255,255,255));
        }
    }

    // 0 - 29 (inclusive) + true location receiving arrow
    // 0 + false - purchase arrow
    // 1 + false - purchase secret
    public void turn(int playerInput, boolean isShooting){
        if (isShooting){
            if (cave.canMove(gameLocs.getPlayerLoc(),playerInput)){
                player.addTurnsTaken();
                player.addArrows(-1);
                // gui.updateTurnCounter(player.getTurnsTaken());
                String[] hazards = gameLocs.getHazards(playerInput);
                boolean wumpusShot = false;
                boolean missed = false;
                if (hazards[0].equals("Wumpus")){
                    if (Math.random() < 0.5)
                        wumpusShot = true;
                    else {
                        missed = true;
                    }
                }
                if (wumpusShot){
                    gui.updateActionText("You Won!", new Color(0,255,0));
                    gameEnd();
                } else if (missed) {
                    gui.updateActionText("You Missed!", new Color(255,255,0));
                } else {
                    gui.updateActionText("Seems The Wumpus Wasn't There...!", new Color(255,255,0));
                }
            }
        } else {
            questionType = playerInput;
            triviaTime();
        }
        gameLocs.moveWumpus(player.getTurnsTaken());
    }

    public void triviaTime(){
        // first is a list of trivia questions with all their info, there are always 5 questions at a time
        // each one corresponds to a second list with indexes organized as such:
        // index 0 - question (Q)
        // index 1 - 4 - answers (A)
        // index 5 - correct answer as a single letter (K)
        int numCorrect = 0;
        String[][] questions = new String[5][6];
        String [] answers = new String[5];
        boolean lastQCorrect;

        for (int i = 0; i < 5; i++){
            questions[i] = trivia.getQandAandK();
        }

        System.out.println("This runs... uh-oh");

        gui.openTriviaMenu(questions[0], 5);
        System.out.println("This runs... uh-oh2");
        long start = System.currentTimeMillis();
        long now = System.currentTimeMillis();
        while(now-start < 1000){
            //Do nothing
            now = System.currentTimeMillis();
            System.out.println("null");
        }
        answers[0] = gui.nextTriviaChoice();
        System.out.println("This runs... uh-oh3");
        lastQCorrect = questions[0][5].equals(answers[0].substring(0, 1));
        if (lastQCorrect){
            numCorrect++;
        }
        
        for (int i = 1; i < 5; i++){
            gui.nextTriviaQuestion(lastQCorrect, questions[i], false, i - 1);
            answers[i] = gui.nextTriviaChoice();
            lastQCorrect = questions[i][5].equals(answers[i]);
            if (lastQCorrect){
                numCorrect++;
            }
        }

        gui.nextTriviaQuestion(lastQCorrect, new String[]{"", "", "", "", "", ""}, true, 4);

        triviaAction(numCorrect >= 3);
    }

    public void updateNumRight(){
        numRight++;
    }

    public void allQuestionsAsked(){
        triviaAction(numRight > 3);
        numRight = 0;
    }

    // response is "A", "B", "C", or "D"
    public void triviaAction(boolean triviaSuccess){
        if (questionType == 0){
            if (triviaSuccess){
                player.purchaseArrow();
                gui.updateActionText("Arrow Gained", new Color(255,0,255));
            }
            player.addTurnsTaken();
            // gui.updateTurnCounter(player.getTurnsTaken());
        } else if (questionType == 1){
            if (triviaSuccess){ 
                // gui.displaySecret(writeSecret((int) (Math.random() * 10 + 1)));
            }
            player.addTurnsTaken();
            // gui.updateTurnCounter(player.getTurnsTaken());
        } else if (questionType == 2){
            if (!triviaSuccess){
                gui.updateActionText("You died!", new Color(255, 0 , 0));
                gameEnd();
            } else {
                gui.updateActionText("You lived!", new Color(0, 255, 0));
            }
        } else if (questionType == 3){
            if (!triviaSuccess){
                gui.updateActionText("You died!", new Color(255, 0 , 0));
                gameEnd();
            } else {
                gui.updateActionText("The Wumpus is Wounded!", new Color(0, 255, 0));
            }
        } else if (questionType == 4){
            if (!triviaSuccess){
                int newRoom = gameLocs.batTransport();
                gui.move(newRoom);
                gui.updateActionText("You Were Transported Into Room #" + newRoom + "!", new Color(255,255,0));
            } else {
                gui.updateActionText("You Escaped The Bats!", new Color(0, 255, 0));
            }
        }
        triviaSuccess = false;
    }

    public String writeSecret(int secretIndex){
        String secret = secrets[secretIndex];
        return secret;
    }

    public void gameEnd(){
        gui.updateActionText("Game Over", new Color(255,255,255));
        try {
            String[][] leaderboardInfo = scores.endOfGame();
        } catch (IOException io) {
            io.printStackTrace();
        }
        // gui.displayLeaderboard(leaderboardInfo);
        System.exit(0);
    }
}