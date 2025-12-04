import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Builtins {

    private static File currentDir = new File(System.getProperty("user.dir"));
    private static File homeDir = new File(System.getProperty("user.home"));
    private static List<String> historyList = new ArrayList<>();

    // returns true if a builtin handled the line
    public static boolean runCommand(String line) {
        String[] parts = line.split("\\s+", 2);
        CommandName command = CommandName.of(parts[0]);
        String args = parts.length > 1 ? parts[1] : "";
        
        if (command == null) return false;

        // add to history
        historyList.add(parts[0]);

        switch (command) {
            case exit -> {
                System.exit(0);
            }
            case echo -> {
                System.out.println(args);
            }
            case pwd -> {
                System.out.println(currentDir.getAbsolutePath());
            }
            case type -> {
                runType(args);
            }
            case cd -> {
                runCd(args.isEmpty() ? "~" : args);
            }
            case history -> {
                runHistory();
            }
        }

        return true; // not a builtin
    }

    public static void runHistory() {
        int i = 1;
        for (String cmd: historyList) {
            System.out.printf("%5d  %s%n", i++, cmd);
        }
    }

    public static void runCd(String dir) {
        Path newPath;

        if (dir.equals("~")) {
            currentDir = homeDir;
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
        CommandName command = CommandName.of(cmd);
        if (command != null) {
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
