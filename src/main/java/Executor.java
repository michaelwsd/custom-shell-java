import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Executor {

    public static boolean runProgram(String line) {
        String[] input = line.split("\\s+");
        String program = input[0];
        String path = PathSearch.findExecutable(program);

        if (path == null) return false;

        List<String> cmd = new ArrayList<>();
        cmd.add(program);
        cmd.addAll(Arrays.asList(input).subList(1, input.length));

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.inheritIO();
            Process p = pb.start();
            p.waitFor();
            return true;
        } catch (Exception e) {
            return true; // found but error running
        }
    }
}
