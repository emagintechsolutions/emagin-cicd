 package com.objectrepository.demo;
 import org.openqa.selenium.*;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import java.net.MalformedURLException;
 import java.net.URL;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.testng.Assert;
 import org.testng.annotations.*;
 import java.util.concurrent.TimeUnit;
 
 public class SeleniumGridclicknext{

     WebDriver driver;
     String baseURL, nodeURL;

     @BeforeClass
     public void setUp() throws MalformedURLException {
         baseURL = "https://www.w3schools.com/java/default.asp";
         nodeURL = "http://zalenium.kodewatch.svc.cluster.local:4444/wd/hub";
         DesiredCapabilities capability = DesiredCapabilities.chrome();
         capability.setPlatform(Platform.LINUX);
         driver = new RemoteWebDriver(new URL(nodeURL), capability);
         driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);         
     }

     @AfterClass
     public void afterTest() {
         driver.quit();
     }
	 
    @Test
    public void clicknext() {
    try
    {
    Thread.sleep(1000); 
    
        driver.get(baseURL);
        driver.findElement(By.className("w3-right w3-btn")).click();
     
    }
    catch(Exception e)
    {
        
    }
    }
	}
