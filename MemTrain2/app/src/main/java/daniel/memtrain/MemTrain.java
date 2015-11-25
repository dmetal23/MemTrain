package daniel.memtrain;

import java.util.*;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;

import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by daniel on 7/13/15.
 */
public final class MemTrain extends Activity{

    private MemTrain model; //new MemTrain object
    public static final int TOTAL_BUTTONS = 3 * 3; //9 buttons superfluous, only 5 in use
    private List<Listener> listeners = new ArrayList<Listener>();
    private static TextView e;
    private static int hScore = 0, ultimateHs;//HighScore
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static boolean isAHS = false, startButtonIsOn = false;;
    private static Intent mainmenu;
    private static ImageButton startButton;
    private static ImageButton lastButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        model = new MemTrain(this); //new object

        //creating font and view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_layout);
        e = (TextView)findViewById(R.id.eric_hs);
        String fontPath = "font/arcadepix.TTF";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        e.setTypeface(tf);
        e.setText("Score: " + hScore);

        mainmenu = new Intent(this, MainActivity.class);
        sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ultimateHs = loadHighScore();

        ButtonsMain grid = (ButtonsMain) this.findViewById(R.id.button_grid);
        grid.setMemTrain(model); //new interactable button grid set up for our memtrain model

        startButton = (ImageButton)findViewById(R.id.start); //start button
        startButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startButton.setImageResource(R.drawable.playgamestwo);
                        break;
                    case MotionEvent.ACTION_UP:
                        startButton.setImageResource(R.drawable.playgamesone);
                        mUpdateHandler.postDelayed(new Runnable() {
                            public void run() {
                                startButtonIsOn = true;//ADDED
                                hScore = 0;
                                e.setText("Score: " + hScore);
                                model.gameStart();
                            }
                        }, 100);
                        break;
                }
                return false;
            }
        });

        lastButton = (ImageButton) findViewById(R.id.last_button);
        lastButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastButton.setImageResource(R.drawable.playgameltwo);
                        break;
                    case MotionEvent.ACTION_UP:
                        lastButton.setImageResource(R.drawable.playgamelone);
                        if(gameMode != LOST) {
                            model.playLast();
                        }
                        if (!startButtonIsOn) {

                        }
                        else {
                            if (hScore == 0) {
                                hScore = -10;
                            }
                            else if (hScore > 0){
                                if (hScore == 10) {
                                    hScore = 0;
                                }
                                else {
                                    hScore = hScore / 2;
                                }
                            }
                            else {
                                hScore = hScore * 2;
                            }
                            e.setText("Score: " + hScore);
                        }
                        break;
                }
                return false;
            }
        });

        /* After all initialization, we set up our save/restore InstanceState Bundle. */
        if (savedInstanceState == null) {		// Just launched.  Set initial state.
            SharedPreferences settings = getPreferences (0); // Private mode by default.
            model.setLevel(settings.getInt(MemTrain.GAME_LEVEL, 1)); //setting level to default
        }
    }

    public interface Listener { //Listener uses invalidate button so pixels are redrawn in main thread
        void buttonStateChanged(int index); //calls invalidate()
        void multipleButtonStateChanged(); //calls invalidate()
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private boolean[] buttonPressMap = new boolean[TOTAL_BUTTONS]; //boolean map of our buttons
    private SoundPool soundPool; //soundPool mapped to animations
    private int[] soundIds = new int[TOTAL_BUTTONS + 1];  // Add lose
    private int speakerStream;

    //doStream is similar to Mediaplayer's .start()
    public void doStream(int soundId) {
        if (soundId != 0) {  // Don't do anything different if our soundID is invalid.
            if (speakerStream != 0) {  // Stop what we were doing.
                soundPool.stop(speakerStream);
            }
            speakerStream = soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    public void showButtonRelease(int index) { //play sound with button animation
        if (index >= 0 && index < TOTAL_BUTTONS) { //if our index exists
            if (buttonPressMap[index] == true) { //if our index in our buttonmap is on
                buttonPressMap[index] = false; //turn it off
                if (speakerStream != 0) {
                    soundPool.stop(speakerStream); //stop speaker
                    speakerStream = 0;
                }
                for (Listener listener : listeners) {
                    listener.buttonStateChanged(index); //release button
                }
            }
        }
    }

    public boolean isButtonPressed(int index) { //check if individual button was pressed by index
        if (index < 0 || index > TOTAL_BUTTONS) { //if our index does not exist
            return false; //our button was not pressed
        } else {
            return buttonPressMap[index]; //return booelan result of the index
        }
    }

    //TIMERS
    private static final int TICK_DURATION = 100;
    private static final int BETWEEN_DURATION = 50;
    private static final int TICK_COMPENSATION = TICK_DURATION - BETWEEN_DURATION;

    //handler messages
    private static final int UI = 0;

    //game states
    private static final int IDLE = 0;
    private static final int LISTENING = 1;
    private static final int PLAYING = 2;
    private static final int LOSING = 8;
    private static final int LOST = 9;

    //sound management
    private static final int GREEN = 0;
    private static final int RED = 1;
    private static final int YELLOW = 2;
    private static final int BLUE = 3;
    private static final int VICTORY_SOUND = 4; //currently used only for new high score
    private static final int LOSE_SOUND = 5; //FAIL sound

    //keys for game-state control
    public static final String THE_GAME = "theGame";
    public static final String GAME_LEVEL = "gameLevel";
    private static final String SEQUENCE_INDEX = "sequenceIndex";
    private static final String TOTAL_LENGTH = "totalLength";
    private static final String PLAYER_POSITION = "playerPosition";
    private static final String GAME_MODE = "gameMode";
    private static final String WIN_TONE_INDEX = "winToneIndex";
    private static final String IS_LIT = "isLit";
    private static final String BEEP_DURATION = "beepDuration";
    private static final String HEARD_BUTTON_PRESS = "heardButtonPress";
    private static final String PAUSE_DURATION = "pauseDuration";
    private static final String ACTIVE_COLORS = "activeColors";
    private static final String CURRENT_SEQUENCE = "currentSequence";

    private boolean[] activeColors = new boolean[4]; //array of the active colors on our board
    private int[] currentSequence = new int[32]; //we keep track of current

    private int sequenceLength;
    private int sequenceIndex;
    private int totalLength;
    private int playerPosition;
    private long beepDuration;
    private long mLastUpdate;
    private int gameMode;
    private int winToneIndex;
    private int theGame;

    private static final Random RNG = new Random();
    private boolean isLit;
    private boolean heardButtonPress;  // Avoid a race of: down -> listen -> up.
    private long pauseDuration;

    public MemTrain(Context context) { //object
        for (int i = 0; i < TOTAL_BUTTONS; ++i) { //while there are no buttons in play
            buttonPressMap[i] = false; //no buttons are being pressed
        }

        //mapping soundIds to our button layout using SoundPool
        soundPool = new SoundPool(TOTAL_BUTTONS, AudioManager.STREAM_MUSIC, 0);
        soundIds[GREEN] = soundPool.load(context, R.raw.green, 1);
        soundIds[RED] = soundPool.load(context, R.raw.red, 1);
        soundIds[YELLOW] = soundPool.load(context, R.raw.purple, 1);
        soundIds[BLUE] = soundPool.load(context, R.raw.blue, 1);
        soundIds[VICTORY_SOUND] = soundPool.load(context, R.raw.mbison, 1);//Might need to fix
        soundIds[LOSE_SOUND] = soundPool.load(context, R.raw.lose, 1);

		/* initializing */
        mLastUpdate = System.currentTimeMillis(); //generic update
        gameMode = IDLE; //game now waits for instruction
        isLit = false; //no buttons in play
        heardButtonPress = false; //no press allowed until gamestart
        winToneIndex = 0;

    }

    //save the state and various indexes of the game
    public Bundle saveState(Bundle map) {
        if (map != null) {
            map.putInt(THE_GAME, Integer.valueOf(theGame));
            map.putString(CURRENT_SEQUENCE, parseSequenceToString(currentSequence, sequenceLength));
            map.putInt(SEQUENCE_INDEX, Integer.valueOf(sequenceIndex));
            map.putInt(TOTAL_LENGTH, Integer.valueOf(totalLength));
            map.putInt(PLAYER_POSITION, Integer.valueOf(playerPosition));
            map.putInt(WIN_TONE_INDEX, Integer.valueOf(winToneIndex));
            map.putLong(BEEP_DURATION, Long.valueOf(beepDuration));
            map.putLong(PAUSE_DURATION, Long.valueOf(pauseDuration));
            map.putBooleanArray(ACTIVE_COLORS, activeColors);
            map.putBoolean(IS_LIT, Boolean.valueOf(isLit));
            map.putBoolean(HEARD_BUTTON_PRESS, Boolean.valueOf(heardButtonPress));
            map.putInt(GAME_MODE, Integer.valueOf(gameMode));
        }
        return map;
    }

    void scaleBeepDuration(int index) { //beep length
        if (index < 6) beepDuration = 420;         //.42s
        else if (index < 14) beepDuration = 320; // .32s
        else beepDuration = 220;                //  .22s
        beepDuration -= TICK_COMPENSATION;
        if (beepDuration < 0) beepDuration = 0;
    }

    private UpdateHandler mUpdateHandler = new UpdateHandler(); //handler for delays/UI

    class UpdateHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UI:
                    MemTrain.this.update();
                    break;
            }
        }

        public void sleep(long delayMillis) {
            this.removeMessages(UI);
            sendMessageDelayed(obtainMessage(UI), delayMillis);
        }
    }

    public void setLevel(int level) { //only one level for now, can set more with use cases
        int savedTotalLength = totalLength;

        switch (level) {
            case 1:
                totalLength = 32; //SET TO RUN A MAX PATTERN OF 32 WITHOUT LEVELS
                break;
            default:
                totalLength = 4;    // Should never be here but we need a default so whatever.
                break;
        }
        if (totalLength != savedTotalLength) {    // If we changed the game level we reset the game.
            if (pauseDuration > 0) pauseDuration = 0;  // Go directly to idle, and don't pause.
            if (isLit) playNext();      // If there's a button lit, turn it off.
            gameMode = IDLE;
            sequenceIndex = 0;
            // Can set sequenceLength to 0 but it works without it so yolo
        }
    }

    private String parseSequenceToString(int[] array, int length) { //generic string parser
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(array[i]);
        }
        return sb.toString();
    }

    public void update() {
        long now = System.currentTimeMillis(); //update time to current
        long delay = beepDuration;    // Events are normally the length of a beep.
        if (isLit) delay = BETWEEN_DURATION;   //delay 50ms after turning off a lit light.

        if (gameMode != LISTENING) { //if we are not interacting with user
            if (now - mLastUpdate > delay) { //check if we updated already, if not
                playNext(); //head to playNext method
                mLastUpdate = now; //tell mLastUpdate we updated the sequence at this time
            }
            mUpdateHandler.sleep(delay);
        }
    }

    /*Main logic of the program to play next sequence with proper sound and animation as well as
     proper state updates */

    public void playNext() {
        if (pauseDuration > 0) {  // delay check
            pauseDuration = 0;
            return;
        }
        switch (gameMode) {
            case PLAYING:  //  Play the current sequence if we are playing the game.
                if (sequenceIndex < sequenceLength) {    // Keep playing
                    if (isLit) { //if our light is on
                        showButtonRelease(currentSequence[sequenceIndex]); // Stop previous tone.
                        isLit = false; //turn it off
                        sequenceIndex++; // Point at next index
                        if (sequenceIndex == sequenceLength) { // check to see if we played last tone
                            if (gameMode == PLAYING) {   // if we're playing begin listening for input.
                                sequenceIndex = 0;     // use sequenceIndex as match cursor.
                                gameMode = LISTENING;	// switch to Listen when button release feedback is done.
                            } else
                                gameMode = IDLE;  // or go to Idle state after replay.
                        }
                    } else {
                        showButtonPress(currentSequence[sequenceIndex]);    // Flash and beep current.
                        isLit = true; //our light is now on
                    }
                } // Fall through and do nothing if we're past the end of the sequence.
                break;
        }
    }

    public void playCurrent() { //update that system that we are playing currently
        gameMode = PLAYING;
        sequenceIndex = 0;
        update();
    }

    public int getRandomColor() { //RNG pick a color to beep
        int returnval = RNG.nextInt(4);
        if (theGame == 3) {            // Filter out inactive colors
            while (activeColors[returnval] == false)
                returnval = RNG.nextInt(4);  // Keep trying till we get an active.
        }
        return returnval;
    }

    public void gameStart() { //initiates MemTrain activity
        for (int i = 0; i < 4; i++) { //loop through all button indexes
            activeColors[i] = true;            // Mark all colors active.
        }

		/* In case user was fast on the draw: Reset all buttons. */
        for (int index = 0; index < 4; index++) {
            showButtonRelease(index);
        }

        winToneIndex = 0;
        sequenceLength = 1;
        scaleBeepDuration(1);
        playerPosition = 1;
        currentSequence[0] = getRandomColor(); //value of our rng color determines current sequence index in our array
        playCurrent(); //we are now playing the game
    }

    public void gameLose() {
        if (isAHS) {
            e.setText("New High Score!\n" + hScore);
            e.setGravity(Gravity.CENTER);
            isAHS = false;
            doStream(soundIds[VICTORY_SOUND]);
        }
        else {
            e.setText("FAIL!");
            doStream(soundIds[LOSE_SOUND]);
        }
        hScore = 0;
        startButtonIsOn = false;

        if (theGame == 3) {   // In game 3 we eliminate a color and start again.
            int activeColorCount = 0;
            for (int i = 0; i < 4; i++) {
                if (activeColors[i]) activeColorCount++;
            }
        } else {
            gameMode = LOST;
            update();
        }
    }

    public void gameCycle() {
        mLastUpdate = System.currentTimeMillis();
        pauseDuration = 800;        // Wait .8s after last key pressed to play next for game 1 and 3.
        playerPosition = 1;
        update();
        playCurrent();
    }

	/* pressButton is called by the Touch Handler in response to the user... we calculator if the user is
	 correct first, then we show the release animation */

    public void pressButton(int buttonIndex) {
        if (gameMode != LISTENING) return;        // Only examine values when game is in play.
        // Guard against entering LISTENING state between a press and a release.
        heardButtonPress = true;
        // Logic for game 2:  We take user input as next color and fall through to normal case.
        if (playerPosition > sequenceLength) {
            currentSequence[sequenceIndex] = buttonIndex;
            sequenceLength++;
            playerPosition++;        // Point past new end of list and trigger restart of matching.
        }

        if (currentSequence[sequenceIndex] == buttonIndex) {    // showButton only if match.
            showButtonPress(buttonIndex);
        } else {
            doStream(soundIds[LOSE_SOUND]);
            if (theGame == 3) {        // Eliminate color that was pressed in game 3.
                activeColors[buttonIndex] = false;
            }
            gameLose();
        }
    }

    public void showButtonPress(int index) {
        if (index >= 0 && index < TOTAL_BUTTONS) { //if our index exists
            if (buttonPressMap[index] == false) { //and our index on the map is not pressed
                buttonPressMap[index] = true; //press it

                switch (gameMode) { //then enter the gameMode you are in
                    case LOSING: //if we are losing play the lose sound
                        doStream(soundIds[LOSE_SOUND]);
                        return;
                    case LISTENING:
                        if (currentSequence[sequenceIndex] == index) //if we match our index with the system
                            doStream(soundIds[index]); //play that corresponding sound
                        else
                            doStream(soundIds[LOSE_SOUND]); //otherwise play the lose sound
                        break;
                    default:
                        doStream(soundIds[index]); //we play the sound for the corresponding color
                        break;
                }
                for (Listener listener : listeners) {
                    listener.buttonStateChanged(index); //invalidate to buffer pixels correctly
                }
            }
        }
    }

    public void releaseAllButtons() { //we reset our buttonpressmap boolean and turn all buttons off
        for (int i = 0; i < buttonPressMap.length; ++i) {
            buttonPressMap[i] = false;
        }
        for (Listener listener : listeners) {
            listener.multipleButtonStateChanged(); //invalidate once again to ensure smooth bitmap
        }
    }

    /* releaseButton calculates if the user is correct and then displays the release animation */

    public void releaseButton(int buttonIndex) { //logic for releasing a single button
        if (gameMode != LISTENING) return;
        // Guard against acting on a button press that happened before we were LISTENING.
        if (heardButtonPress == false) return;

        heardButtonPress = false;            // Reset our heardButtonPress state.
        mLastUpdate = System.currentTimeMillis();
        if (sequenceIndex < sequenceLength) {
            if (currentSequence[sequenceIndex] == buttonIndex) { // Matched. Continue.
                showButtonRelease(buttonIndex);            // showButtonRelease only if match.
                sequenceIndex++; //point to next index
                if (sequenceIndex == sequenceLength) {
                    //SCORING SYSTEM LOGIC
                    if (hScore == 0) {
                        hScore = 10;
                    }
                    else if (hScore < 0) {
                        if (hScore == -10) {
                            hScore = 0;
                        }
                        else {
                            hScore /= 2;
                        }
                    }
                    else {
                        hScore *= 2;
                    }
                    e.setText("Score: " + hScore);
                    if (hScore > ultimateHs) {
                        ultimateHs = hScore;
                        saveHighScore();
                        isAHS = true;
                    }
                    if (sequenceLength < totalLength) {  // add one more.
                        if (theGame == 2) {  // in game 2, user adds next item in sequence
                            if (playerPosition > sequenceLength) {
                                playerPosition = 1;
                                sequenceIndex = 0;        // we added one. Now restart matching sequence.
                            } else {
                                playerPosition++;        // set the stage for adding to sequence on next button press.
                            }
                        } else {
                            sequenceLength++;
                            playerPosition = 1;
                            scaleBeepDuration(sequenceLength);
                            currentSequence[sequenceIndex] = getRandomColor(); //get another color
                            gameCycle(); //cycle the game to play next color
                        }
                    }
                } else {
                    playerPosition++;
                }
            } else {
                if (theGame == 3) {        // Eliminate color that was pressed in game 3.
                    activeColors[buttonIndex] = false;
                }
                gameLose();
            }
        }
    }

    public void playLast() { //used with our Play Last button to play the current sequence again
        for (int index = 0; index < 4; index++) {
            showButtonRelease(index); //In case user was fast on the draw: Reset all buttons.
        }
        gameMode = PLAYING;
        sequenceIndex = 0;
        update();
    }

    //Loading the High Score
    public int loadHighScore() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        int hs = sharedPreferences.getInt("HighScore", 0);

        if (hs == 0) {
        }
        else {
            ultimateHs = hs;
        }
        return ultimateHs;
    }

    //Saving the High Score
    public void saveHighScore() {
        if (ultimateHs >= hScore) {
            editor.putInt("HighScore", ultimateHs);
            editor.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        model.saveState(outState);
    }

    //default constructor to fix error in android manifest file
    public MemTrain() {
    }

    @Override
    public void onBackPressed() {
        startActivity(mainmenu);
    }
}