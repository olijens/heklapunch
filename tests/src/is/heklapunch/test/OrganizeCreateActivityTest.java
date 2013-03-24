package is.heklapunch.test;

import is.heklapunch.OrganizeCreateActivity;
import android.annotation.SuppressLint;
import android.test.ActivityInstrumentationTestCase2;

public class OrganizeCreateActivityTest extends
		ActivityInstrumentationTestCase2<OrganizeCreateActivity> {

	private OrganizeCreateActivity mActivity;

	@SuppressLint("NewApi")
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		setActivityInitialTouchMode(false);

		mActivity = getActivity();

	}
	
	@SuppressWarnings("deprecation")
	public OrganizeCreateActivityTest() {
        super("", OrganizeCreateActivity.class);
	}
	
	@SuppressLint("NewApi")
	public OrganizeCreateActivityTest(
			Class<OrganizeCreateActivity> activityClass) {
		super(activityClass);
		// TODO Auto-generated constructor stub
	}

	public void testPreConditions() {
		assertTrue(mActivity.courses != null);
	} 
}
