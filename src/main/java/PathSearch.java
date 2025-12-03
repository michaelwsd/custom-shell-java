import java.io.File;

public class PathSearch {

    private static final String[] dirs =
            System.getenv("PATH").split(File.pathSeparator);

    private static final String[] winExt = {
        ".exe", ".cmd", ".bat"
    };

    public static String findExecutable(String cmd) {

        for (String dir : dirs) {
            File f = new File(dir, cmd);
            if (f.exists() && f.canExecute()) {
                return f.getAbsolutePath();
            }

            // Windows extension lookup
            for (String ext : winExt) {
                File f2 = new File(dir, cmd + ext);
                if (f2.exists() && f2.canExecute()) {
                    return f2.getAbsolutePath();
                }
            }
        }

        return null;
    }
}
