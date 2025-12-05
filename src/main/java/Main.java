import java.util.Scanner;

public class Main {
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        clearScreen();

        while (true) {
            System.out.print("$ ");
            String line = scanner.nextLine().strip();

            if (line.isEmpty()) continue;
            if (Builtins.handleRedirection(line, ">>")) continue;
            if (Builtins.handleRedirection(line, ">")) continue;

            // check for command
            if (!Builtins.runCommand(line)) {
                if (!Executor.runProgram(line)) {
                    System.out.println(line + ": command not found");
                }
            }
        }
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush(); // Forces the output to be written immediately
    }
}
