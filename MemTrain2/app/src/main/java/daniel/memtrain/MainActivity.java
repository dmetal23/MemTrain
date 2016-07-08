package daniel.memtrain;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.app.Activity;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

//THIS IS OUR MAIN MENU
public class MainActivity extends Activity {
    MediaPlayer chipTune; //our menu music
    ImageButton play, help, highScore, credits; //our menu buttons

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent newGame = new Intent(this, MemTrain.class);
        final Intent hsPage = new Intent(this, HighScore.class);
        final Intent helpPage = new Intent(this, Help.class);
        //final Intent creditsPage = new Intent(this,Credits.class);

        chipTune = MediaPlayer.create(this, R.raw.jakim); //create music
        chipTune.setLooping(true); //will loop infinitely
        chipTune.start(); //start immediately when the main menu opens



        //initialing some buttons for the main activity
        play = (ImageButton) findViewById(R.id.startGame);//a button that opens the actual game
        play.setOnTouchListener(new View.OnTouchListener() { //we use a listener to listen for a tap
            @Override
            public boolean onTouch(View v, MotionEvent event) { //a motionevent is used to detect a tap
                switch (event.getAction()) { //depending on our action state
                    case MotionEvent.ACTION_DOWN: //when we tap down
                        play.setImageResource(R.drawable.sgametwo); //show the 'button pressed' image
                        break;
                    case MotionEvent.ACTION_UP: //when we let go of the tap on the 'up' motion
                        play.setImageResource(R.drawable.sgameone); //set back default button image
                        chipTune.pause(); //pause the music when we go into our game
                        startActivity(newGame); //we start our actual game Intent in MemTrain
                        break;
                }
                return false;
            }
        });

        highScore = (ImageButton) findViewById(R.id.highScore); //a button that opens the high score page
        highScore.setOnTouchListener(new View.OnTouchListener() { //same logic as before
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        highScore.setImageResource(R.drawable.highscoretwo);
                        break;
                    case MotionEvent.ACTION_UP:
                        highScore.setImageResource(R.drawable.highscoreone);
                        startActivity(hsPage);
                        break;
                }
                return false;
            }
        });

        help = (ImageButton) findViewById(R.id.help); //a button that opens the help page
        help.setOnTouchListener(new View.OnTouchListener() { //same logic as before
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        help.setImageResource(R.drawable.helptwo);
                        break;
                    case MotionEvent.ACTION_UP:
                        help.setImageResource(R.drawable.helpone);
                        startActivity(helpPage);
                        break;
                }
                return false;
            }
        });

    }
}
