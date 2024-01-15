package quest.flo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class CleanWait {
    private final Object consumerLock = new Object();
    private final Object generatorLock = new Object();
    private final Vector<Integer> numbers = new Vector<>();
    private boolean running = true;
    private boolean generating = true;

    public CleanWait() {
        Generator generator = new Generator();
        Consumer consumer = new Consumer();
        KeyboardListener keyboardListener = new KeyboardListener();
        generator.setName("Generator");
        consumer.setName("Consumer");
        keyboardListener.setName("KeyboardListener");
        generator.start();
        consumer.start();
        keyboardListener.start();
    }

    private class Generator extends Thread {
        private final int interval;
        private final int amount;

        public Generator() {
            this.interval = 1000;
            this.amount = 10;
        }

        public Generator(int interval, int amount) {
            this.interval = interval;
            this.amount = amount;
        }

        public void run() {
            while (running) {
                if (!generating) {
                    try {
                        synchronized (generatorLock) {
                            generatorLock.wait();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }

                StringBuilder numString = new StringBuilder(this.getName() + ": Generated [");

                for (int i = 0; i < amount; i++) {
                    Integer num = (int) Math.round(Math.random() * 100);
                    numbers.addLast(num);

                    numString
                            .append(i != 0 ? ", " : "")
                            .append(Ansi.blue("0".repeat(3 - num.toString().length())))
                            .append(Ansi.blue(num.toString()));
                }
                numString.append("]");
                System.out.println(numString);

                synchronized (consumerLock) {
                    consumerLock.notify();
                }

                try {
                    //noinspection BusyWait
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private class Consumer extends Thread {
        private final int interval;
        private final int max;

        public Consumer() {
            this.interval = 2000;
            this.max = 10;
        }

        public Consumer(int interval, int max) {
            this.interval = interval;
            this.max = max;
        }

        public void run() {
            while (running) {
                if (numbers.size() == 0) {
                    System.out.println(this.getName() + ": No numbers found --> waiting!");
                    try {
                        synchronized (consumerLock) {
                            consumerLock.wait();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }

                int amount = numbers.size();

                String numbersFound = this.getName() + ": found " + Ansi.red(Integer.toString(amount)) + " of numbers! Using the next " + Ansi.green(
                        Integer.toString(max)) + "!";

                if (amount > this.max) {
                    amount = this.max;
                }

                StringBuilder header = new StringBuilder("|");
                StringBuilder separator = new StringBuilder("|");
                StringBuilder row = new StringBuilder("|");

                for (int i = 0; i < amount; i++) {
                    header.append("  ").append(Ansi.cyan(String.valueOf((char) (i + 65)))).append("  |");
                    separator.append(" --- |");
                    String num = numbers.removeFirst().toString();
                    row.append(" ").append(Ansi.blue("0".repeat(3 - num.length()) + num)).append(" |");
                }

                System.out.println("\n" + numbersFound + "\n" + header + "\n" + separator + "\n" + row + "\n");

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private class KeyboardListener extends Thread {
        private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        public void run() {
            while (running) {
                try {
                    switch ((char) this.reader.read()) {
                        case 'e' -> {
                            System.out.println(this.getName() + ": Exit");
                            running = false;

                            synchronized (generatorLock) {
                                generatorLock.notify();
                            }

                            synchronized (consumerLock) {
                                consumerLock.notify();
                            }
                        }
                        case 'w' -> {
                            System.out.print(this.getName() + ": ");

                            generating = !generating;

                            if (generating) {
                                System.out.println(Ansi.green("Start generating!"));

                                synchronized (generatorLock) {
                                    generatorLock.notify();
                                }
                            }

                            if (!generating) {
                                System.out.println(Ansi.red("Stop generating!"));
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
