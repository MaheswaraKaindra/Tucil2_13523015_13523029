import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageProcessor {
    private BufferedImage inputImage;
    private BufferedImage outputImage;

    public ImageProcessor() {
        inputImage = null;
        outputImage = null;
    }

    public BufferedImage getInputImage() {
        return inputImage;
    }

    public BufferedImage getOutputImage() {
        return outputImage;
    }

    public void setInputImage(BufferedImage inputImage) {
        this.inputImage = inputImage;
    }

    public void setOutputImage(BufferedImage outputImage) {
        this.outputImage = outputImage;
    }

    public boolean loadImage(String path) {
        try {
            inputImage = ImageIO.read(new File(path));
            return inputImage != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean saveImage(String path) {
        try {
            String format = path.substring(path.lastIndexOf('.') + 1).toLowerCase();
            if (!format.matches("png|jpg|jpeg")) {
                format = "png";
            }
            return ImageIO.write(outputImage, format, new File(path));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean saveImageFrame(String path, BufferedImage image) {
        try {
            return ImageIO.write(image, "png", new File(path));
        } catch (Exception e){
            return false;
        }
    }

    public long getFileSize(String path) {
        File file = new File(path);
        return file.length();
    }
}
