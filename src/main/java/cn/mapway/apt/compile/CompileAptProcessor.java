package cn.mapway.apt.compile;


import cn.mapway.apt.compile.util.CompileSourceUtil;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

/**
 * CompileAptProcessor
 * 提供编译信息的插件
 *
 * @author zhang
 */


@AutoService({Processor.class})
@SupportedSourceVersion(SourceVersion.RELEASE_5)
public class CompileAptProcessor extends AbstractProcessor {
    Filer filer;
    Messager messager;
    boolean hasProcessed = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(CompileInfoProvider.class);

        for (Element element : elementsAnnotatedWith) {
            if (hasProcessed) {
                return true;
            }
            hasProcessed = true;
            TypeElement typeElement = (TypeElement) element;
            JavaFile javaFile = CompileSourceUtil.compileSource(typeElement);
            try {
                javaFile.writeTo(filer);
                messager.printMessage(Diagnostic.Kind.WARNING, "compileInfo has processed");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }
        return true;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of("cn.mapway.apt.compile.CompileInfoProvider");
    }
}
