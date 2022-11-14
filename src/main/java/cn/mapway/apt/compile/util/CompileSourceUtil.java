package cn.mapway.apt.compile.util;

import cn.mapway.gwt.runtime.client.compile.ICompileInfo;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CompileSourceUtil
 *
 * @author zhang
 */
public class CompileSourceUtil {
    /**
     * 生成 编译信息的源代码
     *
     * @param element
     * @return JavaFile
     */
    public static JavaFile compileSource(TypeElement element) {
        String gitHash = "编译环境需要有GIT";
        String gitAuthor = "";
        long gitTime = 0L;
        String gitCommit = "";

        String exec = "git --no-pager  log -n 1 --pretty=format:\"%h%n%cE%n%cI%n%s\"";
        long commitTime = System.currentTimeMillis();
        try {
            StringBuilder stringBuilder = Lang.execOutput(exec);
            String ll = stringBuilder.toString().trim();
            ll = Strings.removeFirst(ll, '"');
            ll = Strings.removeLast(ll, '"');
            BufferedReader reader = new BufferedReader(Streams.utf8r(Lang.ins(ll)));
            List<String> lines = new ArrayList<String>();
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            if (lines.size() >= 3) {
                gitHash = lines.get(0).trim();
                gitAuthor = lines.get(1);
                String t = lines.get(2).replaceAll("T", " ");
                gitTime = Times.D(t).getTime();
            }
            if (lines.size() >= 4) {
                gitHash = lines.get(0).trim();
                gitAuthor = lines.get(1);
                String t = lines.get(2).replaceAll("T", " ");
                gitTime = Times.D(t).getTime();
                gitCommit = lines.get(3);
            } else {
                gitAuthor = "Unknown";
                gitTime = System.currentTimeMillis();
                gitHash = "Unknown";
            }
        } catch (IOException e) {
            e.printStackTrace();
            gitAuthor = "Unknown";
            gitTime = System.currentTimeMillis();
            gitHash = e.getMessage();
        }


        String template = "if(COMPILE_INFO==null){\r\n" +
                " COMPILE_INFO=new ICompileInfo(){\r\n" +
                " public java.util.Date getCompileTime(){ return new java.util.Date(" + System.currentTimeMillis() + "L);}\r\n" +
                " public String getGitCommit(){ return \"" + gitCommit + "\";}\r\n" +
                " public String getGitAuthor(){ return \"" + gitAuthor + "\";}\n\n" +
                " public String getVersion(){ return \"" + gitHash + "\";}\n\n" +
                " public java.util.Date getGitTime(){ return new java.util.Date(" + gitTime + "L);}\n\n" +
                "};\n" +
                "}\r\nreturn COMPILE_INFO;\n";

        String packageName = "cn.mapway.gwt.runtime.client.compile";

        String filedName = "COMPILE_INFO";
        FieldSpec constField = FieldSpec.builder(ICompileInfo.class, filedName,
                Modifier.PUBLIC, Modifier.STATIC).build();
        MethodSpec getMethod = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ICompileInfo.class)
                .addCode(template)
                .build();
        TypeSpec compileInfoImpl = TypeSpec
                .classBuilder("CompileInfo")
                .addModifiers(Modifier.PUBLIC)
                .addField(constField)
                .addMethod(getMethod)
                .build();
        JavaFile javaFile = JavaFile.builder(packageName, compileInfoImpl)
                .skipJavaLangImports(true)
                .indent("    ").build();
        return javaFile;
    }
}
