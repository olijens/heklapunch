package is.heklapunch;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	//Go to compete mode
    public void keppa(View view) {
    	Intent k = new Intent(this, KeppaActivity.class);
        startActivity(k);
    }
    
  //Go to organize mode
    public void organize(View view) {
    	Intent o = new Intent(this, OrganizeActivity.class);
        startActivity(o);
    }

}
