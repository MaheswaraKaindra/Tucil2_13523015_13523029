import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

public class GifGenerator {
    public static void generateGif(String framesDir, String outputGifPath, int delayMS) {
        try {
            File dir = new File(framesDir);
            File[] imageFiles = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith("depth_") && name.endsWith(".png");
                }
            });

            if (imageFiles == null || imageFiles.length == 0) {
                System.out.println("Tidak ada frame PNG ditemukan di " + framesDir);
                return;
            }

            Arrays.sort(imageFiles, Comparator.comparing(File::getName));

            BufferedImage firstImage = ImageIO.read(imageFiles[0]);
            ImageOutputStream output = new FileImageOutputStream(new File(outputGifPath));
            GifSequence writer = new GifSequence(output, firstImage.getType(), delayMS);

            writer.writeToSequence(firstImage);
            for (int i = 1; i < imageFiles.length; i++) {
                BufferedImage nextImage = ImageIO.read(imageFiles[i]);
                writer.writeToSequence(nextImage);
            }

            writer.close();
            output.close();

            System.out.println("GIF berhasil dibuat di: " + outputGifPath);

            for (File file : imageFiles) {
                if (!file.delete()) {
                    System.out.println("Gagal menghapus file: " + file.getName());
                }
            }
            System.out.println("Frame dihapus dari: " + framesDir);

        } catch (Exception e) {
            return;
        }
    }
}
