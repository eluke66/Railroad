package com.eluke.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class Utils {
	@SuppressWarnings("unchecked")
	public static <Key,Value extends Comparable> List<Key> SortMapByValues(Map<Key,Value> map, boolean descending) {
        List<Key> keySet = new ArrayList<Key>(map.keySet());
        final Map<Key,Value> finalMap = map;
        Comparator<Key> myComparator = new Comparator<Key>() {
            public int compare(Key o1, Key o2) {
                return finalMap.get(o1).compareTo(finalMap.get(o2));
            }
        };
        Collections.sort(keySet, myComparator);
        if ( descending ) {
            Collections.reverse(keySet);
        }
        return keySet;
    }
}
