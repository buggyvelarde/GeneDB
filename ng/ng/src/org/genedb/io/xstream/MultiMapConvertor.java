package org.genedb.io.xstream;

import com.google.common.collect.Multimap;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@SuppressWarnings("unchecked")
public class MultiMapConvertor implements Converter {

	public boolean canConvert(Class clazz) {
		return clazz.equals(Multimap.class);
	}

	public void marshal(Object value, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		Multimap map = (Multimap) value;
		writer.startNode("map");
		for (Object key : map.keys()) {
			writer.startNode("entry");
			writer.addAttribute("key", key.toString());
			for (Object v : map.get(key)) {
				writer.startNode("value");
				writer.setValue(v.toString());
				writer.endNode();
			}
			writer.endNode();
		}
		writer.endNode();
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return null;
	}

}
