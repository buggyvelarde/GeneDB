package org.genedb.web.mvc.model;


public class MergeBerkeleyIndices {

    public static void main(String[] args) {

        BerkeleyMapFactory source = new BerkeleyMapFactory();
        source.setRootDirectory(args[0]);
        source.setReadOnly(true);

        BerkeleyMapFactory destination = new BerkeleyMapFactory();
        destination.setRootDirectory(args[1]);
        destination.setReadOnly(false);

        destination.getDtoMap().putAll(source.getDtoMap());
        destination.getContextMapMap().putAll(source.getContextMapMap());

        destination.closeDb();
        source.closeDb();

    }

}
