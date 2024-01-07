package application;

import application.commands.ConvertCommand;
import picocli.CommandLine;

public final class Application {

    private Application() {
    }

    /* INTERFACE */

    public static int run(String[] args) {
        return new CommandLine(new ConvertCommand()).execute(args);
    }

}
