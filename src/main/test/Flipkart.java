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

public class Flipkart {
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


// To log in flipkart
@Test
public void login() {
try
{
Thread.sleep(1000);
}
catch(Exception e)
{
driver.findElement(By.partialLinkText("Login")).click();
driver.findElement(
By.cssSelector("input[placeholder='Enter email/mobile']"))
.sendKeys(username);
driver.findElement(
By.cssSelector("input[placeholder='Enter password']"))
.sendKeys(password);
driver.findElement(By.cssSelector("input[value='Login'][class='submit-btn login-btn btn']")).click();
}
}

@AfterClass
public void quit() {
driver.quit();
}
}
