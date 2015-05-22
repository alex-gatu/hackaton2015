package ro.endava.hackathon2015;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

public class GSP {
    public String parse(Reader reader) throws IOException {
        SimpleTemplate template = new SimpleTemplate();
        return template.parse(reader);
    }

    public static class SimpleTemplate {

        protected String parse(Reader reader) throws IOException {
            if (!reader.markSupported()) {
                reader = new BufferedReader(reader);
            }
            StringWriter sw = new StringWriter();
            startScript(sw);
            int c;
            while ((c = reader.read()) != -1) {
                if (c == '<') {
                    reader.mark(1);
                    c = reader.read();
                    if (!isPercent(c)) {
                        sw.write('<');
                        reader.reset();
                    } else {
                        reader.mark(1);
                        c = reader.read();
                        if (c == '=') {
                            groovyExpression(reader, sw, "");
                        } else if (c == '&') {
                            groovyExpression(reader, sw, "groovy.xml.XmlUtil.escapeXml");
                        } else if (c == '%') {
                            groovyExpression(reader, sw, "java.net.URLEncoder.encode");
                        } else if (c == '#') {
                            sw.append("\'\'\');};");
                            String[] s = groovyDump(reader).split("\\(");
                            sw.append("this." + s[0]);
                            String params = "";
                            if (s.length > 1) {
                                if (s[1].split("\\)").length > 0)
                                    params = s[1].split("\\)")[0];
                            }
                            if (params.length() > 0) {
                                String[] pp = params.split("\\,");
                                params = "";
                                for (String q : pp) {
                                    if (params.length() > 0)
                                        params += ",";
                                    params += "def " + q + "=null";
                                }
                            }
                            sw.append("={" + params + "->out.print(\'\'\'");
                        } else {
                            reader.reset();
                            groovySection(reader, sw);
                        }
                    }
                    continue;
                }
                if (c == '\'') {
                    sw.write('\\');
                }
                if (c == '$') {
                    sw.write('\\');
                }
                if (c == '\n' || c == '\r') {
                    if (c == '\r') {
                        reader.mark(1);
                        c = reader.read();
                        if (c != '\n') {
                            reader.reset();
                        }
                    }
                    sw.write("\n");
                    continue;
                }
                sw.write(c);
            }
            endScript(sw);
            System.out.println(sw.toString());
            return sw.toString();
        }

        private void startScript(StringWriter sw) {
            sw.write("this.\' \'={->out.print(\'\'\'");
        }

        private void endScript(StringWriter sw) {
            sw.write("\'\'\');}");
        }

        private void groovyExpression(Reader reader, StringWriter sw, String processingFunction) throws IOException {
            sw.write("''');out.print(" + processingFunction + "((");
            int c;
            while ((c = reader.read()) != -1) {
                int cc = c;
                if (isPercent(cc)) {
                    c = reader.read();
                    if (c != '>') {
                        sw.write(cc);
                    } else {
                        break;
                    }
                }
                if (c != '\n' && c != '\r') {
                    sw.write(c);
                }
            }
            sw.write(")as String));out.print('''");
        }

        private String groovyDump(Reader reader) throws IOException {
            StringWriter sw = new StringWriter();
            int c;
            while ((c = reader.read()) != -1) {
                int cc = c;
                if (isPercent(cc)) {
                    c = reader.read();
                    if (c != '>') {
                        sw.write(cc);
                    } else {
                        break;
                    }
                }
                sw.write(c);
            }
            return sw.toString();
        }

        private void groovySection(Reader reader, StringWriter sw) throws IOException {
            sw.write("\'\'\');");
            int c;
            while ((c = reader.read()) != -1) {
                int cc = c;
                if (isPercent(cc)) {
                    c = reader.read();
                    if (c != '>') {
                        sw.write(cc);
                    } else {
                        break;
                    }
                }
                sw.write(c);
            }
            sw.write(";out.print(\'\'\'");
        }

    }

    private static boolean isPercent(int c) {
        return (c == '%') || (c == '?');
    }
}
