import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
public class Gui extends JPanel{
    //////////////////////////////////////
    //VARAIBLES
    //////////////////////////////////////
    Graphics2D g2d;
    int width, height;
    int mapStartX = 150;
    int mapStartY = 50;
    int mapRoomSize; // = width / 40;
    GameLocations gameLocs;
    int playerLoc;

    // Drawing variables

    // {How long into the animation, location}
    // -1 if not drawing
    int[] failMoveHex = {-1, -1};
    // "Question?", "Answer 1", "Answer 2", "answer 3", "Answer 4"
    String[] triviaQuestion = new String[5];
    // {total # of Qs, # of Qs right}
    int[] triviaScoreData = new int[2];
    /////////////////////////////////////
    // CONSTRUCTOR(S)
    ////////////////////////////////////
    public Gui(String name, int width, int height, GameLocations locations){
        this.width = width;
        this.height = height;
        this.gameLocs = locations;
        this.mapRoomSize = width / 40;
        // Create a new Frame for everything to live in
        JFrame frame = new JFrame();
        // Debug message
        System.out.println("New display instantiated with dimensions " + width + "x" + height);
        // Set display size
        this.setPreferredSize(new Dimension(width, height));
        // Set default close operation (end program once the window is closed)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // for later 
        // frame.addKeyListener();

        //Put this panel into its frame so it can be displayed
        frame.add(this);
        //Make the screen fit around the panel so that there is no overlap
        frame.pack();
        //Make the window visible and set its name to the given name
        frame.setTitle(name);
        frame.setVisible(true);
        this.move(23);
        this.failMove(2);
    }
    
    public Gui(String name){
        JFrame frame = new JFrame();
        System.out.println("New display instantiated with default dimensions 960x540");
        this.setPreferredSize(new Dimension(960, 540));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // for later frame.addKeyListener();
        frame.add(this);
        frame.pack();
        frame.setTitle(name);
        frame.setVisible(true);
    }
    /////////////////////////////////////
    // METHODS
    /////////////////////////////////////
    
    //Should run in a loop - gets called once every 17 ms
    //Draws things based on 
    public void paint(Graphics g){
        g2d = (Graphics2D)g;
        //Antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
RenderingHints.VALUE_ANTIALIAS_ON); 
        //Background + map
        g2d.fillRect(0, 0, width, height);
        drawMap(mapStartX, mapStartY, mapRoomSize, g2d, playerLoc);

