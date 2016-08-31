package com.research.firewall.beans;

import java.util.Map.Entry;

public interface MVPNInterface <K, V> {
    void cleanup(Entry<K, V> entry);
}