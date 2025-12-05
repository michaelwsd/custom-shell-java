import java.util.Scanner;

import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.TerminalBuilder;

public class Main {
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        clearScreen();
        var terminal = TerminalBuilder.builder().system(true).build();

        var parser = new DefaultParser();
        parser.setEscapeChars(new char[0]);
        var stringsCompleter = new StringsCompleter("echo", "exit");
        var lineReader =
                LineReaderBuilder.builder()
                        .terminal(terminal)
                        .completer(stringsCompleter)
                        .parser(parser)
                        .build();

        while (true) {
            String line = lineReader.readLine("$ ");

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