        if(failMoveHex[0] != -1 && failMoveHex[1] != -1){
            drawFailMoveHex(failMoveHex[0], failMoveHex[1]);
        }
        
    }
    /*
     * drawRoom
     * 
     * centerX, Y: Center of the hexagon.
     * Radius: distance from center to any vertex.
     * number: Which number to label the room.
     * Color: What color to draw the room.
     * doors: Which sides to label as doors.
     *          1_
     *      2 /    \ 0
     *      3 \ __ / 5
     *           4
     */
    private void drawRoom(double centerX, double centerY, double radius, String number, Color color){
        double currentX = 0;
        double currentY = 0;
        int[] xPoints = new int[6];
        int[] yPoints = new int[6];
        double doorScale = 0.3;
        for(int i = 0; i < 6; i++){
            currentX = centerX + (Math.cos((Math.PI/3) * i) * radius);
            currentY = centerY + (Math.sin((Math.PI/3) * i) * radius);
            xPoints[i] = (int)(currentX);
            yPoints[i] = (int)(currentY);
        }
        g2d.setColor(color);
        g2d.fillPolygon(xPoints, yPoints, 6);
        g2d.setColor(new Color(255, 255, 255));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(xPoints, yPoints, 6);
        g2d.drawString(number, (int)centerX, (int)centerY);
    }
    private void drawHex(double centerX, double centerY, double radius, Color color){
        double currentX = 0;
        double currentY = 0;
        int[] xPoints = new int[6];
        int[] yPoints = new int[6];
        double doorScale = 0.3;
        for(int i = 0; i < 6; i++){
            currentX = centerX + (Math.cos((Math.PI/3) * i) * radius);
            currentY = centerY + (Math.sin((Math.PI/3) * i) * radius);
            xPoints[i] = (int)(currentX);
            yPoints[i] = (int)(currentY);
        }
        g2d.setColor(color);
        g2d.drawPolygon(xPoints, yPoints, 6);
    }
    private void fillHex(double centerX, double centerY, double radius, Color color){
        double currentX = 0;
        double currentY = 0;
        int[] xPoints = new int[6];
        int[] yPoints = new int[6];
        double doorScale = 0.3;
        for(int i = 0; i < 6; i++){
            currentX = centerX + (Math.cos((Math.PI/3) * i) * radius);
            currentY = centerY + (Math.sin((Math.PI/3) * i) * radius);
            xPoints[i] = (int)(currentX);
            yPoints[i] = (int)(currentY);
        }
        g2d.setColor(color);
        g2d.fillPolygon(xPoints, yPoints, 6);
    }
    private void drawMap(int startX, int startY, int radius, Graphics2D g2d, int playerLoc){
        //X = (even) startX + (3radius) * x
        //    (odd)  (startX + (3radius) * x) + 1.5 radius
        Color currentColor = new Color(0, 0, 0);
        Cave cave = gameLocs.getCave();

        int[] possibleMovesInt = cave.possibleMoves(playerLoc);
        ArrayList<Integer> possibleMoves = new ArrayList<Integer>();
        for(int i : possibleMovesInt) possibleMoves.add(i);
        //COlumn
        for(int i = 0; i < 6; i++){
            double x = (i * (radius*1.5));
            //Row
            for(int k = 0; k < 5; k++){ 
                double y = (k * (Math.sqrt(3) * radius));
                int currentRoomNum = k * 6 + i;

                if(playerLoc == currentRoomNum){
                    currentColor = new Color(0, 255, 0);
                } else if(possibleMoves.size() > 0 && possibleMoves.get(0) == currentRoomNum){
                    currentColor = new Color(60, 60, 60);
                    possibleMoves.remove(0);
                } else {
                    currentColor = new Color(20, 20, 20);
                }
                
                drawRoom(x + startX, (y + ( (i % 2) * (Math.sqrt(3)*radius)/2) ) + startY, radius + 1, String.valueOf(currentRoomNum + 1), currentColor);
            }
        }
    
    }
    private void drawFailMoveHex(int millis, int loc){
        int x = loc % 6;
        int y = loc / 6;
        int drawX = (int)(x * mapRoomSize * 1.5 + mapStartX);
        int drawY = (int)(y + mapStartY + ((y % 2) * (Math.sqrt(3) * (mapRoomSize + 1))/2));
        double currentTransparency = ((double)millis / 500) * 255;
        Color currentColor = new Color(255, 0, 0, (int)(255 - currentTransparency));
        fillHex(drawX, drawY, mapRoomSize - 2, currentColor);
        
    }
    public void move(int whereTo){
        playerLoc = whereTo;
        this.repaint();

    }
    public void failMove(int whereTo){
        long animationStart = System.currentTimeMillis();
        long now = System.currentTimeMillis();
        while(now - animationStart < 500){
            now = System.currentTimeMillis();
            failMoveHex[0] = (int)(now - animationStart);
            failMoveHex[1] = whereTo;
            this.repaint();
        }
        failMoveHex[0] = -1;
        failMoveHex[1] = -1;
    }
    public void openTriviaWindow(String[] triviaQuestion, int numQuestions){
        
    }
    public void nextTriviaQuestion(boolean correct){
        
    }
    public void closeTriviaWindow(){
        
    }
    //public void drawActionText(String[] text) LATER

    //public void drawScene(String[] room) LATER


    //public void drawPlayer(int x, y) LATER
    


    //public void drawWumpus(int x, y)


    //public void drawObstacle(String[] room)




    
}
