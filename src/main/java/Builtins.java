import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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

        // not a built-in method
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
            case cat -> {
                runCat(args);
            }
        }

        return true; 
    }

    public static void runCat(String args) {
        if (args.isEmpty()) {
            System.err.println("cat: missing operand");
            return;
        }
    
        StringBuilder combinedOutput = new StringBuilder();
        String[] files = args.split("\\s+");
    
        for (String file : files) {
            Path p = Paths.get(file);
    
            try {
                byte[] info = Files.readAllBytes(p);
                String content = new String(info);
    
                // print to stdout (this may be redirected)
                System.out.print(content);
    
                combinedOutput.append(content);
    
            } catch (NoSuchFileException e) {
                // STDERR should NOT be redirected
                System.out.println("cat: " + file + ": No such file or directory");
            } catch (Exception e) {
                System.out.println("cat: " + file + ": Error reading file");
            }
        }
    }

    public static boolean handleRedirection(String line) {
        String[] parts = line.split(">", 2);

        if (parts.length != 2) return false;

        String command = parts[0].trim(), fileName = parts[1].trim();

        if (command.endsWith("1")) {
            command = command.substring(0, command.length()-1).trim();
        }

        File outputFile = new File(fileName);

        PrintStream originalOutput = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer)); // print output to buffer instead of terminal 

        // run command
        boolean handled = runCommand(command);
        if (!handled) {
            Executor.runProgram(command);   
        }

        // restore original output
        System.setOut(originalOutput);

        // write to file 
        try {
            Files.write(outputFile.toPath(), buffer.toByteArray());
        } catch (Exception e) {
            System.err.println("redirection error: " + e.getMessage());
        }

        return true;
    }

    public static void runLs(String args) {
        String[] parts = args.split("\\s+");
        List<String> flags = new ArrayList<>();
        List<String> paths = new ArrayList<>();

        for (String p: parts) {
            if (p.startsWith("-")) flags.add(p);
            else paths.add(p);
        }

        if (paths.isEmpty()) {
            paths.add(".");
        }

        // list each path with the provided flags
        for (String path: paths) {
            listPath(path, flags);
        }
    }

    private static void listPath(String pathStr, List<String> flags) {
        Path p = Paths.get(pathStr);

        if (!Files.exists(p)) {
            System.err.println("ls: " + pathStr + ": No such file or directory");
            return;
        }

        if (!Files.isDirectory(p)) {
            System.out.println(pathStr);
            return;
        }

        try {
            Files.list(p)
                .map(x -> x.getFileName().toString())
                .sorted()
                .forEach(System.out::println);
        } catch (IOException e) {
            System.err.println("ls: " + pathStr + ": Error reading directory");
        }
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
