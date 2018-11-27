package nl.utwente.di.OVSoftware.seleniumTest;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.util.List;

public class OVSeleniumTest {

	/*
	 * Before running the test, make sure your path is configured properly.
	 */

	private static final String PATH = "/Users/akhilbandi/Desktop/Module4/apache-tomcat-8.0.51/chromedriver";
	// this file change anro's payrate
	private static final String EXPORT_FILE_PATH = "/Users/akhilbandi/Downloads/export.csv"; // change export file
																									// location here
	private static final String LOGIN_URL = "http://farm03.ewi.utwente.nl:7015/mod04di08";
	private static final String MAIN_URL = "http://farm03.ewi.utwente.nl:7015/mod04di08/main.html";
	private static final String ADMIN_URL = "http://farm03.ewi.utwente.nl:7015/mod04di08/admin.html";
	static WebElement username;
	static WebElement password;
	static WebElement loginbutton;
	static WebElement sidebarbut;
	static WebElement empnum;
	static WebElement empname;
	static WebElement statusbut;
	static WebElement statusA;
	static WebElement statusI;
	static WebElement statusH;
	static WebElement searchbut;
	static WebElement officebut;
	static WebElement importbut;
	static WebElement exportbut;
	static WebElement logoutbutton;
	static WebElement resultEmp;
	static List<WebElement> resultEmpName;
	static List<WebElement> resultEmpStatuses;
	static List<WebElement> resultEmpIds;
	static WebElement sidebartoggle;
	static WebElement sidebarpayrates;
	static WebElement sidebaradministration;
	static WebElement googleradio;
	static WebElement ovradio;
	static WebElement addbut;
	static WebElement fullnamesort;
	static WebElement empnumsort;
	private static WebDriver driver;

	public static void setUpDriver() {
		System.setProperty("webdriver.chrome.driver", PATH);
		driver = new ChromeDriver();
	}

