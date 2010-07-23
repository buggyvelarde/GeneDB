<@javaSource name="net.sf.jelly.apt.examples.ClassAndMethodPrinter">
    package net.sf.jelly.apt.examples;

    public class ClassAndMethodPrinter {

        public static void main(String[] args) {
            <@forAllTypes var="type">
                System.out.println("${type.qualifiedName}");
                <@forAllMethods var="method">
                    System.out.println("${type.qualifiedName}.${method.simpleName}");
                </@forAllMethods>
            </@forAllTypes>
        }
    }
</@javaSource>
