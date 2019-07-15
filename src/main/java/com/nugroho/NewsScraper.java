package com.nugroho;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class NewsScraper {
    final static Logger logger = Logger.getLogger(NewsScraper.class);
    static WebDriver driver;

    public static void main(String args[]){
        logger.info("test");
        driver = new ChromeDriver();
        Kompas kompas = new Kompas(driver);
        kompas.getImages();
    }
}
