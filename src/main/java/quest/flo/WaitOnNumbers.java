package quest.flo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class WaitOnNumbers {
    private final Object lock = new Object();
    private final Vector<Integer> numbers = new Vector<>();
    private final String ANSI_RESET = "\u001B[0m";
    private final String ANSI_BLACK = "\u001B[30m";
    private final String ANSI_RED = "\u001B[31m";
    private final String ANSI_GREEN = "\u001B[32m";
    private final String ANSI_YELLOW = "\u001B[33m";
    private final String ANSI_BLUE = "\u001B[34m";
    private final String ANSI_PURPLE = "\u001B[35m";
    private final String ANSI_CYAN = "\u001B[36m";
    private final String ANSI_WHITE = "\u001B[37m";
    private boolean running = true;
    private boolean generating = true;

    public WaitOnNumbers() {
        new Generator("Generator").start();
        new Consumer("Consumer").start();
        new KeyboardListener("Keyboard").start();
    }

    private class KeyboardListener extends Thread {
        private final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        private String name = "";

        public KeyboardListener(String name) {
            this.name = ANSI_RESET + name + ": ";
        }

        public void run() {
            while (running) {
                try {
                    switch ((char) this.bufferedReader.read()) {
                        case 'e' -> {
                            System.out.println(this.name + "Exit");
                            running = false;

                            synchronized (lock) {
                                lock.notifyAll();
                            }
                        }
                        case 'w' -> {
                            System.out.print(this.name);

                            generating = !generating;

                            if (generating) {
                                System.out.println(ANSI_GREEN + "Start generating!" + ANSI_RESET);
                                synchronized (lock) {
                                    // notifyAll() since notify() only wakes one arbitrary Thread and there seems to be no way to ensure what thread gets woken up
                                    lock.notifyAll();
                                }
                            }

                            if (!generating) {
                                System.out.println(ANSI_RED + "Stop generating!" + ANSI_RESET);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private class Generator extends Thread {
        private final int interval;
        private final int amount;
        private String name = "";


        public Generator(String name, int interval, int amount) {
            this.name = ANSI_RESET + name + ": ";
            this.interval = interval;
            this.amount = amount;
        }

        public Generator(String name) {
            this.name = ANSI_RESET + name + ": ";
            this.interval = 1000;
            this.amount = 10;
        }

        public void run() {
            while (running) {
                if (!generating) {
                    try {
                        synchronized (lock) {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                StringBuilder numString = new StringBuilder(this.name + "Generated [");

                for (int i = 0; i < amount; i++) {
                    Integer num = (int) Math.round(Math.random() * 100);
                    numbers.addLast(num);

                    numString
                            .append(i != 0 ? ", " : "")
                            .append(ANSI_BLUE)
                            .append("0".repeat(3 - num.toString().length()))
                            .append(num)
                            .append(ANSI_RESET);
                }
                numString.append("]");
                System.out.println(numString);

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private class Consumer extends Thread {
        private final int interval = 2000;
        private final int max = 10;
        private String name = "";

        public Consumer(String name) {
            this.name = ANSI_RESET + name + ": ";
        }

        public void run() {
            while (running) {
                if (numbers.size() > 0) {
                    int amount = numbers.size();

                    String numbersFound = this.name + "found " + ANSI_RED + amount + ANSI_RESET + " of numbers using the next " + ANSI_GREEN + "10" + ANSI_RESET + "!";

                    if (amount > max) {
                        amount = max;
                    }

                    StringBuilder header = new StringBuilder("|");
                    StringBuilder separator = new StringBuilder("|");
                    StringBuilder row = new StringBuilder("|");

                    for (int i = 0; i < amount; i++) {
                        header.append("  ").append(ANSI_CYAN).append((char) (i + 65)).append(ANSI_RESET).append("  |");
                        separator.append(" --- |");
                        String num = numbers.removeFirst().toString();
                        row
                                .append(" ")
                                .append(ANSI_BLUE)
                                .append("0".repeat(3 - num.length()))
                                .append(num)
                                .append(ANSI_RESET)
                                .append(" |");
                    }

                    System.out.println("\n" + numbersFound + "\n" + header + "\n" + separator + "\n" + row + "\n");

                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (numbers.size() == 0) {
                    System.out.println(this.name + "No numbers found --> waiting for " + ANSI_RED + interval + "ms" + ANSI_RESET);
                    try {
                        synchronized (lock) {
                            lock.wait(interval);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        }
    }
}
