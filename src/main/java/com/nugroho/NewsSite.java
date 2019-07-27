package com.nugroho;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import sun.misc.IOUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class NewsSite {
    protected WebDriver driver;
    Set<Cookie> cookies;
    Gson gson = new Gson();
    protected Logger logger = Logger.getLogger(NewsSite.class);


    public NewsSite(WebDriver driver){
        this.driver = driver;
    }

    protected abstract String getLoginPage();

    protected abstract String getTodayEpaperPage();

    protected abstract boolean isLoggedIn();

    protected abstract boolean doLogin();

    protected abstract List<String> getImagesElements();

    protected abstract String getSaveFileName();

    public void login(){
        driver.get(getLoginPage()+"ajfair298"); //404 not found
        try(FileReader fileReader = new FileReader("utils/cookies.json")){
            JsonReader jsonReader = new JsonReader(fileReader);
            Type type = new TypeToken<Set<Cookie>>(){}.getType();
            cookies = gson.fromJson(jsonReader,type);
            for(Cookie cookie:cookies){
                logger.info(cookie);
                driver.manage().addCookie(cookie);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!isLoggedIn()){
            doLogin();
        }
    }

    public void saveCookies(){
        gson.toJson(cookies);
        try(FileWriter writer = new FileWriter("utils/cookies.json")){
            JsonWriter jsonWriter = new JsonWriter(writer);
            gson.toJson(cookies,Set.class,jsonWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info(cookies);
    }

    public void getImages(){
        login();
        driver.get(getTodayEpaperPage());
        List<String> imageUrls = getImagesElements();
        PDDocument document = new PDDocument();
        for(String imageUrl:imageUrls){
            try {
                processImage(imageUrl,document);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        try {
            document.save("out/"+getSaveFileName());
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processImage(String imageUrl, PDDocument document) throws MalformedURLException {
        logger.debug(String.format("processing %s", imageUrl));
        Set<Cookie> cookies = driver.manage().getCookies();
        URL url = new URL(imageUrl);
        try {
            URLConnection connection = url.openConnection();
            String cookieString = "";
            for(Cookie cookie:cookies){
                cookieString+=String.format("%s=%s; ",cookie.getName(),cookie.getValue());
                cookieString+=String.format("%s=%s; ","path",cookie.getPath());
                cookieString+=String.format("%s=%s; ","domain",cookie.getDomain());
            }
            cookieString = cookieString.substring(0,cookieString.length()-2);
            logger.debug(cookieString);
            System.setProperty("http.agent", "Chrome");
            connection.addRequestProperty("User-Agent", "Mozilla/4.76");
            connection.addRequestProperty("Cookie",cookieString);
            InputStream in = connection.getInputStream();
//            byte[] read = new byte[100];
//            int i = in.read(read);
//            while(i>0){
//                logger.debug(i);
//                i = in.read(read);
//            }
            // Read the image and close the stream
            Image image = ImageIO.read(in);
            float width = image.getWidth(null);
            float height = image.getHeight(null);
            PDPage page = new PDPage(new PDRectangle(width, height));
            document.addPage(page);
            PDImageXObject img = LosslessFactory.createFromImage(document,toBufferedImage(image));
//            PDImageXObject img = PDImageXObject.createFromByteArray(document, org.apache.pdfbox.io.IOUtils.toByteArray(in),imageUrl);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.drawImage(img, 0, 0);
            contentStream.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
}
