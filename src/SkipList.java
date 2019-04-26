import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * An implementation of skip lists.
 */
public class SkipList<K, V> implements SimpleMap<K, V> {

  // +-----------+---------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The initial height of the skip list.
   */
  static final int INITIAL_HEIGHT = 16;

  // +---------------+-----------------------------------------------
  // | Static Fields |
  // +---------------+

  static Random rand = new Random();

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * Pointers to all the front elements.
   */
  ArrayList<SLNode<K, V>> front;

  /**
   * The comparator used to determine the ordering in the list.
   */
  Comparator<K> comparator;

  /**
   * The number of values in the list.
   */
  int size;

  /**
   * The current height of the SkipList.
   */
  int height;

  /**
   * The probability used to determine the height of nodes.
   */
  double prob = 0.5;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new skip list that orders values using the specified comparator.
   */
  public SkipList(Comparator<K> comparator) {
    this.front = new ArrayList<SLNode<K, V>>(INITIAL_HEIGHT);
    for (int i = 0; i < INITIAL_HEIGHT; i++) {
      front.add(null);
    } // for
    this.comparator = comparator;
    this.size = 0;
    this.height = INITIAL_HEIGHT;
  } // SkipList(Comparator<K>)

  /**
   * Create a new skip list that orders values using a not-very-clever default comparator.
   */
  public SkipList() {
    this((k1, k2) -> k1.toString().compareTo(k2.toString()));
  } // SkipList()

  // +-------------------+-------------------------------------------
  // | SkipList methods |
  // +-------------------+

  public V set(K key, V value) {
    if (key == null) {
      throw new NullPointerException("null key");
    }

    SLNode<K, V> node = new SLNode<>(null, null, this.height);
    node.next = this.front;

    /* initialize update */
//    ArrayList<SLNode<K, V>> update = new ArrayList<>(height);
//    for (int i = 0; i < height; i++)
//      update.add(null);
    SLNode<K, V>[] update = (SLNode<K, V>[]) new SLNode[this.height];


    for (int i = this.height - 1; i >= 0; i--) {
      while (node.next.get(i) != null && this.comparator.compare(node.next.get(i).key, key) < 0) {
        node = node.next.get(i);
      }
      //update.set(i, node);
      update[i] = node;
    } // for
    node = node.next.get(0);

    /* existing value in the list */
    if (node != null && this.comparator.compare(node.key, key) == 0) {
      V previousValue = node.value;
      node.value = value;
      return previousValue;
    }

    /* insert new node */
    int newHeight = randomHeight();
    SLNode<K, V> newNode = new SLNode<>(key, value, newHeight);

    /* if new node exceed current max height */
    if (newHeight > this.height) {
      SLNode<K, V> header = new SLNode<>(null, null, this.height); //dummy node
      header.next = this.front;

      SLNode<K, V>[] newUpdate = (SLNode<K, V>[]) new SLNode[newHeight];
      for (int i = 0; i < this.height; i++) {
        newUpdate[i] = update[i];
      }

      for (int i = this.height; i < newHeight; i++) {
        newUpdate[i] = header;
        front.add(null);
      }
      update = newUpdate;

      this.height = newHeight;
    } // if

    for (int i = 0; i < newHeight; i++) {
//      newNode.next.set(i, update.get(i).next.get(i));
//      update.get(i).next.set(i, newNode);
      newNode.next.set(i, update[i].next.get(i));
      update[i].next.set(i, newNode);
    } // for

    this.size += 1;
    return null;
  } // set(K,V)

  public V get(K key) {
    if (key == null) {
      throw new NullPointerException("null key");
    }

    SLNode<K, V> node = new SLNode<>(null, null, this.height);
    node.next = this.front;

    for (int i = this.height - 1; i >= 0; i--) {
      while (node.next.get(i) != null && this.comparator.compare(node.next.get(i).key, key) < 0)
        node = node.next.get(i);
    } // for
    node = node.next.get(0);

    if (node != null && this.comparator.compare(node.key, key) == 0) {
      /* search hit */
      return node.value;
    }

    /* throw exception when search miss*/
    throw new IndexOutOfBoundsException("key not found");
  } // get(K,V)

  public int size() {
    return this.size;
  } // size()

  public boolean containsKey(K key) {
    try {
      get(key);
    } catch (Exception IndexOutOfBoundsException) {
      return false;
    }
    return true;
  } // containsKey(K)

