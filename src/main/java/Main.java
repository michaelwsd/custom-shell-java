import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Set<String> commands = new HashSet<>(Set.of("exit", "echo", "type"));

        while (true) {
            System.out.print("$ ");
            String line = scanner.nextLine();

            if (line.equals("exit")) {
                break;
            } else if (line.startsWith("echo ")) {
                System.out.println(line.substring(5));
            } else if (line.startsWith("type ")) {
                String command = line.substring(5);
                if (commands.contains(command)) {
                    System.out.println(command + " is a shell builtin");
                } else {
                    System.out.println(command + ": not found");    
                }
            } else {
                System.out.println(line + ": command not found");
            }
        }

        scanner.close();
    }
}
