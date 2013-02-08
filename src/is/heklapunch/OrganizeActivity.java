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
    	Intent k = new Intent(this, OrganizeModifyActivity.class);
        startActivity(k);
    }
    
	//Go to modify mode
    public void modify(View view) {
    	Intent k = new Intent(this, OrganizeCreateActivity.class);
        startActivity(k);
    }
    
	//Go to recieve mode
    public void recieve(View view) {
    	Intent k = new Intent(this, OrganizeReceiveActivity.class);
        startActivity(k);
    }
}
