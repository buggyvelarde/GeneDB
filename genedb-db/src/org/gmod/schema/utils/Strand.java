package org.gmod.schema.utils;

public enum Strand {
	
	FORWARD((short)1), UNKNOWN((short)0), REVERSE((short)-1);
	
	short value;
	
	Strand(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
}
