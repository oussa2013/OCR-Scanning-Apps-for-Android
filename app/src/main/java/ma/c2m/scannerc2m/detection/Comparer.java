package ma.c2m.scannerc2m.detection;

import android.graphics.Color;

public class Comparer {
    private State state1 = null;
    private State state2 = null;
    private int xBoxes;
    private int yBoxes;
    private int xPixelsPerBox;
    private int yPixelsPerBox;
    private int xLeftOver;
    private int yLeftOver;
    private int rowWidthInPix;
    private int colWidthInPix;
    private int leniency;
    private int debugMode;
    private int[][] variance = null;
    private boolean different = false;

    public Comparer(State s1, State s2, int xBoxes, int yBoxes, int leniency, int debug) {
        this.state1 = s1;
        this.state2 = s2;
        this.xBoxes = xBoxes;
        if (this.xBoxes > s1.getWidth()) this.xBoxes = s1.getWidth();
        this.yBoxes = yBoxes;
        if (this.yBoxes > s1.getHeight()) this.yBoxes = s1.getHeight();
        this.leniency = leniency;
        this.debugMode = 0;
        this.xPixelsPerBox = (int) (Math.floor(s1.getWidth() / this.xBoxes));
        if (xPixelsPerBox <= 0) xPixelsPerBox = 1;
        this.yPixelsPerBox = (int) (Math.floor(s1.getHeight() / this.yBoxes));
        if (yPixelsPerBox <= 0) yPixelsPerBox = 1;
        this.xLeftOver = s1.getWidth() - (this.xBoxes * this.xPixelsPerBox);
        if (xLeftOver > 0) this.xBoxes++;
        yLeftOver = s1.getHeight() - (this.yBoxes * this.yPixelsPerBox);
        if (yLeftOver > 0) this.yBoxes++;
        this.rowWidthInPix = (this.xBoxes * xPixelsPerBox) - (xPixelsPerBox - xLeftOver);
        this.colWidthInPix = this.xPixelsPerBox;
        this.debugMode = debug;
        this.different = isDifferent(this.state1, this.state2);
    }

    public boolean isDifferent(State s1, State s2) {
        if (s1 == null || s2 == null) throw new NullPointerException();
        if (s1.getWidth() != s2.getWidth() || s1.getHeight() != s2.getHeight()) return true;
        this.variance = new int[yBoxes][xBoxes];
        boolean different = false;
        int b1 = 0;
        int b2 = 0;
        int diff = 0;
        for (int y = 0; y < yBoxes; y++) {
            for (int x = 0; x < xBoxes; x++) {
                b1 = aggregateMapArea(state1.getMap(), x, y);
                b2 = aggregateMapArea(state2.getMap(), x, y);
                diff = Math.abs(b1 - b2);
                variance[y][x] = diff;
                if (diff > leniency) different = true;
            }
        }
        different = true;
        return different;
    }

    private int aggregateMapArea(int[] map, int xBox, int yBox) {
        if (map == null) throw new NullPointerException();
        int yPix = yPixelsPerBox;
        int xPix = xPixelsPerBox;
        if (yBox == (yBoxes - 1) && yLeftOver > 0) yPix = yLeftOver;
        if (xBox == (xBoxes - 1) && xLeftOver > 0) xPix = xLeftOver;
        int rowOffset = (yBox * yPixelsPerBox * rowWidthInPix);
        int columnOffset = (xBox * colWidthInPix);
        int i = 0;
        int iy = 0;
        for (int y = 0; y < yPix; y++) {
            iy = (y * (xBoxes * xPixelsPerBox)) - (y * (xPixelsPerBox - xLeftOver));
            for (int x = 0; x < xPix; x++) {
                i += map[rowOffset + columnOffset + iy + x];
            }
        }
        return (i / (xPix * yPix));
    }

    public void paintDifferences(int[] data) {
        if (data == null) throw new NullPointerException();
        for (int y = 0; y < yBoxes; y++) {
            for (int x = 0; x < xBoxes; x++) {
                if (variance[y][x] > leniency) paint(data, x, y, Color.RED);
            }
        }
    }

    private void paint(int[] data, int xBox, int yBox, int color) {
        if (data == null) throw new NullPointerException();
        int yPix = yPixelsPerBox;
        int xPix = xPixelsPerBox;
        if (yBox == (yBoxes - 1) && yLeftOver > 0) yPix = yLeftOver;
        if (xBox == (xBoxes - 1) && xLeftOver > 0) xPix = xLeftOver;
        int rowOffset = (yBox * yPixelsPerBox * rowWidthInPix);
        int columnOffset = (xBox * colWidthInPix);
        int iy = 0;
        for (int y = 0; y < yPix; y++) {
            iy = y * (xBoxes * xPixelsPerBox) - (y * (xPixelsPerBox - xLeftOver));
            for (int x = 0; x < xPix; x++) {
                if (y == 0 || y == (yPix - 1) || x == 0 || x == (xPix - 1))
                    data[rowOffset + columnOffset + iy + x] = color;
            }
        }
    }

    public int getCompareX() {
        return xBoxes;
    }

    public int getCompareY() {
        return yBoxes;
    }

    public int getLeniency() {
        return leniency;
    }

    public int getDebugMode() {
        return debugMode;
    }

    public boolean isDifferent() {
        return different;
    }

    @Override
    public String toString() {
        int diff = 0;
        StringBuilder output = new StringBuilder();
        for (int y = 0; y < yBoxes; y++) {
            output.append('|');
            for (int x = 0; x < xBoxes; x++) {
                diff = variance[y][x];
                if (debugMode == 1) output.append((diff > leniency) ? 'X' : ' ');
                if (debugMode == 2) output.append(diff + ((x < (xBoxes - 1)) ? "," : ""));
            }
            output.append("|\n");
        }
        return output.toString();
    }
}