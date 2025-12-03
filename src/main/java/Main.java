import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
                // check for program and run 
                String[] input = line.split(" ");
                
                if (!runProgram(input)) {
                    System.out.println(line + ": command not found");
                }
            }
        }

        scanner.close();
    }

    public static boolean runProgram(String[] input) {
        String program = input[0];
        String path = findExecutable(program);
        if (path == null) return false;

        List<String> cmd = new ArrayList<>();
        cmd.add(path);
        for (int i = 1; i < input.length; i++) {
            cmd.add(input[i]);
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.inheritIO();
            Process p = pb.start();
            p.waitFor();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String findExecutable(String cmd) {
        for (String dir: dirs) {
            File f = new File(dir, cmd); // checks if file exists in this directory

            if (f.exists() && f.canExecute()) {
                return f.getAbsolutePath();
            }
        }

        return null;
    }

    public static void runType(String cmd) {
        if (commands.contains(cmd)) {
            System.out.println(cmd + " is a shell builtin");
            return;
        } 

        String found = findExecutable(cmd);

        if (found != null) {
            System.out.println(cmd + " is " + found);
        } else {
            System.out.println(cmd + ": not found");
        }  
    }
}
