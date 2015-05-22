package ro.endava.hackathon2015;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.net.httpserver.HttpExchange;

public class HttpConnection extends Thread {
    public int responseCode = 200;
    public byte[] requestBytes;
    public HashMap<Object, Object> args = new HashMap<>();
    public HashMap<Object, Object> get = new HashMap<>();
    public HashMap<Object, Object> post = new HashMap<>();
    public HashMap<Object, Object> headers = new HashMap<>();
    public HashMap<Object, Object> responseHeaders = new HashMap<>();
    public HashMap<Object, Object> security = new HashMap<>();
    public final HttpExchange exchange;
    public final BufferedOutputStream output;
    static LinkedBlockingQueue<String[]> requests = new LinkedBlockingQueue<>();

    public HttpConnection(HttpExchange exchange) throws IOException {
        this.exchange = exchange;
        output = new BufferedOutputStream(this);
    }

    private boolean isHeadersSent = false;

    void makeSureHeadersAreSent() throws IOException {
        if (isHeadersSent)
            return;
        sendHeaders();
    }

    public void sendHeaders() throws IOException {
        if (isHeadersSent)
            throw new IllegalStateException("Headers already sent");
        isHeadersSent = true;
        responseHeaders.forEach((Object key, Object v) ->
                {
                    if (key instanceof String)
                        exchange.getResponseHeaders().add((String) key, String.valueOf(v));
                }
        );
        exchange.sendResponseHeaders(responseCode, 0);
        output.flushAndSwap();
    }

    public void run() {
        try {
            handle();
        } catch (Throwable t) {
            t.printStackTrace();
            printError(t, exchange);
        }
        try {
            makeSureHeadersAreSent();
            exchange.getResponseBody().flush();
        } catch (Throwable e) {
            if (!e.getMessage().startsWith(
                    "An existing connection was forcibly"))
                e.printStackTrace();
        }
        exchange.close();
    }

    public void handle() {
        try {
            {// HEADERS, COOKIE
                HttpHelper.mergeObject(headers, args);
            }
            {// POST
                requestBytes = readAllStream(exchange.getRequestBody());
                if ("multipart/form-data".equals(headers.get("Content-type:"))) {
                    post = multipartFormData(new String(requestBytes), (String) headers.get("Content-type:boundary"));
                } else {
                    post = argumentsAsObject(new String(requestBytes));
                }
                HttpHelper.mergeObject(post, args);
            }
            {// GET
                String queryString = exchange.getRequestURI().getRawQuery();
                get = argumentsAsObject(queryString);
                HttpHelper.mergeObject(get, args);
            }
            String url = exchange.getRequestURI().getRawPath();// .getPath();
            if(url.equals("/"))
                url="/index.dsp";
            String[] segments = url.split("/");
            for (int i = 0; i < segments.length; i++)
                segments[i] = URLDecoder.decode(segments[i], "UTF-8");
            File res = new File("http");
            for (String segment : segments) {
                if (res.isDirectory())
                    res = new File(res, segment);
            }
            if ("GET".equals(exchange.getRequestMethod()))
                requests.add(new String[]{get(this.security, "user"), url});
            responseCode = 200;
            if (!res.exists()) {
                res=new File("http\\HTTP404.dsp");
            };
            if (res.isFile()) {
                if (!res.getName().toLowerCase().endsWith(".dsp")) {
                    responseHeaders.put("Content-Type", "text/html");
                    String etag = String.valueOf(res.lastModified());
                    responseHeaders.put("ETag", etag);
                    if (etag.equals(headers.get("If-None-Match"))) {
                        responseCode = 304;
                        return;
                    }
                    sendHeaders();
                    output.write(Files.readAllBytes(res.toPath()));
                    return;
                } else {
                    responseHeaders.put("Content-Type",
                            "text/html");
                    responseHeaders.put("Pragma", "no-cache");
                    responseHeaders.put("Expires", "-1");
                    HashMap<String, Object> context = new HashMap<>();
                    context.put("_POST", post);
                    context.put("_GET", get);
                    context.put("_ARGS", args);
                    context.put("_HEADERS", headers);
                    context.put("httpConnection", this);
                    context.put("exchange", exchange);
                    context.put("out", output);
//                    DSCompiler.compile(new String(Files.readAllBytes(res.toPath())), res.getName());
                    String code = Compile.compile(new String(Files.readAllBytes(res.toPath())));//.split("<\\?#")[0]);
                    Compile.exec(code + ";this.' '();", exchange.getRequestURI().getPath(), context);
                }
            } else {
                responseCode = 403;
            }
            makeSureHeadersAreSent();
            try {
                this.output.flushAllCache();
            } catch (Throwable ignored) {
            }
        } catch (SocketTimeoutException ste) {
            System.err.println("TIMEOUT " + Thread.currentThread());
            ste.printStackTrace();
        } catch (Throwable e) {
            try {
                this.output.flushAllCache();
            } catch (Throwable ignored) {
            }
            printError(e, exchange);
            e.printStackTrace();
        }
    }

