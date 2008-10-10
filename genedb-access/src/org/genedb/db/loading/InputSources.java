package org.genedb.db.loading;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

public class InputSources {
    File referenceDirectory;
    List<String> files;

    public File getReferenceDirectory() {
        return referenceDirectory;
    }
    public void setReferenceDirectory(File referenceDirectory) {
        this.referenceDirectory = referenceDirectory;
    }
    public void setFiles(List<String> files) {
        this.files = files;
    }

    public List<File> inputFiles() {
        List<File> ret = Lists.newArrayListWithExpectedSize(files.size());
        for (String fileName : files) {
            ret.add(new File(referenceDirectory, fileName));
        }
        return ret;
    }

}
