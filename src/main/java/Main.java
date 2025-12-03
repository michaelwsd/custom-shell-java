import java.util.Scanner;

public class Main {
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {

        while (true) {
            System.out.print("$ ");
            String line = scanner.nextLine().strip();

            if (line.isEmpty()) continue;

            if (Builtins.handle(line)) {
                // builtin was executed
                continue;
            }

            // external program
            String[] input = line.split("\\s+");

            if (!Executor.runProgram(input)) {
                System.out.println(line + ": command not found");
            }
        }
    }
}
