package com.eluke;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class Writable {

	public abstract void write(DataOutputStream out) throws IOException;
	public abstract void read(DataInputStream in) throws IOException;

	protected void writeList(List<? extends Writable> list, DataOutputStream out) throws IOException {
		out.writeInt(list.size());
		for (Writable w : list) {
			w.write(out);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void readList(List list, Class<? extends Writable> theClass, DataInputStream in) throws IOException {
		list.clear();
		int num = in.readInt();
		try {
			for ( int i = 0; i < num; i++) {
				Writable w = theClass.newInstance();
				w.read(in);
				list.add(w);
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	protected void writeMap(Map<? extends Writable,? extends Writable> map, DataOutputStream out) throws IOException {
		out.writeInt(map.keySet().size());
		for (Entry<? extends Writable,? extends Writable> entry : map.entrySet()) {
			entry.getKey().write(out);
			entry.getValue().write(out);
		}
	}
	
	

	@SuppressWarnings("unchecked")
	protected void readMap(Map map, Class<? extends Writable> keyClass, Class<? extends Writable> valueClass, DataInputStream in) throws IOException {
		map.clear();
		int num = in.readInt();
		try {
			for ( int i = 0; i < num; i++) {
				Writable key = keyClass.newInstance();
				key.read(in);
				Writable value = valueClass.newInstance();
				value.read(in);
				map.put(key, value);
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
