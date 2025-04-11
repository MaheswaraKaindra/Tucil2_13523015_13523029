import java.util.InputMismatchException;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println();
        System.out.println(color("╭────────────────────────────────────────────────────╮", "\u001B[34m"));
        System.out.println(color("│", "\u001B[34m") + color("       Welcome to, Quadtree Image Compressor        ", "\u001B[35m") + color("│", "\u001B[34m"));
        System.out.println(color("│", "\u001B[34m") + color("              A Project by Kaindra and Ho           ", "\u001B[36m") + color("│", "\u001B[34m"));
        System.out.println(color("╰────────────────────────────────────────────────────╯", "\u001B[34m"));
        System.out.println();


        System.out.print("Masukkan ");
        System.out.print(color("alamat absolut", "\u001B[36m"));
        System.out.print(" gambar: ");
        String inputPath = scanner.nextLine();
        ImageProcessor processor = new ImageProcessor();
        while (!processor.loadImage(inputPath)) {
            System.out.println(color("Alamat tidak dapat dibaca.", "\u001B[31m"));
            System.out.println();
            System.out.print("Masukkan ");
            System.out.print(color("alamat absolut", "\u001B[36m"));
            System.out.print(" gambar: ");
            inputPath = scanner.nextLine();
        }

        System.out.println(color("╭────────────────────────────────────────────────────╮", "\u001B[34m"));
        System.out.println(color("│", "\u001B[34m") + color("            Pilih Metode Pengukuran Error           ", "\u001B[35m") + color("│", "\u001B[34m"));
        System.out.println(color("╰────────────────────────────────────────────────────╯", "\u001B[34m"));

        System.out.println(color("   1. ", "\u001B[36m") + "Variance");
        System.out.println(color("   2. ", "\u001B[36m") + "Mean Absolute Deviation (MAD)");
        System.out.println(color("   3. ", "\u001B[36m") + "Max Pixel Difference");
        System.out.println(color("   4. ", "\u001B[36m") + "Entropy");
        int methodChoice = 0;
        boolean validMethodChoiceInput = false;
        while (!validMethodChoiceInput){
            System.out.println();
            System.out.print("Masukkan pilihan ");
            System.out.print(color("(1 sampai 4)", "\u001B[36m") + ": ");
            try {
                methodChoice = scanner.nextInt();
                if (methodChoice >= 1 && methodChoice <= 4) {
                    validMethodChoiceInput = true;
                } else {
                    System.out.println(color("Input invalid, input harus berupa integer pada range 1 sampai 4.", "\u001B[31m"));
                }
            } catch(InputMismatchException e) {
                System.out.println(color("Masukkan nilai integer yang valid.", "\u001B[31m"));
                scanner.next();
            }
        }

        boolean validThresholdInput = false;
        double threshold = 0;
        while (!validThresholdInput) {
            System.out.println();
            System.out.print("Masukkan ");
            System.out.print(color("threshold error", "\u001B[36m") + ": ");
            try {
                threshold = scanner.nextDouble();
                if (threshold >= 0) {
                    validThresholdInput = true;
                } else {
                    System.out.println(color("Threshold tidak boleh negatif.", "\u001B[31m"));
                }
            } catch (InputMismatchException e) {
                System.out.println(color("Masukkan nilai double yang valid.", "\u001B[31m"));
                scanner.next();
            }
        }

        boolean validMinBlockSizeInput = false;
        int minBlockSize = 0;
        while (!validMinBlockSizeInput){
            System.out.println();
            System.out.print("Masukkan ");
            System.out.print(color("ukuran blok minimum", "\u001B[31m") + ": ");
            try {
                minBlockSize = scanner.nextInt();
                if (minBlockSize > 0) {
                    validMinBlockSizeInput = true;
                } else {
                    System.out.println(color("Input harus lebih dari nol dan berupa integer yang valid.", "\u001B[36m"));
                }
            } catch (InputMismatchException e) {
                System.out.println(color("Input harus berupa integer yang valid.","\u001B[36m"));
                scanner.next();
            }
        }

        scanner.nextLine();

        System.out.println();
        System.out.print("Masukkan ");
        System.out.print(color("output path gambar", "\u001B[36m") + ": ");
        String outputPath = scanner.nextLine();
        String tempGifName = new File(outputPath).getName();
        String gifName = tempGifName.contains(".") ? tempGifName.substring(0, tempGifName.lastIndexOf('.')) : tempGifName;

        String gifChoice = "";
        boolean generateGIF = false;

        System.out.println();
        System.out.print("Ingin menyimpan GIF proses kompresi? ");
        System.out.print(color("(y/n)", "\u001B[36m") + ": ");
        gifChoice = scanner.nextLine().trim().toLowerCase();

        while (!gifChoice.equals("y") && !gifChoice.equals("n")) {
                System.out.println(color("Input invalid! Masukkan hanya 'y' atau 'n'.", "\u001B[31m"));
                System.out.println();
                System.out.print("Ingin menyimpan GIF proses kompresi? ");
                System.out.print(color("(y/n)", "\u001B[36m") + ": ");
                gifChoice = scanner.nextLine().trim().toLowerCase();
        }

generateGIF = gifChoice.equals("y");
        
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
            GifGenerator.generateGif("test/frames", "test/" + gifName + ".gif", 500);
        }
        
        long originalSize = processor.getFileSize(inputPath);
        long compressedSize = processor.getFileSize(outputPath);
        double compressionPercentage = 0.0;
        if (originalSize > 0)
        compressionPercentage = (1.0 - (double) compressedSize / originalSize) * 100.0;
        
        
        System.out.println();
        System.out.println(color("╭────────────────────────────────────────────────────╮", "\u001B[34m"));
        System.out.println(color("│", "\u001B[34m") + color("               Statistik Kompresi                   ", "\u001B[35m") + color("│", "\u001B[34m"));
        System.out.println(color("╰────────────────────────────────────────────────────╯", "\u001B[34m"));

        System.out.printf("%-28s : %s%n", "Waktu eksekusi", color(String.format("%.3f detik", executionTime), "\u001B[36m"));
        System.out.printf("%-28s : %s%n", "Ukuran gambar sebelum", color(originalSize + " bytes", "\u001B[36m"));
        System.out.printf("%-28s : %s%n", "Ukuran gambar sesudah", color(compressedSize + " bytes", "\u001B[36m"));
        System.out.printf("%-28s : %s%n", "Persentase kompresi", color(String.format("%.2f %%", compressionPercentage), "\u001B[36m"));
        System.out.printf("%-28s : %s%n", "Kedalaman pohon", color(String.valueOf(qt.getMaxDepth()), "\u001B[36m"));
        System.out.printf("%-28s : %s%n", "Banyak simpul", color(String.valueOf(qt.getNodeCount()), "\u001B[36m"));
        
        scanner.close();
    }

    public static String color(String text, String colorCode) {
        return colorCode + text + "\u001B[0m";
    }
}