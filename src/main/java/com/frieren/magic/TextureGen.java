package com.frieren.magic;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TextureGen {
    public static void main(String[] args) {
        // Посохи
        generateStaffTexture("frieren_staff.png", new Color(230, 35, 60), new Color(255, 110, 130), new Color(160, 20, 40));
        generateStaffTexture("frieren_staff_flower.png", new Color(235, 120, 185), new Color(255, 200, 235), new Color(190, 80, 140));
        generateStaffTexture("frieren_staff_zoltraak.png", new Color(245, 180, 30), new Color(255, 250, 190), new Color(175, 115, 15));
        generateStaffTexture("frieren_staff_shield.png", new Color(50, 215, 235), new Color(180, 250, 255), new Color(20, 145, 170));
        generateStaffTexture("frieren_staff_heal.png", new Color(40, 210, 100), new Color(170, 255, 190), new Color(15, 140, 60));
        generateStaffTexture("frieren_staff_flight.png", new Color(30, 110, 240), new Color(170, 220, 255), new Color(15, 60, 170));
        generateStaffTexture("frieren_staff_dig.png", new Color(255, 140, 0), new Color(255, 200, 80), new Color(180, 80, 0)); // Копание

        // Набалдашники
        generateFocusTexture("flower_focus.png", new Color(235, 120, 185), new Color(255, 200, 235), new Color(190, 80, 140));
        generateFocusTexture("zoltraak_focus.png", new Color(250, 190, 35), new Color(255, 255, 210), new Color(180, 120, 15));
        generateFocusTexture("shield_focus.png", new Color(50, 215, 235), new Color(180, 250, 255), new Color(20, 145, 170));
        generateFocusTexture("heal_focus.png", new Color(40, 210, 100), new Color(170, 255, 190), new Color(15, 140, 60));
        generateFocusTexture("flight_focus.png", new Color(30, 110, 240), new Color(170, 220, 255), new Color(15, 60, 170));
        generateFocusTexture("dig_focus.png", new Color(255, 140, 0), new Color(255, 200, 80), new Color(180, 80, 0)); // Копание
    }

    private static void generateStaffTexture(String filename, Color ruby, Color rubyBright, Color rubyDark) {
        int width = 16, height = 16;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Color woodDark = new Color(78, 53, 36);
        Color wood = new Color(115, 75, 45);
        Color woodLight = new Color(148, 98, 60);
        Color goldDark = new Color(180, 130, 25);
        Color gold = new Color(235, 180, 50);
        Color goldLight = new Color(255, 220, 100);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 16; y++) {
                if ((x + y) % 3 == 0) img.setRGB(x, y, woodLight.getRGB());
                else if ((x + y) % 2 == 0) img.setRGB(x, y, wood.getRGB());
                else img.setRGB(x, y, woodDark.getRGB());
            }
        }
        for (int x = 4; x < 10; x++) {
            for (int y = 0; y < 16; y++) {
                if (y < 3) img.setRGB(x, y, goldLight.getRGB());
                else if (y < 10) img.setRGB(x, y, gold.getRGB());
                else img.setRGB(x, y, goldDark.getRGB());
            }
        }
        for (int x = 10; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (x > 11 && x < 14 && y > 4 && y < 9) {
                    img.setRGB(x, y, rubyBright.getRGB());
                } else if (y > 11) {
                    img.setRGB(x, y, rubyDark.getRGB());
                } else {
                    img.setRGB(x, y, ruby.getRGB());
                }
            }
        }
        saveImage(img, filename);
    }

    private static void generateFocusTexture(String filename, Color crystalMain, Color crystalGaze, Color crystalBorder) {
        int w = 16, h = 16;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Color goldDark = new Color(160, 115, 20);
        Color gold = new Color(230, 175, 45);
        Color goldLight = new Color(255, 225, 110);
        Color crystalCore = new Color(255, 255, 255);

        int[][] goldPixels = {
                {6, 3}, {7, 3}, {8, 3}, {9, 3},
                {4, 4}, {5, 4}, {10, 4}, {11, 4},
                {3, 6}, {3, 7}, {3, 8}, {3, 9},
                {12, 6}, {12, 7}, {12, 8}, {12, 9},
                {4, 11}, {5, 11}, {10, 11}, {11, 11},
                {6, 12}, {7, 12}, {8, 12}, {9, 12}
        };

        for (int[] p : goldPixels) {
            img.setRGB(p[0], p[1], gold.getRGB());
        }

        img.setRGB(6, 3, goldLight.getRGB());
        img.setRGB(7, 3, goldLight.getRGB());
        img.setRGB(4, 5, goldLight.getRGB());
        img.setRGB(11, 11, goldDark.getRGB());
        img.setRGB(9, 12, goldDark.getRGB());

        for (int x = 5; x <= 10; x++) {
            for (int y = 5; y <= 10; y++) {
                img.setRGB(x, y, crystalMain.getRGB());
            }
        }

        img.setRGB(5, 5, crystalBorder.getRGB());
        img.setRGB(10, 5, crystalBorder.getRGB());
        img.setRGB(5, 10, crystalBorder.getRGB());
        img.setRGB(10, 10, crystalBorder.getRGB());

        img.setRGB(7, 7, crystalCore.getRGB());
        img.setRGB(8, 7, crystalGaze.getRGB());
        img.setRGB(7, 8, crystalGaze.getRGB());
        img.setRGB(8, 8, crystalCore.getRGB());

        saveImage(img, filename);
    }

    private static void saveImage(BufferedImage img, String filename) {
        File outFile = new File("src/main/resources/assets/frierenmagic/textures/item/" + filename);
        outFile.getParentFile().mkdirs();
        try {
            ImageIO.write(img, "PNG", outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}