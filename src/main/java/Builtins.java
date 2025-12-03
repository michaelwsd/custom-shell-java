import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class Builtins {

    private static final Set<String> builtins = Set.of("exit", "echo", "type", "pwd");
    private static File currentDir = new File(System.getProperty("user.dir"));
    private static File homeVar = new File(System.getProperty("user.home"));

    public static boolean isBuiltin(String cmd) {
        return builtins.contains(cmd);
    }

    // returns true if a builtin handled the line
    public static boolean handle(String line) {
        if (line.equals("exit")) {
            System.exit(0);
        }

        if (line.startsWith("echo ")) {
            System.out.println(line.substring(5));
            return true;
        }

        if (line.equals("pwd")) {
            System.out.println(currentDir.getAbsolutePath());
            return true;
        }

        if (line.startsWith("type ")) {
            String cmd = line.substring(5);
            runType(cmd);
            return true;
        }

        if (line.startsWith("cd ")) {
            String dir = line.substring(3);
            runCd(dir);
            return true;
            
        }

        return false; // not a builtin
    }

    public static void runCd(String dir) {
        Path newPath;

        if (dir.equals("~")) {
            currentDir = homeVar;
            return;
        }

        // absolute paths
        Path inputPath = Paths.get(dir);
        if (!inputPath.isAbsolute()) {
            inputPath = currentDir.toPath().resolve(dir); // combine relative path with currentDir
        }

        // normalize to remove "." and ".."
        newPath = inputPath.normalize();

        if (Files.exists(newPath) && Files.isDirectory(newPath)) {
            currentDir = newPath.toFile();
        } else {
            System.out.println("cd: " + dir + ": No such file or directory");
        }
    }

    public static void runType(String cmd) {
        if (isBuiltin(cmd)) {
            System.out.println(cmd + " is a shell builtin");
            return;
        }

        String found = PathSearch.findExecutable(cmd);

        if (found != null) {
            System.out.println(cmd + " is " + found);
        } else {
            System.out.println(cmd + ": not found");
        }
    }
}
