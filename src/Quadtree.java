import java.awt.image.BufferedImage;
import java.awt.Color;
import java.lang.Math;

public class Quadtree {
    private QuadtreeNode root;
    private int nodeCount;
    private int maxDepth;
    
    public enum ErrorMethod {
        VARIANCE, MAD, MAX_DIFF, ENTROPY
    }

    public Quadtree() {
        root = null;
        nodeCount = 0;
        maxDepth = 0;
    }

    public QuadtreeNode getRoot() {
        return root;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void build(BufferedImage image, int x, int y, int width, int height,
                  double threshold, int minSize, ErrorMethod method,
                  ImageProcessor processor, boolean generateGIF) {
        nodeCount = 0;
        maxDepth = 0;

        root = buildRecursive(image, x, y, width, height, threshold, minSize, method, 1);

        if (generateGIF) {
            for (int d = 1; d <= maxDepth; d++) {
                BufferedImage stepImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
                processor.setOutputImage(stepImage);
                reconstructUntilDepth(stepImage, root, d, 1);

                String filename = String.format("../test/frames/depth_%02d.png", d);
                boolean saved = processor.saveImageFrame(filename, stepImage);
                if (!saved) {
                    System.out.println("Gagal menyimpan frame untuk depth " + d);
                }
            }
        }
    }


    public void reconstructUntilDepth(BufferedImage output, QuadtreeNode node, int maxAllowedDepth, int currentDepth) {
        if (node == null) return;
    
        if (node.isLeaf() || currentDepth >= maxAllowedDepth) {
            Color fill = new Color(node.getAvgRed(), node.getAvgGreen(), node.getAvgBlue());
            for (int i = node.getY(); i < node.getY() + node.getHeight(); i++) {
                for (int j = node.getX(); j < node.getX() + node.getWidth(); j++) {
                    output.setRGB(j, i, fill.getRGB());
                }
            }
        } else {
            for (QuadtreeNode child : node.getChildren()) {
                reconstructUntilDepth(output, child, maxAllowedDepth, currentDepth + 1);
            }
        }
    }

    private QuadtreeNode buildRecursive(BufferedImage image, int x, int y, int width, int height,
                                    double threshold, int minSize, ErrorMethod method, int currentDepth) {
    nodeCount++;
    if (currentDepth > maxDepth) maxDepth = currentDepth;

    double[] avg = new double[3];
    double error = computeError(image, x, y, width, height, method, avg);

    QuadtreeNode node = new QuadtreeNode(x, y, width, height);
    node.setAvgRed((int) avg[0]);
    node.setAvgGreen((int) avg[1]);
    node.setAvgBlue((int) avg[2]);

    int currentArea = width * height;
    int childArea = (width / 2) * (height / 2);

    if (error > threshold && currentArea > minSize && childArea >= minSize) {
        node.setLeaf(false);
        int halfW = width / 2;
        int halfH = height / 2;

        QuadtreeNode[] children = new QuadtreeNode[4];
        children[0] = buildRecursive(image, x, y, halfW, halfH, threshold, minSize, method, currentDepth + 1);
        children[1] = buildRecursive(image, x + halfW, y, width - halfW, halfH, threshold, minSize, method, currentDepth + 1);
        children[2] = buildRecursive(image, x, y + halfH, halfW, height - halfH, threshold, minSize, method, currentDepth + 1);
        children[3] = buildRecursive(image, x + halfW, y + halfH, width - halfW, height - halfH, threshold, minSize, method, currentDepth + 1);

        node.setChildren(children);
    }

    return node;
}


    private double computeError(BufferedImage image, int x, int y, int width, int height,
                                ErrorMethod method, double[] avg) {
        int count = width * height;
        double sumR = 0, sumG = 0, sumB = 0;

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                Color color = new Color(image.getRGB(j, i));
                sumR += color.getRed();
                sumG += color.getGreen();
                sumB += color.getBlue();
            }
        }
        avg[0] = sumR / count;
        avg[1] = sumG / count;
        avg[2] = sumB / count;

        double error = 0.0;
        switch (method) {
            case VARIANCE:
                double varR = 0, varG = 0, varB = 0;
                for (int i = y; i < y + height; i++) {
                    for (int j = x; j < x + width; j++) {
                        Color color = new Color(image.getRGB(j, i));
                        varR += Math.pow(color.getRed() - avg[0], 2);
                        varG += Math.pow(color.getGreen() - avg[1], 2);
                        varB += Math.pow(color.getBlue() - avg[2], 2);
                    }
                }
                error = (varR + varG + varB) / (3 * count);
                break;

            case MAD:
                double madR = 0, madG = 0, madB = 0;
                for (int i = y; i < y + height; i++) {
                    for (int j = x; j < x + width; j++) {
                        Color color = new Color(image.getRGB(j, i));
                        madR += Math.abs(color.getRed() - avg[0]);
                        madG += Math.abs(color.getGreen() - avg[1]);
                        madB += Math.abs(color.getBlue() - avg[2]);
                    }
                }
                error = (madR + madG + madB) / (3 * count);
                break;

            case MAX_DIFF:
                int minR = 255, minG = 255, minB = 255;
                int maxR = 0, maxG = 0, maxB = 0;
                for (int i = y; i < y + height; i++) {
                    for (int j = x; j < x + width; j++) {
                        Color color = new Color(image.getRGB(j, i));
                        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
                        if (r < minR) minR = r;
                        if (g < minG) minG = g;
                        if (b < minB) minB = b;
                        if (r > maxR) maxR = r;
                        if (g > maxG) maxG = g;
                        if (b > maxB) maxB = b;
                    }
                }
                error = ((maxR - minR) + (maxG - minG) + (maxB - minB)) / 3.0;
                break;

            case ENTROPY:
                double entropySum = 0.0;
                for (int c = 0; c < 3; c++) {
                    int[] hist = new int[256];
                    for (int i = y; i < y + height; i++) {
                        for (int j = x; j < x + width; j++) {
                            Color color = new Color(image.getRGB(j, i));
                            int value = (c == 0) ? color.getRed() : (c == 1 ? color.getGreen() : color.getBlue());
                            hist[value]++;
                        }
                    }
                    double entropy = 0.0;
                    for (int k = 0; k < 256; k++) {
                        if (hist[k] > 0) {
                            double p = (double) hist[k] / count;
                            entropy -= p * (Math.log(p) / Math.log(2));
                        }
                    }
                    entropySum += entropy;
                }
                error = entropySum / 3.0;
                break;
        }
        
        return error;
    }

    public void reconstruct(BufferedImage output, QuadtreeNode node) {
        if (node == null) return;
        if (node.isLeaf()) {
            Color fill = new Color(node.getAvgRed(), node.getAvgGreen(), node.getAvgBlue());
            for (int i = node.getY(); i < node.getY() + node.getHeight(); i++) {
                for (int j = node.getX(); j < node.getX() + node.getWidth(); j++) {
                    output.setRGB(j, i, fill.getRGB());
                }
            }
        } else {
            for (QuadtreeNode child : node.getChildren()) {
                reconstruct(output, child);
            }
        }
    }
}
