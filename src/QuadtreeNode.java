public class QuadtreeNode {
    private int x, y, width, height;
    private int avgRed, avgGreen, avgBlue;
    private boolean isLeaf;
    private QuadtreeNode[] children;

    public QuadtreeNode(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isLeaf = true;
        this.children = new QuadtreeNode[4];
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setColor(int red, int green, int blue) {
        this.avgRed = red;
        this.avgGreen = green;
        this.avgBlue = blue;
    }

    public int getAvgRed() {
        return avgRed;
    }

    public void setAvgRed(int avgRed) {
        this.avgRed = avgRed;
    }

    public int getAvgGreen() {
        return avgGreen;
    }

    public void setAvgGreen(int avgGreen) {
        this.avgGreen = avgGreen;
    }

    public int getAvgBlue() {
        return avgBlue;
    }

    public void setAvgBlue(int avgBlue) {
        this.avgBlue = avgBlue;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public QuadtreeNode[] getChildren() {
        return children;
    }

    public void setChildren(QuadtreeNode[] children) {
        this.children = children;
    }
}