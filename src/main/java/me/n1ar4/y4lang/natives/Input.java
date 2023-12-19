package me.n1ar4.y4lang.natives;

import java.util.Scanner;

public class Input {
    public static String input(){
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
}
