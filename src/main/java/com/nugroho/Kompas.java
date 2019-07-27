package com.nugroho;

import com.google.gson.stream.JsonReader;
import com.nugroho.helper.Credential;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Kompas extends NewsSite {

    public Kompas(WebDriver driver) {
        super(driver);
    }

    @Override
    protected String getLoginPage() {
        return "https://kompas.id/my-account/";
    }

    @Override
    protected String getTodayEpaperPage() {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return "https://epaper.kompas.id/baca/kompaspagi/"+localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    @Override
    protected boolean isLoggedIn() {
        driver.get(getLoginPage());
        List<WebElement> webElements = driver.findElements(By.id("post-39"));
        return (webElements.size() > 0);
    }

    @Override
    protected boolean doLogin() {
        try(FileReader fileReader = new FileReader("utils/credentials.json")){
            JsonReader jsonReader = new JsonReader(fileReader);
            Credential credential = gson.fromJson(jsonReader, Credential.class);
            driver.findElement(By.id("username")).sendKeys(credential.getUsername());
            driver.findElement(By.id("password")).sendKeys(credential.getPassword());
            driver.findElement(By.id("password")).sendKeys(Keys.ENTER);
            cookies = driver.manage().getCookies();
            saveCookies();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected List<String> getImagesElements() {
        WebElement root = driver.findElement(By.xpath("//*[@id=\"container\"]/div/ul"));
        List<WebElement> imageElements = root.findElements(By.xpath("li/img"));
        List<String> ret = new ArrayList<>();
        for(WebElement element:imageElements){
            String[] parts = (element.getAttribute("src")).split("getthumb");
            ret.add(parts[0]+"getpreview"+parts[1]);
        }
        return ret;
    }

    @Override
    protected String getSaveFileName() {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return String.format("kompas %s.pdf",localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }
}
