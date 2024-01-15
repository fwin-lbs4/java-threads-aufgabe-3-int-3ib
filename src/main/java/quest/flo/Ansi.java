package quest.flo;

public class Ansi {
    public static String reset() {
        return "\u001B[0m";
    }

    public static String black(String string) {
        return "\u001B[30m" + string + Ansi.reset();
    }

    public static String red(String string) {
        return "\u001B[31m" + string + Ansi.reset();
    }

    public static String green(String string) {
        return "\u001B[32m" + string + Ansi.reset();
    }

    public static String yellow(String string) {
        return "\u001B[33m" + string + Ansi.reset();
    }

    public static String blue(String string) {
        return "\u001B[34m" + string + Ansi.reset();
    }

    public static String purple(String string) {
        return "\u001B[35m" + string + Ansi.reset();
    }

    public static String cyan(String string) {
        return "\u001B[36m" + string + Ansi.reset();
    }

    public static String white(String string) {
        return "\u001B[37m" + string + Ansi.reset();
    }
}
