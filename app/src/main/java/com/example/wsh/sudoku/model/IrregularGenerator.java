package com.example.wsh.sudoku.model;


import com.example.wsh.sudoku.db.IrregularData;
import com.example.wsh.sudoku.util.MyPoint;
import com.example.wsh.sudoku.util.Util;

import java.util.ArrayList;
import java.util.Random;



public class IrregularGenerator {
    private static Random random = new Random();
    private int mSize;

    public IrregularGenerator(int size) {
        mSize = size;
    }

    private int nextIndex(int[][] a, int x, int y) {
        Random random = new Random();

        ArrayList<Integer> strategyArray = new ArrayList<>(4);
        strategyArray.add(0);
        strategyArray.add(1);
        strategyArray.add(2);
        strategyArray.add(3);

        int k = 4;
        while(k > 0) {
            switch (strategyArray.remove(random.nextInt(k--))) {
                case 0:
                    if (x > 0 && x < mSize && a[x-1][y] == 0) {
                        return (x-1) * mSize + y;
                    }
                    break;
                case 1:
                    if (x >= 0 && x < mSize-1 && a[x+1][y] == 0) {
                        return (x+1) * mSize + y;
                    }
                    break;
                case 2:
                    if (y > 0 && y < mSize && a[x][y-1] == 0) {
                        return x * mSize + y-1;
                    }
                    break;
                case 3:
                    if (y >= 0 && y < mSize-1 && a[x][y+1] == 0) {
                        return x * mSize + y+1;
                    }
                    break;
            }
        }

        return -1;
    }

    private int extend(int[][] a, int x, int y, int value) {
        ArrayList<MyPoint> points = new ArrayList<>(mSize);

        a[x][y] = value;
        MyPoint myPoint1 = new MyPoint(x, y);
        points.add(myPoint1);

        int count = 1;
        for (;;) {
            if (points.isEmpty()) {
                return count;
            }

            int index = random.nextInt(points.size());
            myPoint1 = points.get(index);
            int next = nextIndex(a, myPoint1.x, myPoint1.y);
            if (next == -1) {
                points.remove(index);
                continue;
            }

            int row = next / mSize;
            int column = next % mSize;
            a[row][column] = value;
            myPoint1 = new MyPoint(row, column);
            points.add(myPoint1);
            if (++count >= mSize) {
                return count;
            }
        }
    }

    private boolean fill (int a[][], int key) {
        if (key > mSize) {
            return true;
        }

        int x = -1;
        int y = -1;

        label:
        for (int i = 0; i < mSize + mSize - 1; i++) {
            for (int j = 0; j <= i; j++) {
                if (j < mSize && i-j < mSize && a[j][i-j] == 0) {
                    x = j;
                    y = i-j;
                    break label;
                }
            }
        }

        if (extend(a, x, y, key) == mSize) {
            return fill(a, key+1);
        }

        return false;
    }

    public String produceIrregular() {
        int[][] a = new int[mSize][mSize];

        int count = 1;
        for (;;) {
            Util.initArray(a);

            if (fill(a, 1)) {
                System.out.println("produceIrregular(), count = " + count);
                return Util.intArray2String(a);
            }
            count++;
        }
    }

    public static String randomGetIrregular(int size) {
        switch (size) {
            case SudokuGenerator.SIZE_FOUR:
                return IrregularData.four[random.nextInt(IrregularData.four.length)];
            case SudokuGenerator.SIZE_FIVE:
                return IrregularData.five[random.nextInt(IrregularData.five.length)];
            case SudokuGenerator.SIZE_SIX:
                return IrregularData.six[random.nextInt(IrregularData.six.length)];
            case SudokuGenerator.SIZE_SEVEN:
                return IrregularData.seven[random.nextInt(IrregularData.seven.length)];
            case SudokuGenerator.SIZE_EIGHT:
                return IrregularData.eight[random.nextInt(IrregularData.eight.length)];
            case SudokuGenerator.SIZE_NINE:
                return IrregularData.nine[random.nextInt(IrregularData.nine.length)];
            default:
                return null;
        }
    }

    public static String getIrregular(int size, int index) {
        switch (size) {
            case SudokuGenerator.SIZE_FOUR:
                index = index % IrregularData.four.length;
                return IrregularData.four[index];
            case SudokuGenerator.SIZE_FIVE:
                index = index % IrregularData.five.length;
                return IrregularData.five[index];
            case SudokuGenerator.SIZE_SIX:
                index = index % IrregularData.six.length;
                return IrregularData.six[index];
            case SudokuGenerator.SIZE_SEVEN:
                index = index % IrregularData.seven.length;
                return IrregularData.seven[index];
            case SudokuGenerator.SIZE_EIGHT:
                index = index % IrregularData.eight.length;
                return IrregularData.eight[index];
            case SudokuGenerator.SIZE_NINE:
                index = index % IrregularData.nine.length;
                return IrregularData.nine[index];
            default:
                return null;
        }
    }

    public static void main(String[] args) {
        new IrregularGenerator(6).produceIrregular();
    }
}
