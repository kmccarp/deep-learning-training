import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Kevin on 2/1/2017.
 */
public class ImageGenerator {
    private static final int IMAGE_WIDTH = 255;
    private static final int IMAGE_HEIGHT = 255;
    public static final double LINE_STEP = 0.01;
    public static final int FILES_TO_GENERATE = 10000;

    public static void generateImage(int numHexagons, Path outputPath) throws Exception {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

        int solidColor = randomColor();
        fillImage(image, solidColor);
        for (int i = 0; i < numHexagons; i++) {
            int shapeColor = randomColor();
            while (Math.abs(shapeColor - solidColor) <= 100) {
                shapeColor = randomColor();
            }
            generateHexagon(image, shapeColor);
        }


        ImageIO.write(image, "png", outputPath.toFile());
    }

    private static void generateHexagon(BufferedImage image, int shapeColor) throws Exception {
        // draw shape
        int lineLength = randInt(50, 50);

        // in a right triangle with two equal-length sides, and then a lineWidth hypotenuse,
        //  lineWidth is c, so solve for a and b (or in this case, a)
        int calculatedDistance = (int) Math.sqrt(Math.pow(lineLength, 2) / 2);

        int zeroX = randInt(lineLength + calculatedDistance, IMAGE_WIDTH - lineLength - calculatedDistance);
        int zeroY = randInt(lineLength + calculatedDistance, IMAGE_HEIGHT - lineLength - calculatedDistance);
        drawShape(image, shapeColor, lineLength, calculatedDistance, zeroX, zeroY);

        // fill image
        fill(image, zeroX + 1, zeroY + 1, shapeColor);
    }

    private static void drawShape(BufferedImage image, int shapeColor, int lineLength, int calculatedDistance, int zeroX, int zeroY) {
        // initialize hex points
        int[][] hexPoints = new int[][]{
                new int[]{zeroX, zeroY}, // top left
                new int[]{zeroX + lineLength, zeroY}, // top right
                new int[]{zeroX + lineLength + calculatedDistance, zeroY + calculatedDistance}, // right
                new int[]{zeroX + lineLength, zeroY + calculatedDistance * 2}, // bottom right
                new int[]{zeroX, zeroY + calculatedDistance * 2}, // bottom left
                new int[]{zeroX - calculatedDistance, zeroY + calculatedDistance} // left
        };

        for (int i = 0; i < hexPoints.length; i++) {
            int[] hexPointA = hexPoints[i];
            int[] hexPointB = hexPoints[(i + 1) % hexPoints.length];

            // draw line from hexPointA to hexPointB
            double x = hexPointA[0];
            double y = hexPointA[1];
            while (Math.abs(hexPointB[0] - x) > LINE_STEP || Math.abs(hexPointB[1] - y) > LINE_STEP) {
                image.setRGB((int) Math.round(x), (int) Math.round(y), shapeColor);
                x += (hexPointB[0] - hexPointA[0]) / (lineLength * 2.0);
                y += (hexPointB[1] - hexPointA[1]) / (lineLength * 2.0);
            }
        }
    }

    private static void fillImage(BufferedImage image, int solidColor) {
        for (int x = 0; x < IMAGE_WIDTH; x++) {
            for (int y = 0; y < IMAGE_HEIGHT; y++) {
                image.setRGB(x, y, solidColor);
            }
        }
    }

    private static int randomColor() {
        return rgbPixel(0, randInt(0, 255), randInt(0, 255), randInt(0, 255));
    }

    public static int rgbPixel(int a, int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    public static void fill(BufferedImage image, int x, int y, int color) throws Exception {
        // BFS from this pixel grabbing every pixel, then filling them
        Queue<Pixel> queue = new LinkedList<>();
        enqueue(queue, new Pixel(x, y));
        int startingColor = image.getRGB(x, y);

        boolean[][] visited = new boolean[IMAGE_WIDTH][IMAGE_HEIGHT];
        for (int i = 0; i < visited.length; i++) {
            for (int j = 0; j < visited[i].length; j++) {
                visited[i][j] = false;
            }
        }

        while (!queue.isEmpty()) {
            // poll front of queue
            Pixel top = queue.poll();
            if (visited[top.x][top.y])
                continue; // TODO why do we need this line?

            // set color to new color, mark visited
            image.setRGB(top.x, top.y, color);
            visited[top.x][top.y] = true;

            // enqueue north neighbor, west neighbor, south neighbor, and east neighbor
            Collection<Pixel> pixelsToAdd = new LinkedList<>();
            if (top.y > 0) pixelsToAdd.add(new Pixel(top.x, top.y - 1)); // north
            if (top.x < IMAGE_WIDTH - 1) pixelsToAdd.add(new Pixel(top.x + 1, top.y)); // east
            if (top.y < IMAGE_HEIGHT - 1) pixelsToAdd.add(new Pixel(top.x, top.y + 1)); // south
            if (top.x > 0) pixelsToAdd.add(new Pixel(top.x - 1, top.y)); // west
            for (Pixel newPixel : pixelsToAdd) {
                if (!visited[newPixel.x][newPixel.y] && image.getRGB(newPixel.x, newPixel.y) == startingColor) {
                    enqueue(queue, newPixel);
                }
            }
        }
    }

    private static boolean enqueue(Queue<Pixel> queue, Pixel e) {
        return queue.add(e);
    }

    private static class Pixel {
        Pixel(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private int x;
        private int y;
    }

    private static int randInt(int min, int max) {
        return (int) Math.floor(Math.random() * (max - min) + min);
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < FILES_TO_GENERATE; i++) {
            int numHexagons = (int) Math.round(Math.random() * 3);
            String pathEnd = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
            String filePath = "./images/" + numHexagons + "/generatedImage" + pathEnd + ".png";
            File imageFilePath = new File(filePath);
            imageFilePath.mkdirs();
            generateImage(numHexagons, imageFilePath.toPath());
        }
    }
}
