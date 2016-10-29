package com.example.wsh.sudoku;

/**
 * Created by wsh on 16-8-7.
 */
public class Test {

    public static void main(String[] args) {

        int row = 6;
        int column = 6;

        int[][] a = new int[row][column];

        int count = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                a[i][j] = count++;
            }
        }

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                System.out.print(a[i][j] + " ");
            }
            System.out.println();
        }

        for (int i = 0; i < row + column - 1; i++) {
            for (int j = 0; j <= i; j++) {
                if (j < row && i-j < column) {
                    System.out.print(a[j][i-j] + " ");
                }
            }
            System.out.println();
        }
    }
}
