package com.sampletestpackage;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.net.URL;
import java.net.MalformedURLException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.*;

public class Flipkartlogout {
         WebDriver driver;
         String baseURL, nodeURL;


         String username = ""; // Change to your username and passwrod
         String password = "";

// This method is to navigate flipkart URL
@BeforeClass
public void setUp() throws MalformedURLException {
         baseURL = "https://www.flipkart.com";
         nodeURL = "http://zalenium.kodewatch.svc.cluster.local:4444/wd/hub";
         DesiredCapabilities capability = DesiredCapabilities.chrome();
         capability.setBrowserName("chrome");
         capability.setPlatform(Platform.LINUX);
         driver = new RemoteWebDriver(new URL(nodeURL), capability);
}


@Test
public void logout() {
try
{
Thread.sleep(1000);
}
catch(Exception e)
{
Actions act = new Actions(driver);
WebElement user = driver.findElement(By.partialLinkText(username));
act.moveToElement(user).build().perform();
driver.findElement(By.linkText("Logout")).click();
}
}
@AfterClass
public void quit() {
driver.quit();
}


}
