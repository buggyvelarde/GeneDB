

List orgs = ["Hello", "World", "Wibble"]

switch (args[1]) {
case "Lucene":
    template="My name is ${org}";
    break;
case "DTO":
    template=""
    break
default:
    throw new RuntimeException("Don't know how to run '${args[1]}'")
}


for (org in orgs) {
    println $template
}