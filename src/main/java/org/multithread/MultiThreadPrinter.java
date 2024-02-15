package org.multithread;

public class MultiThreadPrinter {
    private final Object monitor = new Object();
    private String printedLetter = "C";

    public static void main(String[] args) {
        MultiThreadPrinter printer = new MultiThreadPrinter();
        new Thread(() -> {
            printer.printB();
        }).start();
        new Thread(() -> {
            printer.printC();
        }).start();
        new Thread(() -> {
            printer.printA();
        }).start();
    }

    public void printA() {
        synchronized (monitor) {
            for (int i = 0; i < 5; i++) {
                while (!printedLetter.equals("C")) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.print("A");
                printedLetter = "A";
                monitor.notifyAll();
            }
        }
    }

    public void printB() {
        synchronized (monitor) {
            for (int i = 0; i < 5; i++) {
                while (!printedLetter.equals("A")) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.print("B");
                printedLetter = "B";
                monitor.notifyAll();
            }
        }
    }

    public void printC() {
        synchronized (monitor) {
            for (int i = 0; i < 5; i++) {
                while (!printedLetter.equals("B")) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.print("C");
                printedLetter = "C";
                monitor.notifyAll();
            }
        }
    }
}
