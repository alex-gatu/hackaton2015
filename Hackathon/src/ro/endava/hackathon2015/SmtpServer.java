package ro.endava.hackathon2015;
 
import java.io.IOException;
import java.net.ServerSocket;
 
public class SmtpServer extends Thread {
    public static void main(String[] args) throws IOException {
        new SmtpServer(25).start();
    }

    private ServerSocket serverSocket;

    SmtpServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        System.out.println("SMTP server up...");
        for (; ; ) {
            try {
                new SmtpThread(serverSocket.accept()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}