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
        
        // add to history
        historyList.add(line);

        if (command == null) return false;

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
                runHistory(args);
            }
            case ls -> {
                runLs(args);
            } 
            case clear -> {
                Main.clearScreen();
            }
        }

        return true; // not a builtin
    }

    public static File[] runLs(String args) {
        
        if (args.equals("")) {
            File[] files = currentDir.listFiles();
            if (files != null) {
                listFiles(files);
                return files;
            } else {
                System.out.println("Could not list files, possibly due to an I/O error or the path not being a directory.");
            }
        } else {
            File givenDir = new File(args);
            if (givenDir.exists()) {
                File[] files = givenDir.listFiles();
                listFiles(files);
                return files;
            } else {
                System.out.println("Could not list files, possibly due to an I/O error or the path not being a directory.");
            }
        }

        return null;
    }

    public static void listFiles(File[] files) {
        for (File file : files) {
            if (file.isFile()) {
                System.out.println("File: " + file.getName());
            } else if (file.isDirectory()) {
                System.out.println("Directory: " + file.getName());
            }
        }
    }

    public static void runHistory(String args) {
        try {
            int number = Integer.parseInt(args);
            if (number >= historyList.size()) printHistory(1);
            else {
                printHistory(historyList.size() - number + 1);
            }
        } catch (NumberFormatException e) {
            printHistory(1);
        }
    }

    public static void printHistory(int start) {
        for (int i = start-1; i < historyList.size(); i++) {
            System.out.printf("%5d  %s%n", start++, historyList.get(i));
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
