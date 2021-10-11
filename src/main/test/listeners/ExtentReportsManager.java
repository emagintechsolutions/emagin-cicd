package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentReportsManager {
	private static ExtentReports extent;
	private static String reportFileName = "TestReport.html";
	private static String ubuntuPath = System.getProperty("user.dir") + "/TestReport";
	private static String ubuntuReportFileLoc = ubuntuPath + "/" + reportFileName;

	public static ExtentReports getInstance() {
		if (extent == null)
			createInstance();
		return extent;
	}

	// Create an extent report instance
	public static ExtentReports createInstance() {
		String fileName = ubuntuReportFileLoc;
		ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(fileName);
		htmlReporter.config().setTheme(Theme.STANDARD);
		htmlReporter.config().setDocumentTitle(fileName);
		htmlReporter.config().setEncoding("utf-8");
		htmlReporter.config().setReportName(fileName);
		htmlReporter.config().setAutoCreateRelativePathMedia(false);

		extent = new ExtentReports();
		extent.attachReporter(htmlReporter);

		return extent;
	}

}