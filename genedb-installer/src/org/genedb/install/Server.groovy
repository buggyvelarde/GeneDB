package org.genedb.install;

interface Server {
    void install(AntBuilder ant, String repository, String target, String port)
}

interface DbServer extends Server {
    void loadSchema()
    void loadBootstrapData()
}