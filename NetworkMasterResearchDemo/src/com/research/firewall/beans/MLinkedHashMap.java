package com.research.firewall.beans;

import java.util.LinkedHashMap;

public class MLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    private int a;
    private MVPNInterface vpnInterface;

    public MLinkedHashMap(int i, MVPNInterface cVar) {
        super(i + 1, 1.0f, true);
        this.a = i;
        this.vpnInterface = cVar;
    }

    protected final boolean removeEldestEntry(Entry<K, V> entry) {
        if (size() <= this.a) {
            return false;
        }
        this.vpnInterface.cleanup(entry);
        return true;
    }
}