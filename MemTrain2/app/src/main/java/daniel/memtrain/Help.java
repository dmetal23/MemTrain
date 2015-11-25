package daniel.memtrain;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


public class Help extends Activity {
    ImageButton credits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView help = (TextView)findViewById(R.id.textView);
        TextView help2 = (TextView)findViewById(R.id.textView2);
        String fontPath = "font/arcadepix.TTF";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        help.setTypeface(tf);
        help2.setTypeface(tf);

        final Intent creditsPage = new Intent(this,Credits.class);

        credits = (ImageButton) findViewById(R.id.credits);
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
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help, menu);
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
