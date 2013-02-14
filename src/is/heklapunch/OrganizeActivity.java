package is.heklapunch;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class OrganizeActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organize);
    }

	//Go to create mode
    public void create(View view) {
    	Intent m = new Intent(this, OrganizeCreateActivity.class);
        startActivity(m);
    }
    
	//Go to modify mode
    public void modify(View view) {
    	Intent c = new Intent(this, OrganizeModifyActivity.class);
        startActivity(c);
    }
    
	//Go to recieve mode
    public void receive(View view) {
    	Intent r = new Intent(this, OrganizeReceiveActivity.class);
        startActivity(r);
    }
}