  public V remove(K key) {
    if (key == null)
      throw new NullPointerException("null key");

    SLNode<K, V> node = new SLNode<>(null, null, this.height);
    node.next = this.front;

    ArrayList<SLNode<K, V>> update = new ArrayList<>(height);
    for (int i = 0; i < height; i++) {
      update.add(null);
    }

    for (int i = this.height - 1; i >= 0; i--) {
      while (node.next.get(i) != null && this.comparator.compare(node.next.get(i).key, key) < 0) {
        node = node.next.get(i);
      }
      update.set(i, node);
    } // for
    node = node.next.get(0);

    if (node != null && this.comparator.compare(node.key, key) == 0) {
      for (int i = 0; i < this.height; i++) {
        if (update.get(i).next.get(i) != node) {
          break;
        }
        update.get(i).next.set(i, node.next.get(i));
      } // for

      while (this.height > 0 && this.front.get(this.height - 1) == null) {
        this.height--;
      }

      this.size -= 1;
      return node.value;
    } // if

    return null;
  } // remove(K)

  public Iterator<K> keys() {
    return new Iterator<K>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      public K next() {
        return nit.next().key;
      } // next()

      public void remove() {
        nit.remove();
      } // remove()
    };
  } // keys()

  public Iterator<V> values() {
    return new Iterator<V>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      public V next() {
        return nit.next().value;
      } // next()

      public void remove() {
        nit.remove();
      } // remove()
    };
  } // values()

  public void forEach(BiConsumer<? super K, ? super V> action) {
    SLNode<K, V> x = new SLNode<K, V>(null, null, this.height);
    x.next = this.front;

    x = x.next.get(0);
    while (x != null) {
      action.accept(x.key, x.value);
      x = x.next.get(0);
    } // while
  } // forEach

  // +----------------------+----------------------------------------
  // | Other public methods |
  // +----------------------+

  /**
   * Dump the tree to some output location.
   */
  public void dump(PrintWriter pen) {
    SLNode<K, V> x = new SLNode<K, V>(null, null, this.height);
    x.next = this.front;

    pen.println("this.front -> ");
    x.printNext();

    x = x.next.get(0);
    while (x != null) {
      pen.println("At [" + x.key + ", " + x.value + "] : ");
      x.printNext();
      x = x.next.get(0);
    } // while
  } // dump(PrintWriter)

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Pick a random height for a new node.
   */
  int randomHeight() {
    int result = 1;
    while (rand.nextDouble() < prob) {
      result = result + 1;
    }
    return result;
  } // randomHeight()

  /**
   * Get an iterator for all of the nodes. (Useful for implementing the other iterators.)
   */
  Iterator<SLNode<K, V>> nodes() {
    return new Iterator<SLNode<K, V>>() {

      /**
       * A reference to the next node to return.
       */
      SLNode<K, V> next = SkipList.this.front.get(0);

      @Override public boolean hasNext() {
        return this.next != null;
      } // hasNext()

      @Override public SLNode<K, V> next() {
        if (this.next == null) {
          throw new IllegalStateException();
        }
        SLNode<K, V> temp = this.next;
        this.next = this.next.next.get(0);
        return temp;
      } // next();
    }; // new Iterator
  } // nodes()

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

} // class SkipList


/**
 * Nodes in the skip list.
 */
class SLNode<K, V> {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The key.
   */
  K key;

  /**
   * The value.
   */
  V value;

  /**
   * Pointers to the next nodes.
   */
  ArrayList<SLNode<K, V>> next;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new node of height n with the specified key and value.
   */
  public SLNode(K key, V value, int n) {
    this.key = key;
    this.value = value;
    this.next = new ArrayList<SLNode<K, V>>(n);
    for (int i = 0; i < n; i++) {
      this.next.add(null);
    } // for
  } // SLNode(K, V, int)

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+

  public void printNext() {
    for (int i = this.next.size() - 1; i >= 0; i--) {
      if (this.next.get(i) == null)
        System.out.println("Height = " + (i + 1) + " -> null");
      else
        System.out.println(
            "Height = " + (i + 1) + " -> [" + this.next.get(i).key + ", " + this.next.get(i).value
                + "]");
    } // for
  } // printNext

} // SLNode<K,V>
