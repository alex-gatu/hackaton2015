package ro.endava.hackathon2015;

import groovy.lang.GroovyShell;
import groovy.transform.TypeChecked;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

@TypeChecked
public class Compile {
    public static String compile(String original) throws IOException {
        return new GSP().parse(new StringReader(original));
    }

    public static Object exec(String script, String path, Map<String, Object> context) {
        GroovyShell gs = new GroovyShell();
        gs.getContext().getVariables().clear();
        for (String k : context.keySet())
            gs.getContext().setVariable(k, context.get(k));
        return gs.evaluate(script, path);
    }
}
