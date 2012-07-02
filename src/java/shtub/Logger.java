package shtub;

public class Logger {
    private String name;

    private Logger(String name) {
        this.name = name;
    }

    public void debug(String messageTemplate, Object... parameters) {
        log(messageTemplate, parameters);
    }

    public void info(String messageTemplate, Object... parameters) {
        log(messageTemplate, parameters);
    }

    public void warn(Throwable t, String messageTemplate, Object... parameters) {
        log(messageTemplate, parameters);
        t.printStackTrace();
    }

    public void error(Throwable t, String messageTemplate, Object... parameters) {
        log(messageTemplate, parameters);
        t.printStackTrace();
    }

    private void log(String messageTemplate, Object... parameters) {
        System.out.println(String.format(messageTemplate, parameters));
    }

    public static Logger getLogger(Class aClass) {
        return new Logger(aClass.getCanonicalName());
    }
}