	public static void loadPage(String url) {
		try {
			driver.get(url);
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void constructLoginElements() {
		username = driver.findElement(By.id("username"));
		password = driver.findElement(By.id("password"));
		loginbutton = driver.findElement(By.xpath("/html/body/div/form/div[4]/button"));
	}

	public static void constructMainElements() {
		sidebarbut = driver.findElement(By.id("menu-toggle"));
		empnum = driver.findElement(By.id("searchid"));
		empname = driver.findElement(By.id("searchname"));
		statusbut = driver.findElement(By.id("statusButton"));
		statusA = driver.findElement(By.id("statusA"));
		statusI = driver.findElement(By.id("statusI"));
		statusH = driver.findElement(By.id("statusH"));
		searchbut = driver.findElement(By.id("searchmain"));
		officebut = driver.findElement(By.id("officeButton"));
		importbut = driver.findElement(By.id("import"));
		exportbut = driver.findElement(By.id("export"));
		logoutbutton = driver.findElement(By.id("logout"));
		resultEmp = driver.findElement(By.id("EmployeeList"));
		sidebartoggle = driver.findElement(By.xpath("//*[@id=\"menu-toggle\"]"));
		sidebarpayrates = driver.findElement(By.xpath("//*[@id=\"sidebar-wrapper\"]/ul/li[2]/a"));
		sidebaradministration = driver.findElement(By.xpath("//*[@id=\"sidebar-wrapper\"]/ul/li[3]/a"));
		fullnamesort = driver.findElement(By.xpath("//*[@id=\"hovername\"]"));
		empnumsort = driver.findElement(By.xpath("//*[@id=\"hovernum\"]"));
	}

	public static void constructAdminElements() {
		sidebartoggle = driver.findElement(By.xpath("//*[@id=\"menu-toggle\"]"));
		sidebarpayrates = driver.findElement(By.xpath("//*[@id=\"sidebar-wrapper\"]/ul/li[2]/a"));
		sidebaradministration = driver.findElement(By.xpath("//*[@id=\"sidebar-wrapper\"]/ul/li[3]/a"));
		googleradio = driver.findElement(By.xpath("//*[@id=\"googleSelector\"]"));
		ovradio = driver.findElement(By.xpath("//*[@id=\"OVSelector\"]"));
		addbut = driver.findElement(By.xpath("//*[@id=\"card-style\"]/div/div/button"));
		logoutbutton = driver.findElement(By.xpath("//*[@id=\"card-style\"]/div/div/div[4]/button"));
	}

	public static void testLoginOVAccount() {
		username.sendKeys("a");
		password.sendKeys("a");

		loginbutton.click();
		try {
			Thread.sleep(500);
			if (driver.getCurrentUrl().contains("main.html")) {
				System.out.println("Login is succesful!");
			} else {
				System.out.println("Login failed.");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void testSearchFunction() {
		empnum.sendKeys("5");
		empname.sendKeys("MaSt");
		searchbut.click();

		try {
			Thread.sleep(500);
			if (resultEmp.getText().contains("MaSt") && resultEmp.getText().contains("5")) {
				System.out.println("search succeeded.");
			} else {
				System.out.println("search failed.");
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void testStatusFunction() {

		try {
			loadPage(MAIN_URL);
			constructMainElements();
			// test status active
			statusbut.click();
			statusA.click();
			Thread.sleep(500);
			resultEmpStatuses = driver.findElements(By.id("employeestatus"));
			for (WebElement e : resultEmpStatuses) {
				if (!e.getText().equals("Active")) {
					System.out.println("searching active employee failed");
					break;
				}
			}

			// test status inactive
			statusbut.click();
			statusI.click();

			Thread.sleep(500);
			resultEmpStatuses = driver.findElements(By.id("employeestatus"));
			for (WebElement e : resultEmpStatuses) {
				if (!e.getText().equals("Not Active")) {
					System.out.println("searching inactive employee failed");
					break;
				}
			}

			// test status not active yet
			statusbut.click();
			statusH.click();

			Thread.sleep(500);
			resultEmpStatuses = driver.findElements(By.id("employeestatus"));
			for (WebElement e : resultEmpStatuses) {
				if (!e.getText().equals("Not Active Yet")) {
					System.out.println("searching not active yet employee failed");
					break;
				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Test status function done.");

	}

	public static void testLogOut() {

		try {
			logoutbutton.click();
			Thread.sleep(500);
			if (driver.getCurrentUrl().contains("login.html")) {
				System.out.println("log out is succesful");
			} else {
				System.out.println("log out is unsuccessful");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void testDeletePayrate() {
		try {
			Thread.sleep(500);

			WebElement deletepaybut = driver.findElement(By.xpath("//*[@id=\"infotable\"]/tr[2]/td[5]/span"));
			deletepaybut.click();
			Thread.sleep(500);

			WebElement closepopbut = driver.findElement(By.xpath("//*[@id=\"empInfo\"]/div/div/div[1]/button/span"));
			closepopbut.click();

			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void testEditPayrate() {
		try {
			Thread.sleep(500);
			WebElement editpaybut = driver.findElement(By.xpath("//*[@id=\"infotable\"]/tr[2]/td[4]/span"));
			editpaybut.click();
			WebElement epayrate = driver.findElement(By.xpath("//*[@value=\"45\"]"));
			WebElement epayratefrom = driver.findElement(By.xpath("//*[@value=\"2017-01-01\"]"));
			WebElement epayrateuntil = driver.findElement(By.xpath("//*[@value=\"2017-12-31\"]"));

			epayrate.clear();
			epayratefrom.clear();
			epayrateuntil.clear();

			epayrate.sendKeys("55");
			epayratefrom.sendKeys("2017-01-01");
			epayrateuntil.sendKeys("2018-12-31");

			WebElement savebut = driver.findElement(By.xpath("//*[@id=\"empInfo\"]/div/div/div[3]/button[3]"));
			savebut.click();
			Thread.sleep(500);

			WebElement closepopbut = driver.findElement(By.xpath("//*[@id=\"empInfo\"]/div/div/div[1]/button/span"));
			closepopbut.click();
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void testAddPayrate() {
		try {
			Thread.sleep(500);
			WebElement addpayratebut = driver.findElement(By.xpath("//*[@id=\"empInfo\"]/div/div/div[3]/button[1]"));
			addpayratebut.click();
			WebElement newpayrate = driver.findElement(By.xpath("//*[@id=\"infotable\"]/tr[2]/td[1]/input"));
			WebElement newpayratefrom = driver.findElement(By.xpath("//*[@id=\"infotable\"]/tr[2]/td[2]/input"));
			WebElement newpayrateuntil = driver.findElement(By.xpath("//*[@id=\"infotable\"]/tr[2]/td[3]/input"));

			newpayrate.clear();
			newpayratefrom.clear();
			newpayrateuntil.clear();

			newpayrate.sendKeys("45");
			newpayratefrom.sendKeys("2017-01-01");
			newpayrateuntil.sendKeys("2017-12-31");

			WebElement savebut = driver.findElement(By.xpath("//*[@id=\"empInfo\"]/div/div/div[3]/button[3]"));
			savebut.click();
			Thread.sleep(500);

			WebElement closepopbut = driver.findElement(By.xpath("//*[@id=\"empInfo\"]/div/div/div[1]/button/span"));
			closepopbut.click();
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void testOfficeFunction() {
		try {
			Thread.sleep(500);
			officebut.click();
			Thread.sleep(500);
			WebElement belgieoff = driver.findElement(By.xpath("//*[@id=\"officeDropdown\"]/a[2]"));
			belgieoff.click();
			Thread.sleep(500);

			officebut.click();
			Thread.sleep(500);
			WebElement amsoff = driver.findElement(By.xpath("//*[@id=\"officeDropdown\"]/a[1]"));
			amsoff.click();
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void testImportFunction() {

		try {

			WebElement anro = driver.findElement(By.xpath("//*[@id=\"EmployeeList\"]/tr[11]"));
			anro.click();
			Thread.sleep(4000);

			WebElement anroclose = driver.findElement(By.xpath("//*[@id=\"empInfo\"]/div/div/div[1]/button/span"));
			anroclose.click();
			Thread.sleep(400);

			WebElement importinput = driver.findElement(By.xpath("//*[@id=\"fileLoader\"]"));
			importinput.sendKeys(EXPORT_FILE_PATH);

			Thread.sleep(2000);

			anro.click();
			Thread.sleep(4000);

			// restore value from 20 into 0
			WebElement anroedit = driver.findElement(By.xpath("//*[@id=\"infotable\"]/tr/td[4]/span"));
			anroedit.click();
			Thread.sleep(400);
			WebElement anropay = driver.findElement(By.xpath("//*[@id=\"editedPayrate\"]"));
			anropay.clear();
			anropay.sendKeys("0");

			WebElement anrosave = driver.findElement(By.xpath("//*[@id=\"empInfo\"]/div/div/div[3]/button[3]"));
			anrosave.click();
			Thread.sleep(400);

			anroclose.click();
			Thread.sleep(400);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void testExportFunction() {
		try {
			exportbut.click();
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void testSidebar() {
		try {
			sidebartoggle.click();
			Thread.sleep(500);

			sidebaradministration.click();
			Thread.sleep(500);

			constructAdminElements();

			sidebartoggle.click();
			Thread.sleep(500);

			sidebarpayrates.click();
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void testAddGoogleAcc() {
		try {
			googleradio.click();
			Thread.sleep(500);
			WebElement emailinput = driver.findElement(By.xpath("//*[@id=\"email\"]"));
			emailinput.sendKeys("testing@domain.com");
			addbut.click();
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void testAddOVAcc() {
		try {
			ovradio.click();
			Thread.sleep(500);
			WebElement userinput = driver.findElement(By.xpath("//*[@id=\"card-style\"]/div/div/input[1]"));
			WebElement passinput = driver.findElement(By.xpath("//*[@id=\"card-style\"]/div/div/input[2]"));
			userinput.sendKeys("test");
			passinput.sendKeys("test");
			addbut.click();
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void testDeleteGoogleAcc() {
		try {
			WebElement deletemail = driver.findElement(By.xpath("//*[@id=\"GoogleAccountList\"]/tr[7]/td[2]"));
			deletemail.click();
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void testDeleteOVAcc() {
		try {
			WebElement deleteov = driver.findElement(By.xpath("//*[@id=\"OVAccountList\"]/tr[2]/td[3]"));
			deleteov.click();
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void testLogin() {
		loadPage(LOGIN_URL);
		constructLoginElements();
		testLoginOVAccount();
		// testLoginGoogleAccount();
	}

	public static void testSortingFunction() {

		try {
			fullnamesort.click();
			Thread.sleep(400);
			fullnamesort.click();
			Thread.sleep(400);
			empnum.click();
			Thread.sleep(400);
			empnum.click();
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void testMain() {
		// test main.html functions
		constructMainElements();

		testSortingFunction();
		testSearchFunction();
		testStatusFunction();

		loadPage(MAIN_URL);

		WebElement popupr1 = driver.findElement(By.xpath("//*[@id=\"EmployeeList\"]/tr[1]"));
		popupr1.click();
		testAddPayrate();

		popupr1.click();
		testEditPayrate();

		popupr1.click();
		testDeletePayrate();

		loadPage(MAIN_URL);
		constructMainElements();

		testOfficeFunction();

		testImportFunction();
		testExportFunction();
		testSidebar();
	}

	public static void testAdmin() {
		loadPage(ADMIN_URL);
		constructAdminElements();
		testAddGoogleAcc();
		testDeleteGoogleAcc();
		testAddOVAcc();
		testDeleteOVAcc();
	}

	public static void main(String[] args) {
		setUpDriver();
		testLogin();
		testMain();
		testAdmin();
		testLogOut();
	}

}
