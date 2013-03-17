package is.heklapunch;

import android.os.Bundle;

public class OrganizeReceiveActivity extends BlueToothActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ensureDiscoverable();
		
		setContentView(R.layout.activity_organize_receive);
	}
}
