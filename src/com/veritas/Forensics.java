package com.veritas;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Forensics {

    // -------------------------------
    // Step 1: Count frames in folder
    // -------------------------------
    public static int extractFrames(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            System.out.println("Frames folder not found!");
            return 0;
        }

        File[] files = folder.listFiles();
        return files != null ? files.length : 0;
    }

    // -------------------------------
    // Step 2: Analyze image (simple detection)
    // -------------------------------
    public static double analyzeImage(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));

            double total = 0;
            int width = img.getWidth();
            int height = img.getHeight();

            for (int i = 0; i < width - 1; i++) {
                for (int j = 0; j < height - 1; j++) {
                    int rgb1 = img.getRGB(i, j);
                    int rgb2 = img.getRGB(i + 1, j);

                    int r1 = (rgb1 >> 16) & 255;
                    int r2 = (rgb2 >> 16) & 255;

                    total += Math.abs(r1 - r2);
                }
            }

            return total / (width * height);

        } catch (IOException e) {
            return 0;
        }
    }

    // -------------------------------
    // Step 3: Detect deepfake
    // -------------------------------
    public static String detectDeepfake() {
        int frames = extractFrames("frames");
        ArrayList<Integer> suspicious = new ArrayList<>();

        for (int i = 0; i < frames; i++) {
            String path = "frames/frame_" + i + ".jpg";

            double score = analyzeImage(path);

            if (score > 20) {  // threshold
                suspicious.add(i);
            }
        }

        if (suspicious.isEmpty()) {
            return "No major artifacts detected -> Likely real";
        }

        return "Artifacts found in frames " +
                suspicious.subList(0, Math.min(5, suspicious.size())) +
                " -> High probability of AI manipulation";
    }

    // -------------------------------
    // Step 4: Tool method (IMPORTANT)
    // -------------------------------
    public static String deepfakeTool() {
        return detectDeepfake();
    }

    // -------------------------------
    // Step 5: Main method (ONLY ONE)
    // -------------------------------
    public static void main(String[] args) {
        System.out.println(deepfakeTool());
    }
}