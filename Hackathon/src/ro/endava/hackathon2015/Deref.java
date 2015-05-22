package ro.endava.hackathon2015;

//import eu.sorescu.http.Compile;
import groovy.lang.GroovyObjectSupport;
import org.testng.reporters.Files;

import java.io.File;
import java.io.IOException;

public abstract class Deref  extends GroovyObjectSupport {
    public static Deref HTTP=new FolderDeref(new File("http"));
    public static void main(String[]args) throws IOException {
        System.out.println(HTTP.goIn("index").goIn("SearchHistory").getCode());
    }
    public abstract Deref goIn(String name);
    public abstract String getCode() throws IOException;

    public Object getProperty(String key)  {
        Object o=goIn(key);
        if(o!=null)return o;
        try {
            return Compile.compile(this.getCode());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
class FolderDeref extends Deref{
    private final File file;

    FolderDeref (File file){
    this.file=file;
}
    @Override
    public Deref goIn(String name) {
        File newF=new File(file, name);
        if(newF.isDirectory())return new FolderDeref(newF);
        newF=new File(file, name+".dsp");
        if(!newF.exists())return null;
        return new FileDeref(newF);
    }

    @Override
    public String getCode() {
        return null;
    }
}

class FileDeref extends Deref{
    File file;
//    String name;
    public FileDeref(File newF) {
        this.file=newF;
    }

    @Override
    public Deref goIn(String name) {
        return new FileDeref(file);
    }

    @Override
    public String getCode() throws IOException {
        String code= Files.readFile(file);
            return code.split("\\<\\?\\#")[0];
    }
}