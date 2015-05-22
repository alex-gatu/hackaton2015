package ro.endava.hackathon2015;

import sun.net.httpserver.HttpServerImpl;

import java.io.IOException;
import java.net.InetSocketAddress;



public class HttpServer {
    public static void main(String[]args) throws IOException, InterruptedException {
        new HttpServer().start();
        for(;;)Thread.sleep(9999999);
    }

    public HttpServer() throws IOException {
        this(65190);
    }

    private com.sun.net.httpserver.HttpServer server;
    private final int port;

    public HttpServer(int port) throws IOException {
        this.port = port;
    }

    public void start() {
        try {
            System.out.println("Starting on "+port);
            server = HttpServerImpl.create(new InetSocketAddress(port), 99);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/", arg0 -> new HttpConnection(arg0).start());
        server.start();
    }
}