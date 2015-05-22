import com.sun.net.httpserver.HttpsServer;
import ro.endava.hackathon2015.HttpServer;
import ro.endava.hackathon2015.SmtpServer;

import java.io.IOException;

public class Main {
    public static void main(String[]args){
        new Thread(() -> {
            try {
                SmtpServer.main(args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                HttpServer.main(args);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
