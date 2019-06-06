package com.jagtarsingh.game01;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class GameEngine extends SurfaceView implements Runnable {
    private final String TAG = "SPARROW";

    // game thread variables
    private Thread gameThread = null;
    private volatile boolean gameIsRunning;

    // drawing variables
    private Canvas canvas;
    private Paint paintbrush;
    private SurfaceHolder holder;

    // Screen resolution varaibles
    private int screenWidth;
    private int screenHeight;

    // VISIBLE GAME PLAY AREA
    // These variables are set in the constructor
    int VISIBLE_LEFT;
    int VISIBLE_TOP;
    int VISIBLE_RIGHT;
    int VISIBLE_BOTTOM;

    int MOUSETAP_X = 100;
    int MOUSETAP_Y = 700;

    // SPRITES
    Square bullet;
    Square cage;
    int SQUARE_WIDTH = 100;

    Square enemy;

    Sprite player;
    Sprite sparrow;
    Sprite cat;

    ArrayList<Square> bullets = new ArrayList<Square>();

    // GAME STATS
    int score = 0;



    public GameEngine(Context context, int screenW, int screenH) {
        super(context);

        // intialize the drawing variables
        this.holder = this.getHolder();
        this.paintbrush = new Paint();

        // set screen height and width
        this.screenWidth = screenW;
        this.screenHeight = screenH;

        // setup visible game play area variables
        this.VISIBLE_LEFT = 20;
        this.VISIBLE_TOP = 10;
        this.VISIBLE_RIGHT = this.screenWidth - 20;
        this.VISIBLE_BOTTOM = (int) (this.screenHeight * 0.8);


        // initalize sprites
        cage = new Square(context,this.VISIBLE_LEFT,this.VISIBLE_TOP,250);
        this.player = new Sprite(this.getContext(), 100, 700, R.drawable.player64);
        this.sparrow = new Sprite(this.getContext(), 500, 200, R.drawable.bird64);
        this.cat = new Sprite(this.getContext(),this.VISIBLE_LEFT,this.VISIBLE_BOTTOM - 180,R.drawable.robot64);
        this.bullet = new Square(context,100,700,50);
        bullets.add(this.bullet);
    }

    @Override
    public void run() {
        while (gameIsRunning == true) {
            updateGame();    // updating positions of stuff
            redrawSprites(); // drawing the stuff
            controlFPS();
        }
    }


    public void makeBullets(){
        this.bullet = new Square(this.getContext(),100,700,50);
        bullets.add(this.bullet);
    }
    // Game Loop methods

    long timeNow = 0;
    long timePast = 0;
    boolean cageMovingright = true;
    boolean catMovingright = true;
    public void updateGame() {

        // --------------------------------------------------------
        // MOVING CAGE
        // --------------------------------------------------------
        if(cageMovingright == true)
        {
            this.cage.setxPosition(this.cage.getxPosition() + 20);
        }else if (cageMovingright == false)
        {
            this.cage.setxPosition(this.cage.getxPosition() - 20);
        }

        if(cage.getxPosition() < this.VISIBLE_LEFT)
        {
            cageMovingright = true;
        }
        else if (cage.getxPosition() + this.cage.getWidth() >= this.VISIBLE_RIGHT)
        {
            cageMovingright = false;
        }
        //----------------------------------------------------------

        // --------------------------------------------------------
        // MOVING CAT
        // --------------------------------------------------------
        if(catMovingright == true)
        {
            this.cat.setxPosition(this.cat.getxPosition() + 40);
        }else if (catMovingright == false)
        {
            this.cat.setxPosition(this.cat.getxPosition() - 40);
        }

        if(cat.getxPosition() <= this.screenWidth/3)
        {
            catMovingright = true;
        }
        else if (cat.getxPosition() + this.cat.getImage().getWidth() > this.VISIBLE_RIGHT)
        {
            catMovingright = false;
        }

        // --------------------------------------------------------
        // BULLET MATH
        // --------------------------------------------------------


        // 1. calculate distance between bullet and enemy
        for(int i = 0; i< bullets.size(); i++) {
            double a = this.MOUSETAP_X - this.bullets.get(i).getxPosition();
            double b = this.MOUSETAP_Y - this.bullets.get(i).getyPosition();

            // d = sqrt(a^2 + b^2)

            double d = Math.sqrt((a * a) + (b * b));

            Log.d(TAG, "Distance to enemy: " + d);

            // 2. calculate xn and yn constants

            // (amount of x to move, amount of y to move)
            double xn = (a / d);
            double yn = (b / d);

            // 3. calculate new (x,y) coordinates
            int newX = this.bullets.get(i).getxPosition() + (int) (xn * 80);
            int newY = this.bullets.get(i).getyPosition() + (int) (yn * 80);

            this.bullets.get(i).setxPosition(newX);
            this.bullets.get(i).setyPosition(newY);


            if(this.bullet.getHitbox().intersect(this.cage.getHitbox()) )
            {
                Log.d("hit", "its a hit");

                bullets.remove(i);
                this.makeBullets();


            }
        }
      //--------------------------------------------------

        //UPDATING SPARROW MOVEMENTS
        // generate a random (x,y) for the cat
        Random rand = new Random();
        int nX = rand.nextInt(this.screenWidth/2);
        int nY = rand.nextInt(this.VISIBLE_BOTTOM - 300);


        // get current time
        timeNow = System.currentTimeMillis();
            if ((timeNow - timePast) > 1000) {
                this.sparrow.setxPosition(nX);
                this.sparrow.setyPosition(nY);
                timePast = timeNow;
            }




    }


    public void outputVisibleArea() {
        Log.d(TAG, "DEBUG: The visible area of the screen is:");
        Log.d(TAG, "DEBUG: Maximum w,h = " + this.screenWidth +  "," + this.screenHeight);
        Log.d(TAG, "DEBUG: Visible w,h =" + VISIBLE_RIGHT + "," + VISIBLE_BOTTOM);
        Log.d(TAG, "-------------------------------------");
    }



    public void redrawSprites() {
        if (holder.getSurface().isValid()) {

            // initialize the canvas
            canvas = holder.lockCanvas();
            // --------------------------------

            // set the game's background color
            canvas.drawColor(Color.argb(255,255,255,255));

            // setup stroke style and width
            paintbrush.setStyle(Paint.Style.FILL);
            paintbrush.setStrokeWidth(8);

            // --------------------------------------------------------
            // draw boundaries of the visible space of app
            // --------------------------------------------------------
            paintbrush.setStyle(Paint.Style.STROKE);
            paintbrush.setColor(Color.argb(255, 0, 128, 0));

            canvas.drawRect(VISIBLE_LEFT, VISIBLE_TOP, VISIBLE_RIGHT, VISIBLE_BOTTOM, paintbrush);
            this.outputVisibleArea();

            // --------------------------------------------------------
            // draw player and sparrow
            // --------------------------------------------------------

            // 1. player
            canvas.drawBitmap(this.player.getImage(), this.player.getxPosition(), this.player.getyPosition(), paintbrush);

            // 2. sparrow
            canvas.drawBitmap(this.sparrow.getImage(), this.sparrow.getxPosition(), this.sparrow.getyPosition(), paintbrush);

            // --------------------------------------------------------
            // draw hitbox on player
            // --------------------------------------------------------
            Rect r = player.getHitbox();
            paintbrush.setStyle(Paint.Style.STROKE);
            canvas.drawRect(r, paintbrush);


            // --------------------------------------------------------
            // draw hitbox on player
            // --------------------------------------------------------
            paintbrush.setTextSize(60);
            paintbrush.setStrokeWidth(5);
            String screenInfo = "Width: " + this.screenWidth + " Height: " + this.screenHeight + "";
            canvas.drawText(screenInfo, 10, 100, paintbrush);

            // --------------------------------------------------------
            // draw the cage
            // --------------------------------------------------------

            paintbrush.setStyle(Paint.Style.STROKE);
            paintbrush.setColor(Color.RED);

            canvas.drawRect(this.cage.getxPosition(),
                    this.cage.getyPosition(),
                    this.cage.getxPosition() + this.cage.getWidth(),
                    this.cage.getyPosition()+this.cage.getWidth(),paintbrush);

            //Draw cage hitboc

            this.cage.updateHitbox();

            // --------------------------------

            // --------------------------------------------------------
            // draw the cat
            // --------------------------------------------------------
            canvas.drawBitmap(this.cat.getImage(), this.cat.getxPosition(), this.cat.getyPosition(), paintbrush);

            //draw cat hitboc
            this.cat.getHitbox();

            // --------------------------------------------------------
            // draw the bullet
            // --------------------------------------------------------
            canvas.drawRect(this.bullet.getxPosition(),
                    this.bullet.getyPosition(),
                    this.bullet.getxPosition() + this.bullet.getWidth(),
                    this.bullet.getyPosition()+this.bullet.getWidth(),paintbrush);

            //bullet hitbox
            this.bullet.updateHitbox();

            // --------------------------------------------------------
            // BULLET AND CAGE COLLISION
            // --------------------------------------------------------





            holder.unlockCanvasAndPost(canvas);
        }

    }

    public void controlFPS() {
        try {
            gameThread.sleep(17);
        }
        catch (InterruptedException e) {

        }
    }


    // Deal with user input
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int userAction = event.getActionMasked();
        //@TODO: What should happen when person touches the screen?
        if (userAction == MotionEvent.ACTION_DOWN) {

            MOUSETAP_X = (int)event.getX();
            MOUSETAP_Y = (int)event.getY();


        }
        else if (userAction == MotionEvent.ACTION_UP) {
            Log.d(TAG, "Person lifted finger");

        }

        return true;
    }

    // Game status - pause & resume
    public void pauseGame() {
        gameIsRunning = false;
        try {
            gameThread.join();
        }
        catch (InterruptedException e) {

        }
    }
    public void  resumeGame() {
        gameIsRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

}

