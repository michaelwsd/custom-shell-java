import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.reader.EndOfFileException;

public class Main {
    public static void main(String[] args) throws Exception {

        try (Terminal terminal = TerminalBuilder.terminal()) {
            LineReader reader =
                    LineReaderBuilder.builder()
                            .terminal(terminal)
                            .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                            .build();

            while (true) {
                try {
                    String line = reader.readLine("$ ");
                    if (line.isEmpty()) continue;

                    if (!Builtins.runCommand(line)) {
                        if (!Executor.runProgram(line)) {
                            System.out.println(line + ": command not found");
                        }
                    }

                } catch (UserInterruptException | EndOfFileException e) {
                    break;
                }
            }
        }
    }
}
