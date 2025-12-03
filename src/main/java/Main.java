import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static Scanner scanner = new Scanner(System.in);
    public static Set<String> commands = Set.of("exit", "echo", "type"); // immutable set 
    public static String[] dirs = System.getenv("PATH").split(File.pathSeparator);

    public static void main(String[] args) throws Exception {

        while (true) {
            System.out.print("$ ");
            String line = scanner.nextLine();

            if (line.equals("exit")) {
                break;
            } else if (line.startsWith("echo ")) {
                System.out.println(line.substring(5));
            } else if (line.startsWith("type ")) {
                String command = line.substring(5);
                runType(command);  
                
            } else {
                System.out.println(line + ": command not found");
            }
        }

        scanner.close();
    }

    public static void runType(String cmd) {
        if (commands.contains(cmd)) {
            System.out.println(cmd + " is a shell builtin");
            return;
        } 

        // search for files
        for (String dir: dirs) {
            File f = new File(dir, cmd); // checks if file exists in this directory

            if (f.exists() && f.canExecute()) {
                System.out.println(cmd + " is " + f.getAbsolutePath());
                return;
            }
        }

        System.out.println(cmd + ": not found");  
    }
}
