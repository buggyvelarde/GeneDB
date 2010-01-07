package org.genedb.query.generation;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementScanner6;


@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("org.genedb.querying.*")
public class QueryAnnotationProcessor extends AbstractProcessor {

//    private Trees trees;
//    private TreePathScanner<Object, Trees> visitor = new QueryTreeVisitor();

//    @Override
//    public void init(ProcessingEnvironment pe) {
//            super.init(pe);
//            trees = Trees.instance(pe);
//    }


        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

            // Utils
            //Types typeUtils = processingEnv.getTypeUtils();
            //Elements elementUtils = processingEnv.getElementUtils();
            //Messager messager = processingEnv.getMessager();

            // The Serializable interface - used for comparison
            //TypeMirror query = processingEnv.getElementUtils().getTypeElement(Query.class.getCanonicalName()).asType();

            Set<? extends Element> rootElements = roundEnv.getRootElements();
            for (Element element : rootElements){

//                TreePath tp = trees.getPath(element);
                // invoke the scanner
//                visitor.scan(tp, trees);

            }

            // Prevent other processors from processing this annotation
            return true;
        }
    }



//class QueryTreeVisitor extends ElementScanner6<Object>  {
//
//    @Override
//    public Object visitClass(ClassTree classTree, Trees trees) {
//        return super.visitClass(classTree, trees);
//    }
//
//    @Override
//    public Object visitMethod(MethodTree methodTree, Trees trees) {
//
//        return super.visitMethod(methodTree, trees);
//    }
//}

