import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Main {
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        clearScreen();

        try (Terminal terminal = TerminalBuilder.builder().build()) {
            List<String> commands = Arrays.asList("echo", "exit");
            Completer completer = new StringsCompleter(commands);

            LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();

            while (true) {
                String line = reader.readLine("$ ");
    
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush(); // Forces the output to be written immediately
    }
}
