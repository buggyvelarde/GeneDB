package org.genedb.install;

interface DbServer extends Server {
    void loadSchema()
    void loadBootstrapData()
}