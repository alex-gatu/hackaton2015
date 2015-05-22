package ro.endava.hackathon2015;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
 
public class SmtpThread extends Thread {
    private String serverDomain;
    private Socket _socket;
    private PrintWriter out;
    private BufferedReader in;
 
    SmtpThread(Socket socket) throws IOException {
        this._socket = socket;
        this.out = new PrintWriter(this._socket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(
                this._socket.getInputStream()));
        serverDomain = socket.getLocalAddress().getHostName();
    }
 
    private static String extractArguments(String commandLine,
            String... commandNames) {
        for (int i = 0; i < commandNames.length; i++)
            if (commandLine.toUpperCase().startsWith(
                    commandNames[i].toUpperCase()))
                return commandLine.substring(commandNames[i].length());
        return null;
    }
 
    @Override
    public void run() {
        try {
            System.out.println("SMTP received: " + new Date());
            writeLine("220 " + serverDomain
                    + " DMS SMTP MAIL Service, Version: 1.0 1410261259");
            for (;;) {
                String commandLine = in.readLine();
                if (commandLine == null)
                    break;
                if (commandLine.length() == 0)
                    break;
                System.out.println(commandLine);
                String commandArguments = "";
                commandArguments = extractArguments(commandLine, "HELO ",
                        "EHLO ");
                if (commandArguments != null) {
                    HELO_EHLO(commandArguments);
                    continue;
                }
                commandArguments = extractArguments(commandLine, "MAIL FROM:");
                if (commandArguments != null) {
                    MAIL_FROM(commandArguments);
                    continue;
                }
                commandArguments = extractArguments(commandLine, "RCPT TO:");
                if (commandArguments != null) {
                    RCPT_TO(commandArguments);
                    continue;
                }
                commandArguments = extractArguments(commandLine, "DATA");
                if (commandArguments != null) {
                    DATA(commandArguments);
                    continue;
                }
                commandArguments = extractArguments(commandLine, "RSET");
                if (commandArguments != null) {
                    RSET(commandArguments);
                    continue;
                }
                commandArguments = extractArguments(commandLine, "NOOP");
                if (commandArguments != null) {
                    NOOP(commandArguments);
                    continue;
                }
                commandArguments = extractArguments(commandLine, "QUIT");
                if (commandArguments != null) {
                    QUIT(commandArguments);
                    break;
                }
                NA(commandArguments);
            }
            _socket.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
 
    private void writeLine(String string) throws IOException {
        out.println(string);
        out.flush();
    }
 
    public void HELO_EHLO(String domain) throws IOException {
        writeLine("250 " + serverDomain);
    }
 
    private ArrayList<String> to = new ArrayList<String>();
 private String mailFrom;
    public void MAIL_FROM(String reversePath) throws IOException {
        try {
            this.mailFrom=reversePath;
            writeLine("250 OK");
        } catch (Throwable t) {
            t.printStackTrace();
            writeLine("451 Requested action aborted: local error in processing");
        }
    }
 
    public void RCPT_TO(String forwardPath) throws IOException {
        try {
            this.to.add(forwardPath);
            writeLine("250 OK");
        } catch (Throwable t) {
            t.printStackTrace();
            writeLine("451 Requested action aborted: local error in processing");
        }
    }
 
    public void DATA(String empty) throws IOException {
        writeLine("354 Start mail input; end with <CRLF>.<CRLF>");
        String raw = "";
        for (;;) {
            String line = in.readLine();
            if (line == null)
                break;
            if (line.equals("."))
                break;
            raw += line + "\r\n";
        }
        try {
            for (int i = 0; i < to.size(); i++) {
                String too = to.get(i);
                if (too.indexOf('<') >= 0)
                    too = too.split("\\<")[1].split("\\>")[0];
                String[] toos = too.split("\\@");
                SimpleSMTPRelay.relay(mailFrom, raw);
            }

            writeLine("250 OK");
        } catch (Throwable t) {
            t.printStackTrace();
            writeLine("451 Requested action aborted: local error in processing");
        }
    }
 
    public void RSET(String empty) throws IOException {
        // from = null;
        to = new ArrayList<String>();
        writeLine("250 OK");
    }
 
    public void NA(String reversePath) throws IOException {
        writeLine("502 Command not implemented");
    }
    public void NOOP(String arguments) throws IOException {
        writeLine("250 OK");
    }

    public void QUIT(String arguments) throws IOException {
        writeLine("221 " + serverDomain
                + " Service closing transmission channel");
    }
}