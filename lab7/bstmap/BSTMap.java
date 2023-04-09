/** A map that the underlying data structure is binary search tree.
 * @author EnzoGuang
 * @source https://algs4.cs.princeton.edu/32bst/BST.java.html
 * date 2023/04/09
 */
package bstmap;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    public Node root;


    private class Node<K, V> {
        private Node left;
        private Node right;
        private K key;
        private V value;
        private int size;

        public Node() {
            this.left = null;
            this.right = null;
            this.key = null;
            this.value = null;
            this.size = 0;
        }

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
            this.size = 1;
        }
    }

    public BSTMap() {
        this.root = null;
    }

    public BSTMap(K key, V value) {
        root = new Node(key, value);
    }

    /** Remove all of the mappings from this map. */
    @Override
    public void clear() {
        if (root == null) {
            return;
        }
        root = null;
    }

    /** Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("argument to containsKey() is null");
        }
        return containsKey(key, root) != null;
    }

    private Node containsKey(K key, Node n) {
        if (n == null) {
            return null;
        }
        Node result = n;
        int cmp = key.compareTo((K) n.key);
        if (cmp < 0) {
            result = containsKey(key, n.left);
        } else if(cmp > 0) {
            result = containsKey(key, n.right);
        } else {
            result = n;
        }
        return result;
    }


    /** Return the value to which the specified key is mapped, or null if the
     * map contains no mapping for this key./
     */
    @Override
    public V get(K key) {
       return get(root, key);
    }

    private V get(Node x, K key) {
        if (key == null) {
            throw new IllegalArgumentException("the parameter key is null");
        }
        if (x == null) {
            return null;
        }
        int cmp = key.compareTo((K) x.key);
        if (cmp < 0) {
            return (V) get(x.left, key);
        } else if(cmp > 0) {
            return (V) get(x.right, key);
        } else {
            return (V) x.value;
        }
    }

    /** Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return size(root);
    }

    private int size(Node x) {
        if (x == null) {
            return 0;
        }
        return x.size;
    }

    /** Associates the specified value with the specified key in this map. */
    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        root = put(key, value, root);
    }

    private Node put(K key, V value, Node n) {
        if (n == null) {
            root = new Node(key, value);
            return root;
        }
        int cmp = key.compareTo((K) n.key);
        if (cmp < 0) {
            n.left = put(key, value, n.left);
        } else if(cmp > 0) {
            n.right = put(key, value, n.right);
        } else {
            n.value = value;
        }
        n.size = 1 + size(n.left) + size(n.right);
        return n;
    }

    public  void printInOrder(Node x) {
        if (x == null) {
            return;
        }
        printInOrder(x.left);
        System.out.println("key: " + x.key + "\tvalue: " + x.value + "\n");
        printInOrder(x.right);
    }


    /** Returns a Set view of the keys contained in this map.
     *  But current is not implemented
     */
    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    /** Not been implemented. */
    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    /** Not been implemented. */
    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    /** Not been implemented. */
    @Override
    public Iterator<K> iterator() {
        return null;
    }
}
