import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Node {
	private List<Key> keys; //Pointer to Keys
	
	private List<Node> children; //Pointer to Children

	private Node prev; //Pointer to Previous Node

	private Node next; //Pointer to Next Node

	private Node parent; //Pointer to Parent Node
	
	public boolean isLeaf; //Indicate if it's a leaf node

	// Initialize Node
	public Node() {
		this.keys = new ArrayList<>();
		this.children = new ArrayList<>();
		this.prev = null;
		this.next = null;
	}

	// Return Array List of Keys
	public List<Key> getKeys() {
		return keys;
	}

	// Set Keys of the Node
	public void setKeys(List<Key> keys) {
		Iterator<Key> iter = keys.iterator();
		while (iter.hasNext()) {
			this.keys.add(iter.next());
		}
	}

	// Get List of Children Node
	public List<Node> getChildren() {
		return children;
	}

	// Set Children of Node
	public void setChildren(List<Node> children) {
		this.children = children;
	}

	// Get Next Node
	public Node getNext() {
		return next;
	}

	// Set Next Node
	public void setNext(Node next) {
		this.next = next;
	}

	// Get Parent Node
	public Node getParent() {
		return parent;
	}

	// Set Parent Node
	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	// Get Previous Node
	public Node getPrev() {
		return prev;
	}

	// Set Previous Node
	public void setPrev(Node prev) {
		this.prev = prev;
	}
		
	// Return Node Information
	@Override
	public String toString() {
		return "Keys: " + keys.toString();
	}
}
