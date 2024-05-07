/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import models.PixelValueInformation;
import models.Point;
import models.WithinClassVariance;

/**
 *
 * @author Darren
 */
public class Controller {
    
    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static void makeGray(BufferedImage img, float sliderValue) {
        float gamma = sliderValue/10;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);

//                int grayScale = (int) (0.3 * r + 0.59 * g + 0.11 * b);
//                int gray = (grayScale << 16) | (grayScale << 8) | grayScale;
//                img.setRGB(x, y, gray);
                // Normalize and gamma correct:
                double rr = Math.pow(r / 255.0, gamma);
                double gg = Math.pow(g / 255.0, gamma);
                double bb = Math.pow(b / 255.0, gamma);

                // Calculate luminance:
                double lum = 0.3 * rr + 0.59 * gg + 0.11 * bb;

                // Gamma compand and rescale to byte range:
                int grayLevel = (int) (255.0 * Math.pow(lum, 1.0 / gamma));
                int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel; 
                img.setRGB(x, y, gray);
            }
        }
    }
    
//    public static void restoreGamma(BufferedImage img, float sliderValue) {
//        float gamma = sliderValue/10;
//        for (int x = 0; x < img.getWidth(); x++) {
//            for (int y = 0; y < img.getHeight(); y++) {
//                int rgb = img.getRGB(x, y);
//                int r = (rgb >> 16) & 0xFF;
//
//                int restoreValue = (int) (Math.pow(((float)r), 1.0 / gamma));
//                int result = (restoreValue << 16) + (restoreValue << 8) + restoreValue; 
//                img.setRGB(x, y, result);
//            }
//        }
//    }
    
    public static void otsuThreshold(BufferedImage img) {
        int threshold = findMinimumWithinClassVariances(getWithinClassVariances(getAllUniquePixelsValue(img), (img.getWidth() * img.getHeight())));
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                
                if(r<threshold){
                    int background = (0 << 16);
                    img.setRGB(x, y, background);
                } else {
                    int foreground = (255 << 16) | (255 << 8) | 255;
                    img.setRGB(x, y, foreground);
                }
            }
        }
    }
    
    private static int findMinimumWithinClassVariances(ArrayList<WithinClassVariance> withinClassVariances) {
        int result = 0;
        double comparer = Double.MAX_VALUE;
        for (WithinClassVariance temp : withinClassVariances) {
            if (temp.getValue() < comparer) {
                comparer = temp.getValue();
                result = temp.getThreshold();
            }
        }

        return result;
    }
    
    private static ArrayList<WithinClassVariance> getWithinClassVariances(ArrayList<PixelValueInformation> uniquePixels, int totalPixels) {
        ArrayList<WithinClassVariance> result = new ArrayList<>();
        for(PixelValueInformation threshold : uniquePixels) {
            double backgroundWeight;
            double backgroundMean = 0;
            double totalBackgroundPixel = 0;
            double backgroundVariance = 0;
            for(int i = 0; i < uniquePixels.indexOf(threshold); i++) {
                totalBackgroundPixel += uniquePixels.get(i).getValue();
                backgroundMean += (uniquePixels.get(i).getQty() * uniquePixels.get(i).getValue());
            }
            backgroundWeight = totalBackgroundPixel / totalPixels;
            if (totalBackgroundPixel!=0) {
                backgroundMean /= totalBackgroundPixel;
            }
            for(int i = 0; i < uniquePixels.indexOf(threshold); i++) {
                backgroundVariance += (((uniquePixels.get(i).getValue()-backgroundMean) * (uniquePixels.get(i).getValue()-backgroundMean))* uniquePixels.get(i).getQty());//Re-check with the formula(Maybe need to times with qty) (Also maybe this line not conforming the formula) -> after check, this line seems to be the problem and already fixed
            }
            if (totalBackgroundPixel!=0) {
                backgroundVariance /= totalBackgroundPixel;
            }
            
            double foregroundWeight;
            double foregroundMean = 0;
            double totalForegroundPixel = 0;
            double foregroundVariance = 0;
            for(int i = uniquePixels.indexOf(threshold); i < uniquePixels.size(); i++) {
                totalForegroundPixel += uniquePixels.get(i).getValue();
                foregroundMean += (uniquePixels.get(i).getQty() * uniquePixels.get(i).getValue());
            }
            foregroundWeight = totalForegroundPixel / totalPixels;
            if (totalForegroundPixel!=0) {
                foregroundMean /= totalForegroundPixel;
            }
            for(int i = uniquePixels.indexOf(threshold); i < uniquePixels.size(); i++) {
                foregroundVariance += (((uniquePixels.get(i).getValue()-foregroundMean) * (uniquePixels.get(i).getValue()-foregroundMean)) * uniquePixels.get(i).getQty());
            }
            if (totalForegroundPixel!=0) {
                foregroundVariance /= totalForegroundPixel;
            }
            double withinClassVariance = (backgroundWeight * backgroundVariance) + (foregroundWeight * foregroundVariance);
            
            result.add(new WithinClassVariance(threshold.getValue(), withinClassVariance));
        }
        return result;
    }
    
    private static ArrayList<PixelValueInformation> getAllUniquePixelsValue(BufferedImage img) {
        ArrayList<PixelValueInformation> result = new ArrayList<>();
        int[] pixels = new int[img.getWidth()*img.getHeight()];
        int count = 0;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                pixels[count] = (img.getRGB(x, y) >> 16) & 0xFF;
                count++;
            }
        }
        Arrays.sort(pixels);
        int countQty = 1;
        for (int i = 0; i < pixels.length-1; i++) {
            if(pixels[i] != pixels[i+1]) {
                result.add(new PixelValueInformation(pixels[i], countQty));
                countQty = 1;
            } else {
                countQty++;
            }
        }    
        if (pixels.length > 0) {
            result.add(new PixelValueInformation(pixels[pixels.length - 1], countQty));
        }

        return result;
    }
    
    public static void makeImageNegative(BufferedImage img) {
        int max = findMaximumPixelValue(img);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                
                int negative = ((max-r) << 16) + ((max-r) << 8) + (max-r); 
                img.setRGB(x, y, negative);
            }
        }
    }
    
    private static int findMaximumPixelValue(BufferedImage img) {
        int result = 0;
        for(PixelValueInformation temp : getAllUniquePixelsValue(img)) {
            if (temp.getValue() > result) {
                result = temp.getValue();
            }
        }
        return result;
    }
    
    public static void thinning(BufferedImage img) {
        if (!(img.getWidth() < 3 || img.getHeight() < 3)) {
            ArrayList<Point> step1 = new ArrayList<>();
            ArrayList<Point> step2 = new ArrayList<>();
            boolean iterate = true;

            while (iterate) {
                step1.clear();
                for (int x = 1; x < img.getWidth()- 1; x++) {
                    for (int y = 1; y < img.getHeight() - 1; y++) {
                        if (((img.getRGB(x, y)>> 16) & 0xFF) == 0) {
                            int b = functionB(new Point(x, y), img);
                            if (b >= 2 && b <= 6) {
                                int a = functionA(new Point(x, y), img);
                                if (a == 1) {
                                    if (checkAtLeast1PositionIsWhite(new Point(x, y), 2, 4, 6, img)) {
                                        if (checkAtLeast1PositionIsWhite(new Point(x, y), 4, 6, 8, img)) {
                                            step1.add(new Point(x, y));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for (Point temp : step1) {
                    int result = (255 << 16) + (255 << 8) + 255; 
                    img.setRGB(temp.x, temp.y, result);
                }

                step2.clear();
                for (int x = 1; x < img.getWidth() - 1; x++) {
                    for (int y = 1; y < img.getHeight() - 1; y++) {
                        if (((img.getRGB(x, y)>> 16) & 0xFF) == 0) {
                            int b = functionB(new Point(x, y), img);
                            if (b >= 2 && b <= 6) {
                                int a = functionA(new Point(x, y), img);
                                if (a == 1) {
                                    if (checkAtLeast1PositionIsWhite(new Point(x, y), 2, 4, 8, img)) {
                                        if (checkAtLeast1PositionIsWhite(new Point(x, y), 2, 6, 8, img)) {
                                            step2.add(new Point(x, y));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for (Point temp : step2) {
                    int result = (255 << 16) + (255 << 8) + 255; 
                    img.setRGB(temp.x, temp.y, result);
                }

                if (step1.isEmpty() && step2.isEmpty()) {
                    iterate = false;
                }
            }
        }
    }
    
    private static int functionA(Point p, BufferedImage img){
        int count=0;
        for (int i = 2; i<9; i++) {
            Point p1 = getPosition(p, i, img);
            int val1 = (img.getRGB(p1.x, p1.y)>> 16) & 0xFF;
            
            Point p2 = getPosition(p, i+1, img);
            int val2 = (img.getRGB(p2.x, p2.y)>> 16) & 0xFF;
            if(val1==255 && val2==0){
                count++;
            }
        }
        Point p1 = getPosition(p, 9, img);
        int val1 = (img.getRGB(p1.x, p1.y)>> 16) & 0xFF;
            
        Point p2 = getPosition(p, 2, img);
        int val2 = (img.getRGB(p2.x, p2.y)>> 16) & 0xFF;
        if(val1==255 && val2==0){
            count++;
        }
        return count;
    }
    
    private static int functionB(Point p, BufferedImage img){
        int result=0;
        for (int i = 2; i<=9; i++) {
            Point p1 = getPosition(p, i, img);
            int val = (img.getRGB(p1.x, p1.y)>> 16) & 0xFF;
            if(val==0){
                result += 1;
            }
        }
        return result;
    }
    
    private static boolean checkAtLeast1PositionIsWhite(Point p, int p1, int p2, int p3, BufferedImage img) {
        Point p1Point=getPosition(p, p1, img); 
        Point p2Point=getPosition(p, p2, img); 
        Point p3Point=getPosition(p, p3, img); 
        
        int p1Val=(img.getRGB(p1Point.x, p1Point.y)>> 16) & 0xFF;
        int p2Val=(img.getRGB(p2Point.x, p2Point.y)>> 16) & 0xFF;
        int p3Val=(img.getRGB(p3Point.x, p3Point.y)>> 16) & 0xFF;
        
        if(p1Val==255){
            return true;
        }
        if(p2Val==255){
            return true;
        }
        return p3Val==255;
    }
    
    private static Point getPosition(Point p, int position, BufferedImage img) {       
        if (position==2){
            return (new Point (p.x, p.y-1));
        } else if (position==3){
            return (new Point (p.x+1, p.y-1));
        }else if (position==4) {
            return (new Point (p.x+1, p.y));
        }else if (position==5) {
            return (new Point (p.x+1, p.y+1));
        }else if (position==6) {
            return (new Point (p.x, p.y+1));
        }else if (position==7) {
            return (new Point (p.x-1, p.y+1));
        }else if (position==8) {
            return (new Point (p.x-1, p.y));
        }else if (position==9) {
            return (new Point (p.x-1, p.y-1));
        }else {
            return null;
        }
    }
}
