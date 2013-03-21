//handles courseTitlte/courseID pairs for display.

package is.heklapunch;

public class CourseData {
	private String courseTitle;
	private String courseID;
	
	public CourseData(String title, String ID) {
		courseTitle = title;
		courseID = ID;
	}

	public String getSpinnerText() {
		return courseTitle;
	}

	public String getValue() {
		return courseID;
	}

	public String toString() {
		return courseTitle;
	}
}
