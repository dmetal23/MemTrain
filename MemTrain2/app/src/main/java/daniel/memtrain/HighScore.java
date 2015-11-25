package daniel.memtrain;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class HighScore extends Activity {
    private int highscore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        loadHighScore();

        TextView screenText = (TextView)findViewById(R.id.hsText);
        String fontPath = "font/arcadepix.TTF";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        screenText.setTypeface(tf);
        screenText.setTextSize(30);
        screenText.setText("High Score: " + highscore);
    }

    public void loadHighScore() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        int hs = sharedPreferences.getInt("HighScore", -999);

        if (hs == -999) {

        }
        else {
            highscore = hs;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_high_score, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
