package io.foldright.study.main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();
        System.out.println("========================================");
        System.out.println("Hello World!");
        System.out.println(executor);
        System.out.println("========================================");
    }
}
