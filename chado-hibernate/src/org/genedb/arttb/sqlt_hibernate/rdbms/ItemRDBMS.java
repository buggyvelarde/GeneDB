package org.genedb.arttb.sqlt_hibernate.rdbms;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.genedb.arttb.sqlt_hibernate.OutputStyle;

public interface ItemRDBMS {
	public void outputItem(OutputStyle os, PrintWriter out, File file) throws IOException;
}
