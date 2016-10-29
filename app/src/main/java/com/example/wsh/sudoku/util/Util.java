package com.example.wsh.sudoku.util;


import android.content.Context;

import com.example.wsh.sudoku.App;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by wsh on 16-7-13.
 */
public final class Util {
    private Util(){}

    public static String intArray2String(int[][] array) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                builder.append(array[i][j]);
            }
        }
        return builder.toString();
    }

    /**
     * 数独谜题字符串转换成二维数组，限四宫，六宫，九宫
     * @param puzzleString  数独字符串
     * @return null: 失败;  非null: 数独数组
     */
    public static int[][] string2IntArray(String puzzleString) {
        int len = puzzleString.length();
        int size;

        if (len == 81 || len == 64 || len == 36 || len == 16 || len == 25 || len == 49) {
            size = (int) Math.sqrt(len);
        } else {
            System.out.println("puzzstring error, len = " + len);
            return null;
        }


        int[][] array = new int[size][size];

        int k = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                array[i][j] = puzzleString.charAt(k++) - '0';
            }
        }
        return array;
    }

    public static boolean string2IntArray(String puzzleString, int[][] array) {
        int len = puzzleString.length();
        int size;

        if (len == 81 || len == 64 || len == 36 || len == 16 || len == 25 || len == 49) {
            size = (int) Math.sqrt(len);
        } else {
            System.out.println("puzzstring error, len = " + len);
            return false;
        }

        int k = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                array[i][j] = puzzleString.charAt(k++) - '0';
            }
        }
        return true;
    }

    /**
     * 打印二维数组数据
     * @param array 数独数据
     */
    public static void printArray(int[][] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                System.out.print(array[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void initArray(int[][] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                array[i][j] = 0;
            }
        }
    }

    public static void save(Context context, String inputText, String fileName) {
        FileOutputStream out;
        BufferedWriter writer = null;
        try {
            out = context.openFileOutput(fileName, Context.MODE_APPEND);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(inputText);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void save(String inputText, String filename) {
        FileOutputStream out;
        BufferedWriter writer = null;
        try {
            out = new FileOutputStream("/home/wsh/sudokuData/" + filename, true);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(inputText);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
