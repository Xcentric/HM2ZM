import application.Application;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            int exitCode = Application.run(args);

            if (exitCode != Application.ExitCodes.OK) {
                System.exit(exitCode);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(Application.ExitCodes.UNHANDLED_EXCEPTION);
        }
    }

}
