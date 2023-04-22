package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author EnzoGuang
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int size;
    private int initialSize;
    private double loadFactor;

    /** Constructors */
    public MyHashMap() {
        this.size = 0;
        this.initialSize = 16;
        this.loadFactor = 0.75;
        this.buckets = createTable(this.initialSize);
    }

    public MyHashMap(int initialSize) {
        this.size = 0;
        this.initialSize = initialSize;
        this.loadFactor = 0.75;
        this.buckets = createTable(this.initialSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.size = 0;
        this.initialSize = initialSize;
        this.loadFactor = maxLoad;
        this.buckets = createTable(this.initialSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] temp = new Collection[tableSize];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = createBucket();
        }
        return temp;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!
    @Override
    public void clear() {
        buckets = createTable(this.initialSize);
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        if (size == 0) {
            return false;
        }
        int hash = hash(key);
        for (Node temp: buckets[hash]) {
            if (temp.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        if (size == 0) {
            return null;
        }
        if (containsKey(key)) {
            int hash = hash(key);
            for (Node temp: buckets[hash]) {
                if (temp.key.equals(key)) {
                    return temp.value;
                }
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    private int hash(K key) {
        return (key.hashCode() & 0x7fffffff) % initialSize;
    }
    @Override
    public void put(K key, V value) {
        int hash = hash(key);
        if (containsKey(key)) {
            for (Node temp: buckets[hash]) {
                if (temp.key.equals(key)) {
                    temp.value = value;
                }
            }
        }else {
            size++;
            buckets[hash].add(createNode(key, value));
        }
    }

    @Override
    public Set<K> keySet() {
        HashSet<K> keys = new HashSet<>();
        for (int i = 0; i < buckets.length; i++) {
            for (Node temp: buckets[i]) {
                keys.add(temp.key);
            }
        }
        return keys;
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        return new IteratorMyHashMap<>();
    }

    private class IteratorMyHashMap<K> implements Iterator<K> {
        int currentSize;


        public IteratorMyHashMap() {
            this.currentSize = 0;
        }

        @Override
        public boolean hasNext() {
            return currentSize < size;
        }

        @Override
        public K next() {
            return null;
        }
    }

}
