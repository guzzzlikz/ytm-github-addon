import java.util.Date;

public class Logger {
    private Date date;
    public Logger() {
    }
    public void log(String msg) {
        date = new Date();
        System.out.print("[" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "]" + " " + msg);
    }
    public void logln(String msg) {
        date = new Date();
        System.out.println("[" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "]" + " " + msg);
    }
}
