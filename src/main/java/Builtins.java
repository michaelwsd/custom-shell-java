import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Builtins {

    private static File currentDir = new File(System.getProperty("user.dir"));
    private static File homeDir = new File(System.getenv("HOME"));
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
                runEcho(args);
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

    public static void runEcho(String args) {
        List<String> parts = parseArgs(args);

        System.out.println(String.join(" ", parts));
    }

    public static List<String> parseArgs(String line) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
    
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
    
            if (c == '\'' && !inDoubleQuote) { // toggle single quote
                inSingleQuote = !inSingleQuote;
                continue;
            } 
            if (c == '"' && !inSingleQuote) { // toggle double quote
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
    
            if (Character.isWhitespace(c) && !inSingleQuote && !inDoubleQuote) {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
    
        if (current.length() > 0) {
            args.add(current.toString());
        }
    
        return args;
    }    

    public static void runCat(String args) {
        if (args.isEmpty()) {
            System.err.println("cat: missing operand");
            return;
        }
    
        StringBuilder combinedOutput = new StringBuilder();
        List<String> files = parseArgs(args);
    
        for (String file: files) {
            Path p = Paths.get(file);
    
            try {
                byte[] info = Files.readAllBytes(p);
                String content = new String(info);
    
                // print to stdout 
                System.out.print(content);
    
                combinedOutput.append(content);
    
            } catch (NoSuchFileException e) {
                // STDERR should NOT be redirected
                System.err.println("cat: " + file + ": No such file or directory");
            } catch (Exception e) {
                System.err.println("cat: " + file + ": Error reading file");
            }
        }
    }

    public static boolean handleRedirection(String line, String regex) {
        String[] parts = line.split(regex, 2);
        if (parts.length != 2) return false;

        String command = parts[0].trim(), fileName = parts[1].trim();
        File outputFile = new File(fileName);
        PrintStream chosenStream = command.endsWith("2") ? System.err : System.out;
        OutputType type = command.endsWith("2") ? OutputType.err : OutputType.out;
        
        // allow command starting wiht 1
        if (command.endsWith("1") || command.endsWith("2")) {
            command = command.substring(0, command.length()-1).trim();
        }

        redirectOutput(chosenStream, command, type, outputFile, regex);

        return true;
    }

    public static void redirectOutput(PrintStream output, String command, OutputType type, File outputFile, String regex) {
        // create a temp buffer to store output
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        // print output/error to buffer instead of terminal
        switch (type) {
            case out -> {
                System.setOut(new PrintStream(buffer));
            }
            case err -> {
                System.setErr(new PrintStream(buffer));
            }
        }

        // run command
        boolean handled = runCommand(command);
        if (!handled) Executor.runProgram(command);

        // reset stream
        switch (type) {
            case out -> {
                System.setOut(output);
            }
            case err -> {
                System.setErr(output);
            }
        }

        // write to file
        try {
            if (regex.equals(">")) Files.write(outputFile.toPath(), buffer.toByteArray());
            else if (regex.equals(">>")) Files.write(outputFile.toPath(), buffer.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.err.println("redirection error: " + e.getMessage());
        }
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
            System.err.println("cd: " + dir + ": No such file or directory");
        }
    }

    public static void runType(String cmd) {
        if (cmd.equals("cat")) {
            System.out.println("cat is /bin/cat");
            return;
        }

        CommandName command = CommandName.of(cmd);
        if (command != null) {
            System.out.println(cmd + " is a shell builtin");
            return;
        }

        String found = PathSearch.findExecutable(cmd);

        if (found != null) {
            System.out.println(cmd + " is " + found);
        } else {
            System.err.println(cmd + ": not found");
        }
    }
}
