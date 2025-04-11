import java.util.InputMismatchException;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Masukkan alamat absolut gambar input: ");
        String inputPath = scanner.nextLine();
        ImageProcessor processor = new ImageProcessor();
        while (!processor.loadImage(inputPath)) {
            System.out.println("Alamat tidak dapat dibaca.");
            System.out.print("Masukkan alamat absolut gambar input: ");
            inputPath = scanner.nextLine();
        }

        System.out.println("Pilih metode pengukuran error:");
        System.out.println("1. Variance");
        System.out.println("2. Mean Absolute Deviation (MAD)");
        System.out.println("3. Max Pixel Difference");
        System.out.println("4. Entropy");
        int methodChoice = 0;
        boolean validMethodChoiceInput = false;
        while (!validMethodChoiceInput){
            System.out.print("Masukkan pilihan (1-4): ");
            try {
                methodChoice = scanner.nextInt();
                if (methodChoice >= 1 && methodChoice <= 4) {
                    validMethodChoiceInput = true;
                } else {
                    System.out.println("Input invalid, input harus berupa integer pada range 1 sampai 4.");
                }
            } catch(InputMismatchException e) {
                System.out.println("Input invalid, input harus berupa integer pada range 1 sampai 4.");
                scanner.next();
            }
        }

        boolean validThresholdInput = false;
        double threshold = 0;
        while (!validThresholdInput) {
            System.out.print("Masukkan threshold error: ");
            try {
                threshold = scanner.nextDouble();
                if (threshold >= 0) {
                    validThresholdInput = true;
                } else {
                    System.out.println("Threshold tidak boleh negatif.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Masukkan nilai double yang valid.");
                scanner.next();
            }
        }

        boolean validMinBlockSizeInput = false;
        int minBlockSize = 0;
        while (!validMinBlockSizeInput){
            System.out.print("Masukkan ukuran blok minimum: ");
            try {
                minBlockSize = scanner.nextInt();
                if (minBlockSize > 0) {
                    validMinBlockSizeInput = true;
                } else {
                    System.out.println("Input harus lebih dari nol dan berupa integer yang valid.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Input harus berupa integer yang valid.");
                scanner.next();
            }
        }

        scanner.nextLine();

        System.out.print("Masukkan alamat absolut untuk gambar output: ");
        String outputPath = scanner.nextLine();
        String tempGifName = new File(outputPath).getName();
        String gifName = tempGifName.contains(".") ? tempGifName.substring(0, tempGifName.lastIndexOf('.')) : tempGifName;

        System.out.print("Ingin menyimpan GIF proses kompresi? (y/n): ");
        String gifChoice = scanner.nextLine().trim().toLowerCase();
        boolean generateGIF = gifChoice.equals("y");
        
        Quadtree qt = new Quadtree();
        long startTime = System.nanoTime();
        Quadtree.ErrorMethod errorMethod = Quadtree.ErrorMethod.values()[methodChoice - 1];
        
        qt.build(processor.getInputImage(), 0, 0, processor.getInputImage().getWidth(),
        processor.getInputImage().getHeight(), threshold, minBlockSize,
        errorMethod, processor, generateGIF);
        
        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1e9;
        
        BufferedImage newOutput = new BufferedImage(processor.getInputImage().getWidth(),
        processor.getInputImage().getHeight(),
        processor.getInputImage().getType());
        processor.setOutputImage(newOutput);
        
        qt.reconstruct(processor.getOutputImage(), qt.getRoot());
        
        if (!processor.saveImage(outputPath)) {
            System.out.println("Gagal menyimpan gambar output.");
            scanner.close();
            return;
        }
        
        if (gifChoice.equals("y")) {
            GifGenerator.generateGif("/test/frames", "/test/" + gifName + ".gif", 500);
        }
        
        long originalSize = processor.getFileSize(inputPath);
        long compressedSize = processor.getFileSize(outputPath);
        double compressionPercentage = 0.0;
        if (originalSize > 0)
        compressionPercentage = (1.0 - (double) compressedSize / originalSize) * 100.0;
        
        
        System.out.println("\n--- Statistik Kompresi ---");
        System.out.println("Waktu eksekusi: " + executionTime + " detik");
        System.out.println("Ukuran gambar sebelum: " + originalSize + " bytes");
        System.out.println("Ukuran gambar sesudah: " + compressedSize + " bytes");
        System.out.println("Persentase kompresi: " + compressionPercentage + " %");
        System.out.println("Kedalaman pohon: " + qt.getMaxDepth());
        System.out.println("Banyak simpul: " + qt.getNodeCount());
        
        scanner.close();
    }
}