    private HashMap<Object, Object> multipartFormData(String string, String boundary) {
        HashMap<Object, Object> result = new HashMap<>();
        String[] parts = string.split("--" + boundary);
        for (String part : parts)
            try {
                part = part.substring(2, part.length() - 2);
                int i = part.indexOf("\r\n\r\n");
                String data = part.substring(i + 4);
                i = part.indexOf("name=\"");
                int j = part.indexOf("\"", i + 6);
                String name = part.substring(i + 6, j);
                result.put(name, data);
            } catch (Throwable ignored) {
            }
        return result;
    }

    private HashMap<Object, Object> argumentsAsObject(String queryString) {
        HashMap<Object, Object> noo = new HashMap<>();
        if ((queryString == null) || (queryString.length() == 0))
            return noo;
        for (String kv : queryString.split("&")) {
            String[] pair = kv.split("=");
            if (pair.length == 1)
                pair = new String[]{pair[0], ""};
            if (pair.length >= 2)
                try {
                    String k = URLDecoder.decode(pair[0], "UTF-8");
                    String v = URLDecoder.decode(pair[1], "UTF-8");
                    noo.put(k, v);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
        }
        return noo;
    }
    private boolean shouldSkip(StackTraceElement ste){
        if(ste.getClassName().startsWith("org.codehaus.groovy.runtime.callsite"))return true;
        if(ste.getClassName().startsWith("org.codehaus.groovy.runtime.ScriptBytecodeAdapter"))return true;
        if(ste.getClassName().startsWith("sun.reflect.NativeMethod"))return true;
        if(ste.getClassName().startsWith("groovy.lang.MetaMethod"))return true;
        if(ste.getClassName().startsWith("sun.reflect.DelegatingMethodAccessorImpl"))return true;
        if(ste.getClassName().startsWith("java.lang.reflect.Method"))return true;
        if(ste.getClassName().startsWith("org.codehaus.groovy.runtime.DefaultGroovyMethods"))return true;
        if(ste.getClassName().startsWith("groovy.lang.MetaClassImpl"))return true;
        if(ste.getClassName().startsWith("groovy.lang.GroovyShell"))return true;
        if(ste.getClassName().startsWith("org.codehaus.groovy.reflection.CachedMethod"))return true;
        if((ste.getClassName()+"."+ste.getMethodName()).equals("groovy.lang.Closure.call"))return true;
        if((ste.getClassName()+"."+ste.getMethodName()).equals("org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod"))return true;
        if((ste.getClassName()+"."+ste.getMethodName()).startsWith("org.codehaus.groovy.runtime.dgm"))return true;
        return false;
    }
    private String throwable2text(Throwable t,String prefix){
        String result=prefix;
        result+=t;
        for(StackTraceElement st:t.getStackTrace()) {
            if(shouldSkip(st))continue;
            result += "\r\n\tat " + st;
        }
        for (Throwable se : t.getSuppressed())
            result+="\r\n"+throwable2text(se, "Suppressed by: ");
        Throwable ourCause = t.getCause();
        if (ourCause != null)
            result+="\r\n"+throwable2text(ourCause,"Caused by: ");
        return result;
    }
    private void printError(Throwable t, HttpExchange exchange) {
        ByteArrayOutputStream sw = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(sw);
        pw.print(throwable2text(t, ""));
        pw.flush();
        String s = new String(sw.toByteArray());
        try {
            s = URLEncoder.encode(s, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        exchange.getResponseHeaders().add("X-Error", s);
        PrintStream printStream = new PrintStream(output);
        printStream.print(("</pre></script></textarea><pre style='color:red; background: #ffd'>"));
        t.printStackTrace();
        printStream.print(throwable2text(t,""));
//        t.printStackTrace(printStream);
        printStream.flush();
    }

    private static String get(HashMap<Object, Object> o, String key) {
        Object v = o.get(key);
        return (v instanceof String) ? (String) v : null;
    }

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private byte[] buffer = new byte[65536];

    private byte[] readAllStream(InputStream is) throws IOException {
        baos.reset();
        for (; ; ) {
            int len = is.read(buffer);
            if (len < 0)
                break;
            baos.write(buffer, 0, len);
        }
        byte[] data = baos.toByteArray();
        baos.reset();
        return data;
    }
}