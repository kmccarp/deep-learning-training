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

    public static void generateImage(Path outputPath) throws Exception {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

        int solidColor = randomColor();
        int shapeColor = randomColor();
        while (Math.abs(shapeColor - solidColor) <= 100) {
            shapeColor = randomColor();
        }
        fillImage(image, solidColor);

        // draw shape
        int lineLength = randInt(25, 25);

        // in a right triangle with two equal-length sides, and then a lineWidth hypotenuse,
        //  lineWidth is c, so solve for a and b (or in this case, a)
        int calculatedDistance = (int) Math.sqrt(Math.pow(lineLength, 2) / 2);

        int zeroX = randInt(lineLength + calculatedDistance, IMAGE_WIDTH - lineLength - calculatedDistance);
        int zeroY = randInt(lineLength + calculatedDistance, IMAGE_HEIGHT - lineLength - calculatedDistance);

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
            System.out.println("Drawing line between points " + i + " and " + (i + 1));

            // draw line from hexPointA to hexPointB
            double x = hexPointA[0];
            double y = hexPointA[1];
            while (Math.abs(hexPointB[0] - x) > LINE_STEP || Math.abs(hexPointB[1] - y) > LINE_STEP) {
                image.setRGB((int) Math.round(x), (int) Math.round(y), shapeColor);
                x += (hexPointB[0] - hexPointA[0]) / (lineLength * 2.0);
                y += (hexPointB[1] - hexPointA[1]) / (lineLength * 2.0);
            }
            System.out.println("Moving to point " + (i + 1));
        }

        // fill image
        fill(image, zeroX + 1, zeroY + 1, shapeColor);

        ImageIO.write(image, "png", outputPath.toFile());
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

        if (startingColor == color) {
            throw new IllegalArgumentException("Oh noes");
        }

        HashSet<Pixel> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            System.out.println("Queue has " + queue.size() + " elements in it");
            Pixel top = queue.poll();
            int oldColor = image.getRGB(top.x, top.y);
            image.setRGB(top.x, top.y, color);
            int newColor = image.getRGB(top.x, top.y);
            if (oldColor == newColor) {
                throw new IllegalArgumentException(
                        "Pixel (" + top.x + "," + top.y + "), which has " + (visited.contains(top) ? "" : " not") + "been seen,"
                                + " failed its color check.");
            }
            visited.add(new Pixel(top.x, top.y));

            // add all pixels nearby
            Collection<Pixel> e = new ArrayList<>();
            if (top.y > 0) {
                if (top.x > 0 && top.y > 0) e.add(new Pixel(top.x - 1, top.y - 1));
                if (top.y > 0) e.add(new Pixel(top.x, top.y - 1));
                if (top.x < IMAGE_WIDTH - 1) e.add(new Pixel(top.x + 1, top.y - 1));
            }
            if (top.x > 0) e.add(new Pixel(top.x - 1, top.y));
            if (top.x < IMAGE_WIDTH - 1) e.add(new Pixel(top.x + 1, top.y));
            if (top.y < IMAGE_HEIGHT - 1) {
                if (top.x > 0) e.add(new Pixel(top.x - 1, top.y + 1));
                e.add(new Pixel(top.x, top.y + 1));
                if (top.x < IMAGE_WIDTH - 1) e.add(new Pixel(top.x + 1, top.y + 1));
            }
            for (Pixel pixel : e) {
                // TODO there seems to be a bug at this line but i can't figure out why
                if (!visited.contains(pixel) && image.getRGB(pixel.x, pixel.y) == startingColor) {
                    enqueue(queue, pixel);
                }
            }
        }
    }

    private static boolean enqueue(Queue<Pixel> queue, Pixel e) {
//        System.out.println("Adding pixel (" + e.x + "," + e.y + ")");
        return queue.add(e);
    }

    private static class Pixel {
        public Pixel(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pixel pixel = (Pixel) o;

            if (x != pixel.x) return false;
            return y == pixel.y;

        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }

        private int x;
        private int y;
    }

    private static int randInt(int min, int max) {
        int retVal = (int) Math.floor(Math.random() * (max - min) + min);
        System.out.println("Between " + min + " and " + max + ", we got " + retVal);
        return retVal;
    }

    public static void main(String[] args) throws Exception {
        String pathEnd = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
//        File imageFilePath = new File("./images/generatedImage" + pathEnd + ".png");
        File imageFilePath = new File("./images/generatedImage.png");
        imageFilePath.mkdirs();
        generateImage(imageFilePath.toPath());
    }
}
