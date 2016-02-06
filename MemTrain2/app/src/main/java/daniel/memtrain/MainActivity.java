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
        chipTune.start(); //start immediately when game opens

        //initialing some buttons for the main activity
        play = (ImageButton) findViewById(R.id.startGame);
        play.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        play.setImageResource(R.drawable.sgametwo);
                        break;
                    case MotionEvent.ACTION_UP:
                        play.setImageResource(R.drawable.sgameone);
                        chipTune.pause();
                        startActivity(newGame);
                        break;
                }
                return false;
            }
        });

        highScore = (ImageButton) findViewById(R.id.highScore);
        highScore.setOnTouchListener(new View.OnTouchListener() {
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

        help = (ImageButton) findViewById(R.id.help);
        help.setOnTouchListener(new View.OnTouchListener() {
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

       /* credits = (ImageButton) findViewById(R.id.credits);
        credits.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        credits.setImageResource(R.drawable.creditshelpbuttontwo);
                        break;
                    case MotionEvent.ACTION_UP:
                        credits.setImageResource(R.drawable.creditshelpbuttonone);
                        startActivity(creditsPage);
                        break;
                }
                return false;
            }
        }); */
    }
}
