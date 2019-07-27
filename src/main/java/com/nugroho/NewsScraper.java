package com.nugroho;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;

public class NewsScraper {
    final static Logger logger = Logger.getLogger(NewsScraper.class);
    static WebDriver driver;

    public static void main(String args[]){
        logger.info("test");
        System.setProperty("webdriver.chrome.driver","lib/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        Kompas kompas = new Kompas(driver);
        kompas.getImages();
    }
}
