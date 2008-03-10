package org.genedb.install;

interface Server {
    void install(AntBuilder ant, String repository, String target, String port)
}
