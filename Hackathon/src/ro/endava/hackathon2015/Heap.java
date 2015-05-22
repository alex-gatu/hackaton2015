package ro.endava.hackathon2015;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import groovy.json.JsonOutput;
import groovy.json.internal.JsonParserCharArray;


public class Heap {
    private static final long serialVersionUID = -5685880176240431312L;
    final File folder;
    private String joinedPath = "";

    public Heap(String... path) throws IOException {
        if (path.length == 1)
            if (path[0].length() == 0)
                path = new String[0];
        for (String aPath : path) this.joinedPath += "\\" + URLEncoder.encode(aPath, "UTF-8");
        folder = new File("heap" + this.joinedPath + "\\");
    }

    private File file(String arg0) {
        try {
            String encodedName = encode(arg0);
            return new File(folder, encodedName);
        } catch (IOException e) {
            throw new IllegalAccessError("heap://" + joinedPath + "/" + arg0);
        }
    }

    public Object[] list() {
        File[] files = folder.listFiles();
        ArrayList<String> result = new ArrayList<>();
        try {
            if (files != null)
                for (File file : files)
                    if (file.isFile())
                        result.add(decode(file.getName()));
        } catch (IOException e) {
            throw new NullPointerException(e.toString());
        }
        Object[] vector = result.toArray(new String[result.size()]);
        return vector;
    }

    public String[] listHeaps() throws IOException {
        File[] files = folder.listFiles();
        ArrayList<String> result = new ArrayList<>();
        if (files != null)
            for (File file : files)
                if (file.isDirectory())
                    result.add(decode(file.getName()));
        return result.toArray(new String[result.size()]);
    }

    // @Override
    public Object get(final Object arg0) {
        if (arg0 == null)
            throw new NullPointerException("d1012150913 Heap " + folder
                    + ": argument null");
        if (!(arg0 instanceof String))
            throw new NullPointerException("d1012150914 Heap " + folder + ": "
                    + arg0.getClass().getName() + " only String");
        return get((String) arg0);
    }

    public Object get(final String arg0, Object defaultValue) {
        Object result = get(arg0);
        if (result == null)
            result = defaultValue;
        return result;
    }

    private Object get(final String arg0) {
        try {
            File f = file(arg0);
            byte[] data = Files.readAllBytes(f.toPath());
            return new JsonParserCharArray().parse(data);
        } catch (Throwable ignored) {
        }
        return null;
    }

    public long getSize(String arg0) {
        try {
            return file(arg0).length();
        } catch (Throwable t) {
            return 0;
        }
    }

    public boolean has(String arg0) {
        if (arg0 == null)
            return false;
        File file = file(arg0);
        return file != null && file(arg0).exists();
    }

    public String put(Object value) {
        return put(null, value);
    }

    private static SimpleDateFormat TIME_STAMP = new SimpleDateFormat(
            "yyMMddhhmmssSSS");

    public String put(String key, Object value) {
        if ((key == null) || (key.length() == 0))
            key = UUID.randomUUID().toString() + "_"
                    + TIME_STAMP.format(new Date());
        write(file(key), value);
        return key;
    }

    public void delete(String arg0) {
        file(arg0).delete();
    }

    private static void write(File file, Object value)
             {
        String ser = JsonOutput.prettyPrint(JsonOutput.toJson(value));
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            Files.write(file.toPath(), ser.getBytes(), StandardOpenOption.CREATE);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    private static String encode(String k) throws UnsupportedEncodingException {
        return URLEncoder.encode(k, "UTF-8");
    }

    private static String decode(String k) throws UnsupportedEncodingException {
        return URLDecoder.decode(k, "UTF-8");
    }
}