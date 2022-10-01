import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BPTree {

	// B+ Tree Info
	private int maxPointer;
	private Node rootNode;
	private int height = 0;

	// For Experiment
	private int numNodes = 0;
	private int numNodesDeleted = 0;
	private int numNodesMerged = 0;
	private int indexNodesAccess = 0;
	private int dataBlocksAccess = 0;
	private int uniqueKeysCount = 1;
	private int nodeRecordCount = 0;
	private int totalRecordsCount = 0;

	public BPTree(int order) {
		this.maxPointer = order;
		this.rootNode = null;
	}

	// 1: Insertion Functions
	public void insertKey(int key, Record value) {
		// 1: Empty B+ Tree => Create New Root Node
		if (null == this.rootNode) {
			Node newNode = new Node();
			newNode.getKeys().add(new Key(key, value));
			this.rootNode = newNode;
			this.rootNode.setParent(null);
			this.rootNode.isLeaf = true;
		} else if (this.rootNode.getChildren().isEmpty() && this.rootNode.getKeys().size() < (this.maxPointer - 1)) {
			// 2: Node is Not Full
			this.rootNode.isLeaf = false;
			insertLeafNode(key, value, this.rootNode);
		} else {
			// 3: Normal insert
			Node current = this.rootNode;

			// Traverse to leaf node
			while (!current.getChildren().isEmpty()) {
				current = current.getChildren().get(searchInternalNode(key, current.getKeys()));
			}

			insertLeafNode(key, value, current);

			// External node is full => Split node
			if (current.getKeys().size() == this.maxPointer) {
				splitLeafNode(current, this.maxPointer);
			}
		}

		// Increase number of nodes value
		numNodes++;

	}

	private void insertLeafNode(int key, Record value, Node node) {
		// Find index to insert
		int index = searchInternalNode(key, node.getKeys());

		if (index != 0 && node.getKeys().get(index - 1).getKey() == key) {
			// Add the new value to the list
			node.getKeys().get(index - 1).getRecords().add(value);
		} else {
			// Key is null => Add key and data
			Key newKey = new Key(key, value);
			node.getKeys().add(index, newKey);

			node.isLeaf = true; // Set Key isLeaf value
		}
	}

	private void splitLeafNode(Node current, int maxPointer) {

		// Set isLeaf of External Node
		current.isLeaf = true;

		// Find the middle index
		int midIndex = maxPointer / 2;

		Node newParentNode = new Node();
		Node rightNode = new Node();

		// Internal nodes do not contain records => Set only Keys
		newParentNode.getKeys().add(new Key(current.getKeys().get(midIndex).getKey()));
		newParentNode.getChildren().add(rightNode);

		// Shift elements from midIndex onwards to rightNode
		rightNode.setKeys(current.getKeys().subList(midIndex, current.getKeys().size()));
		rightNode.setParent(newParentNode);

		// Update current node to contain only elements before midIndex
		current.getKeys().subList(midIndex, current.getKeys().size()).clear();

		boolean leaf = true;
		splitInternalNode(current.getParent(), current, maxPointer, newParentNode, leaf);

	}

	private void splitInternalNode(Node current, Node childNode, int maxPointer, Node insertedNode, boolean leaf) {

		// If currentent node is null
		if (current == null) {
			// Set new rootNode
			this.rootNode = insertedNode;

			// Find where the child has to be inserted
			int prevIndex = searchInternalNode(childNode.getKeys().get(0).getKey(), insertedNode.getKeys());
			childNode.setParent(insertedNode);
			insertedNode.getChildren().add(prevIndex, childNode);
			if (leaf) {
				// Update the linked list only for first split (for external node)
				if (prevIndex == 0) {
					insertedNode.getChildren().get(0).setNext(insertedNode.getChildren().get(1));
					insertedNode.getChildren().get(1).setPrev(insertedNode.getChildren().get(0));
				} else {
					insertedNode.getChildren().get(prevIndex + 1).setPrev(insertedNode.getChildren().get(prevIndex));
					insertedNode.getChildren().get(prevIndex - 1).setNext(insertedNode.getChildren().get(prevIndex));
				}
			}
		} else {
			// Merge the internal node with the mid + right of previous split
			mergeInternalNodes(insertedNode, current);

			// Split if internal node is full
			if (current.getKeys().size() == maxPointer) {
				int midIndex = (int) Math.ceil(maxPointer / 2.0) - 1;
				Node newParentNode = new Node();
				Node rightNode = new Node();

				newParentNode.getKeys().add(current.getKeys().get(midIndex));
				newParentNode.getChildren().add(rightNode);

				rightNode.setKeys(current.getKeys().subList(midIndex + 1, current.getKeys().size()));
				rightNode.setParent(newParentNode);

				List<Node> currentChildren = current.getChildren();
				List<Node> rightChildren = new ArrayList<>();

				int leftChild = currentChildren.size() - 1;

				// Shift children to rightNode
				for (int i = currentChildren.size() - 1; i >= 0; i--) {
					List<Key> currentKeysList = currentChildren.get(i).getKeys();
					if (newParentNode.getKeys().get(0).getKey() <= currentKeysList.get(0).getKey()) {
						currentChildren.get(i).setParent(rightNode);
						rightChildren.add(0, currentChildren.get(i));
						leftChild--;
					} else {
						break;
					}
				}

				rightNode.setChildren(rightChildren);

				// Update the node to contain just the left part and its children
				current.getChildren().subList(leftChild + 1, currentChildren.size()).clear();
				current.getKeys().subList(midIndex, current.getKeys().size()).clear();

				splitInternalNode(current.getParent(), current, maxPointer, newParentNode, false);
			}
		}
	}

	private void mergeInternalNodes(Node srcNode, Node destNode) {
		Key keyFromInserted = srcNode.getKeys().get(0);
		Node childFromInserted = srcNode.getChildren().get(0);

		// Find index to insert
		int indexToInsert = searchInternalNode(keyFromInserted.getKey(), destNode.getKeys());
		int childInsertPos = indexToInsert;
		if (keyFromInserted.getKey() <= childFromInserted.getKeys().get(0).getKey()) {
			childInsertPos = indexToInsert + 1;
		}

		childFromInserted.setParent(destNode);
		destNode.getChildren().add(childInsertPos, childFromInserted);
		destNode.getKeys().add(indexToInsert, keyFromInserted);

		// Update Linked List of external nodes
		if (!destNode.getChildren().isEmpty() && destNode.getChildren().get(0).getChildren().isEmpty()) {

			if (destNode.getChildren().size() - 1 != childInsertPos
					&& destNode.getChildren().get(childInsertPos + 1).getPrev() == null) {
				destNode.getChildren().get(childInsertPos + 1).setPrev(destNode.getChildren().get(childInsertPos));
				destNode.getChildren().get(childInsertPos).setNext(destNode.getChildren().get(childInsertPos + 1));
			} else if (0 != childInsertPos && destNode.getChildren().get(childInsertPos - 1).getNext() == null) {
				destNode.getChildren().get(childInsertPos).setPrev(destNode.getChildren().get(childInsertPos - 1));
				destNode.getChildren().get(childInsertPos - 1).setNext(destNode.getChildren().get(childInsertPos));
			} else {
				// Merge is in between, then the next and the previous element's prev and next
				// pointers have to be updated
				destNode.getChildren().get(childInsertPos)
						.setNext(destNode.getChildren().get(childInsertPos - 1).getNext());
				destNode.getChildren().get(childInsertPos).getNext().setPrev(destNode.getChildren().get(childInsertPos));
				destNode.getChildren().get(childInsertPos - 1).setNext(destNode.getChildren().get(childInsertPos));
				destNode.getChildren().get(childInsertPos).setPrev(destNode.getChildren().get(childInsertPos - 1));
			}
		}

	}

	// 2: Search Functions
	public int searchInternalNode(int key, List<Key> keys) {
		int startIndex = 0;
		int endIndex = keys.size() - 1;
		int mid;
		int index = -1;

		// Return first index if key is less than the first element
		if (key < keys.get(startIndex).getKey()) {
			return 0;
		}

		// If key greater than last key
		if (key >= keys.get(endIndex).getKey()) {
			return keys.size();
		}

		while (startIndex <= endIndex) {

			// Get mid index
			mid = (startIndex + endIndex) / 2;

			// Find index of key < index key and >= than previous index key
			if (key < keys.get(mid).getKey() && key >= keys.get(mid - 1).getKey()) {
				index = mid;
				break;
			} else if (key >= keys.get(mid).getKey()) {
				startIndex = mid + 1;
			} else {
				endIndex = mid - 1;
			}
		}
		return index;
	}

	public List<Record> search(int key) {

		// Set access numbers to 0
		dataBlocksAccess = 0;
		indexNodesAccess = 0;

		List<Record> recordList = null;

		Node current = this.rootNode;
		indexNodesAccess++;
		System.out.println("Index Node Access: Node= " + current.getKeys());

		// Traverse to the corresponding external node that would contain this key
		while (current.getChildren().size() != 0) {
			current = current.getChildren().get(searchInternalNode(key, current.getKeys()));
			indexNodesAccess++;
			System.out.println("Index Node Access: Node= " + current.getKeys());
		}

		List<Key> keyList = current.getKeys();

		// Do a linear search in this node for the key
		for (int i = 0; i < keyList.size(); i++) {

			// dataBlocksAccess++;

			if (key == keyList.get(i).getKey()) {

				System.out.println("Data Block Access: Key=" + keyList.get(i).getKey());
				// System.out.println("Number of records=" + keyList.get(i).getRecords().size() + " Records");
				// System.out.println("Value (0)=" + keyList.get(i).getRecords().get(0));
				dataBlocksAccess++;

				recordList = keyList.get(i).getRecords();

			}
			if (key < keyList.get(i).getKey()) {
				break;
			}
		}

		return recordList;
	}

	public List<Key> search(int minKey, int maxKey) {

		// Set access numbers to 0
		indexNodesAccess = 0;
		dataBlocksAccess = 0;
		List<Key> searchs = new ArrayList<>();
		Node current = this.rootNode;

		indexNodesAccess++;
		System.out.println("Index Node Access: Node= " + current.getKeys());

		while (current.getChildren().size() != 0) {
			indexNodesAccess++;
			current = current.getChildren().get(searchInternalNode(minKey, current.getKeys()));
			System.out.println("Index Node Access: Node= " + current.getKeys());
		}

		// Stop if value encountered in list is greater than key2
		boolean endSearch = false;

		while (current != null && !endSearch) {
			for (int i = 0; i < current.getKeys().size(); i++) {
				if(current.getKeys().get(i).getKey() >= minKey && current.getKeys().get(i).getKey() <= maxKey){
					dataBlocksAccess++;
//					System.out.println("Data Block Access: Key=" + current.getKeys().get(i).getKey()
//							+ " |\n Value=" + current.getKeys().get(i).getRecords());
//
//					System.out.println("Data Block Access: Key= " + current.getKeys().get(i).getKey());
//					System.out.println("Value Size= " + current.getKeys().get(i).getRecords().size() + " Records");
//					System.out.println("Value (0)= " + current.getKeys().get(i).getRecords().get(0));
				}


				if (current.getKeys().get(i).getKey() >= minKey && current.getKeys().get(i).getKey() <= maxKey)
					searchs.add(current.getKeys().get(i));
				if (current.getKeys().get(i).getKey() > maxKey) {
					endSearch = true;
				}
			}
			current = current.getNext();
		}

		return searchs;
	}
	
	public int countNumberOfIndexNodes() {
		int countIndexNodes = 0;
		
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(this.rootNode);
		queue.add(null);
		Node current = null;
		
		while (!queue.isEmpty()) {
			current = queue.poll();
			if (current == null) {
				queue.add(null);
				if (queue.peek() == null) {
					break;
				}
				continue;
			}

			countIndexNodes++;
			
			if (current.getChildren().isEmpty()) {
				break;
			}
			for (int i = 0; i < current.getChildren().size(); i++) {
				queue.add(current.getChildren().get(i));
			}
		}

		current = current.getNext();
		while (current != null) {
			countIndexNodes++;
			current = current.getNext();
		}
		return countIndexNodes;
	}

	
	// 3: Deletion Functions
//	// Test 1: Remove Key
//	public void removeKey(int key) {
//
//		// Reset
//		numNodesDeleted = 0;
//		numNodesMerged = 0;
//
//		Node current = this.rootNode;
//
//		// Minimum number of keys to balance the b-tree in the leaf node
//		int minLeafKeys = (int) Math.floor(maxPointer / 2.0);
//		System.out.println("Minimum number of keys in the leaf node: " + minLeafKeys);
//
//		// Minimum number of keys to balance the b-tree in the non-leaf node
//		int minInternalKeys = (int) Math.floor((maxPointer - 1) / 2.0);
//		System.out.println("Minimum number of keys in the non-leaf node: " + minInternalKeys);
//
//		// Minimum number of children node to balance the b-tree in the non-leaf node
//		int minInternalNodeChildren = (int) Math.ceil((maxPointer) / 2.0);
//		System.out.println("Minimum number of children in the non-leaf node: " + minInternalNodeChildren);
//
//		while (current.getChildren().size() != 0) {
//			current = current.getChildren().get(searchInternalNode(key, current.getKeys()));
//			System.out.println("current: " + current.getKeys());
//			System.out.println("current children: " + current.getChildren() + " | Size: " + current.getChildren().size());
//
//			List<Key> keys = current.getKeys();
//
//			if (current.isLeaf == false) {
//				System.out.println("=Internal=");
//				System.out.println("Here: " + keys + " | Size: " + keys.size());
//
//				System.out.println("Children: " + current.getChildren());
//
//				// If key is in non-leaf node, remove key
//				for (int i = 0; i < keys.size(); i++) {
//					if (keys.get(i).getKey() == key) {
//						System.out.println("Removed");
//						keys.remove(i);
//					}
//				}
//
//				// if key size is less than min. keys required in non leaf node
//				if (keys.size() < minInternalKeys) {
//					Node firstChildrenNode = current.getChildren().get(0);
//					System.out.println("Children Key: " + firstChildrenNode.getKeys());
//					current.setKeys(firstChildrenNode.getKeys());
//
//				}
//
//			} else {
//				System.out.println("=Leaf=");
//				System.out.println("Here: " + keys + " | Size: " + keys.size());
//
//				// If key is in leaf node, remove key
//				for (int i = 0; i < keys.size(); i++) {
//					if (keys.get(i).getKey() == key) {
//						keys.remove(i);
//					}
//				}
//
//				List<Node> nodeList = current.getParent().getChildren();
//
//				System.out.println("current: " + current.getParent().getChildren());
//				for (int i = 0; i < nodeList.size(); i++) {
//					if (nodeList.get(i).getKeys().size() == 0) {
//						nodeList.remove(i);
//					}
//				}
//
//				// 1: If node has less than ceil(maxPointer/2):
//				if (keys.size() < minLeafKeys) {
//					System.out.println("Not enough keys in node");
//					System.out.println("Parent: " + current.getParent());
//					System.out.println("Next Sibling: " + current.getNext());
//					System.out.println("Prev Sibling: " + current.getPrev());
//
//					// See if sibling can lend a key
//					// If can borrow, borrow key and adjust parent node keys
//					Node nextNode = current.getNext();
//					Node prevNode = current.getPrev();
//					Node parentNode = current.getParent();
//
//					// If cannot borrow, merge with sibling & adjust parent node keys
//
//					if (parentNode != null) {
//						System.out.println("Parent Children: " + current.getParent().getChildren());
//
//					}
//
//				} else {
//					// If smallest key is deleted push up the next key
//				}
//			}
//		}
//
//	}

	// Test 2: Remove Key
	public void deleteKey(int key) {

		// Reset
		numNodesDeleted = 0;
		numNodesMerged = 0;

		Node current = this.rootNode;
		int maxKeys = maxPointer-1;
		// Minimum number of keys to balance the b-tree in the leaf node
		int minLeafKeys = (int) Math.floor( (maxKeys+1) / 2.0);
		System.out.println("Minimum number of keys in the leaf node: " + minLeafKeys);

		// Minimum number of keys to balance the b-tree in the non-leaf node
		int minInternalKeys = (int) Math.floor((maxKeys) / 2.0);
		System.out.println("Minimum number of keys in the non-leaf node: " + minInternalKeys);

		// Minimum number of children node to balance the b-tree in the non-leaf node
//		int minInternalNodeChildren = (int) Math.ceil((maxPointer) / 2.0);
		int minInternalNodeChildren = minInternalKeys+1;
		System.out.println("Minimum number of children in the non-leaf node: " + minInternalNodeChildren);

		/*
		 * if(current.getChildren().isEmpty()) { List<Key> keys = current.getKeys();
		 * 
		 * //If key is a rootNode node, remove key for(int i = 0; i < keys.size(); i++) {
		 * if(keys.get(i).getKey() == key) { System.out.println("Removed");
		 * keys.remove(i); } }
		 * 
		 * // if key size is more than 1 keys required in rootNode node if(keys.size() > 1)
		 * { return; } if(keys.size() == 1) }
		 */

		while (current.getChildren().size() != 0) {
			current = current.getChildren().get(searchInternalNode(key, current.getKeys()));
			System.out.println("current: " + current.getKeys());
			System.out.println("current children: " + current.getChildren() + " | Size: " + current.getChildren().size());

			List<Key> keys = current.getKeys();

			if (current.isLeaf == false) {
				System.out.println("=Internal=");
				System.out.println("Here: " + keys + " | Size: " + keys.size());

				System.out.println("Children: " + current.getChildren());

				// If key is in non-leaf node, remove key
				for (int i = 0; i < keys.size(); i++) {
					if (keys.get(i).getKey() == key) {
						System.out.println("Removed");
						keys.remove(i);
					}
				}

				// if key size is less than min. keys required in non leaf node
				if (keys.size() < minInternalKeys) {
					Node firstChildrenNode = current.getChildren().get(0);
					System.out.println("Children Key: " + firstChildrenNode.getKeys());
					current.setKeys(firstChildrenNode.getKeys());

				}

			} else {
				System.out.println("=Leaf=");
				System.out.println("Here: " + keys + " | Size: " + keys.size());

				int keyCount = 0;
				// If key is in leaf node, remove key
				int originalKeySize=keys.size();
				for (; keyCount < originalKeySize; keyCount++) {
					if (keys.get(keyCount).getKey() == key) {
						keys.remove(keyCount);
						break;
					}
				}
				if(keyCount == originalKeySize){
					System.out.println("Key does not exist");
					return;
				}

				List<Node> nodeList = current.getParent().getChildren();

				System.out.println("Updated children nodes: " + current.getParent().getChildren());
				for (int i = 0; i < nodeList.size(); i++) {
					if (nodeList.get(i).getKeys().size() == 0) {
						nodeList.remove(i);
					}
				}

				// 1: If node has less than ceil(maxPointer+1/2):
				if (keys.size() < minLeafKeys) {
					System.out.println("Not enough keys in node");
					System.out.println("Parent: " + current.getParent());
					System.out.println("Next Sibling: " + current.getNext());
					System.out.println("Prev Sibling: " + current.getPrev());

					// See if sibling can lend a key
					// If can borrow, borrow key and adjust parent node keys
					Node nextNode = current.getNext();
					Node prevNode = current.getPrev();
					Node parentNode = current.getParent();

					/*
					 * // If can borrow a key from left or right sibling, adjust keys in leaf node
					 * and its parent node //1) Check left sibling first, if cannot borrow //2)
					 * Check right sibling
					 * 
					 * int st = 0; int end = prevNode.getKeys().size() - 1;
					 * if(prevNode.getKeys().size() - 1 > minLeafKeys) { // Add the last element
					 * key of left sibling to currentent node keys.add(st,
					 * prevNode.getKeys().get(end));
					 * 
					 * // Update smallest key of currentent node to parent node for(int i = 0; i <
					 * parentNode.getKeys().size(); i++) { // Find the previous smallest key of
					 * currentent node in the parent node if(parentNode.getKeys().get(i).getKey() ==
					 * key) { // Get the new smallest key of currentent node at index position 0 and
					 * update the new smallest key in the parent node
					 * parentNode.getKeys().get(i).setKey(keys.get(st).getKey()); } } } else
					 * if(nextNode.getKeys().size() - 1 > minLeafKeys) { // Add the first
					 * element key of right sibling to currentent node
					 * keys.add(nextNode.getKeys().get(st)); }
					 */

					// If cannot borrow, merge with sibling & adjust parent node keys

					if (parentNode != null) {
						System.out.println("Parent Children: " + current.getParent().getChildren());
						int noChildren = current.getParent().getChildren().size();
						System.out.println("Number of children for parent node: " + noChildren);
						int count = 0;
						while (count < noChildren){
							if(prevNode != current.getParent().getChildren().get(count)){
								count++;
							}
						}
						boolean ableLeftBorrow = false;
						boolean ableRightBorrow = false;
						// Check if previous node is a sibling of the same children node
						if(count < noChildren){
							System.out.println("Previous node shares same parent");
							//check if possible to borrow key from previous sibling
							int start = 0;
							int end = prevNode.getKeys().size() - 1;
							if(prevNode.getKeys().size() - 1 > minLeafKeys) { // Add the last element//
								keys.add(start, prevNode.getKeys().get(end));
								prevNode.getKeys().remove(end);
								ableLeftBorrow = true;
							}
							else {
								//check if can borrow From right sibling
								ableRightBorrow = borrowRight(current, nextNode, parentNode, minLeafKeys, keys);
							}
						}
						// Check if able to borrow from right
						else{
							System.out.println("Previous node does not share same parent");
							//check if possible to borrow key from next sibling
							ableRightBorrow = borrowRight(current, nextNode, parentNode, minLeafKeys, keys);
						}

						//do deletion and merging if fail to borrow
						if(ableLeftBorrow == false && ableRightBorrow == false){
							/*
							insert logic for merging. Left node always merge with right node, so all keys and pointers will shift to left node
							Need to propagate changes to parent node
							 */
							numNodesDeleted++;
							numNodesMerged++;
						}

					}

				} else {
					// If smallest key is deleted push up the next key

					Node parentNode = current.getParent();
					for (int i = 0; i < parentNode.getKeys().size(); i++) {
						// Find the previous smallest key of currentent node in the parent node
						if (parentNode.getKeys().get(i).getKey() == key) {
							// Get the new smallest key of currentent node at index position 0 and update the
							// new smallest key in the parent node
							parentNode.getKeys().get(i).setKey(keys.get(0).getKey());
						}
					}
				}
			}
		}

	}

	public boolean borrowRight(Node current, Node nextNode, Node parentNode, int minLeafKeys, List<Key> keys){
		if(nextNode.getKeys().size()-1 >= minLeafKeys){
			System.out.println("Possible to borrow from next sibling");
			int prevNextNodeSmallestKey = nextNode.getKeys().get(0).getKey();
			System.out.println("Next sibling smallest key: "+nextNode.getKeys().get(0));
			keys.add(nextNode.getKeys().get(0));
			//shift all the keys in next sibling back by 1
			List<Key> nextNodeKeys = nextNode.getKeys();
			for(int i=0;i<nextNode.getKeys().size()-1;i++){
				nextNodeKeys.set(i, nextNode.getKeys().get(i+1));
			}
			//remove last element of next Node
			nextNodeKeys.remove(nextNode.getKeys().size()-1);
			System.out.println("Updated currentent node keys: " + keys);
			System.out.println("Updated right sibling node keys: " + nextNode.getKeys());
			//propagate changes to parent node
			int newSmallestNextNodeKey = nextNode.getKeys().get(0).getKey();
			System.out.println("Smallest Next Node Key: " + newSmallestNextNodeKey);
			for (int i = 0; i < parentNode.getKeys().size(); i++) {
				// Find the previous smallest key of currentent node in the parent node
				if (parentNode.getKeys().get(i).getKey() == prevNextNodeSmallestKey) {
					// Get the new smallest key of currentent node at index position 0 and update the
					// new smallest key in the parent node
					parentNode.getKeys().get(i).setKey(newSmallestNextNodeKey);
				}
			}
			System.out.println("Parent: " + current.getParent());
			return true;
		}
		else {
			System.out.println("Impossible to borrow from next sibling");
			//insert logic for deletion and merging
			return false;
		}
	}

	// 4: Display Functions
	public void displayUpdatedNodesInfo() {
		System.out.println("The total number of deleted nodes is " + numNodesDeleted);
		System.out.println("The total number of merged nodes is " + numNodesMerged);
	}

	public void displayNumNodes() {
		System.out.println("The total number of nodes is " + numNodes);
	}

	public void displayHeight() {
		System.out.println("The tree height is " + height);
	}

	public void displayTree() {
		// Reset all
		int numOfNodes = 0;
		totalRecordsCount = 0;
		nodeRecordCount = 0;
		height = 0;
		uniqueKeysCount = 1;

		Queue<Node> queue = new LinkedList<Node>();
		queue.add(this.rootNode);
		queue.add(null);
		Node current = null;
		int levelNumber = 2;
		System.out.println("Level 1 (Root)");
		while (!queue.isEmpty()) {
			current = queue.poll();
			if (current == null) {
				queue.add(null);
				if (queue.peek() == null) {
					break;
				}
				height = levelNumber;
				System.out.println("\n" + "Level " + levelNumber++);

				continue;
			}

			displayNode(current);
			numOfNodes++;

			if (current.getChildren().isEmpty()) {
				break;
			}
			for (int i = 0; i < current.getChildren().size(); i++) {
				queue.add(current.getChildren().get(i));
			}
		}

		current = current.getNext();
		while (current != null) {
			displayNode(current);
			numOfNodes++;
			current = current.getNext();
		}
		System.out.println("\nTotal number of nodes in B+ tree is: " + numOfNodes);
		System.out.println("Total number of records in B+ tree is: " + totalRecordsCount);
	}
	public void displayDeleteInfo(){
		displayUpdatedNodesInfo();

		int numOfNodes = 0;
		totalRecordsCount = 0;
		nodeRecordCount = 0;
		height = 0;
		uniqueKeysCount = 1;
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(this.rootNode);
		queue.add(null);
		Node current = null;
		int levelNumber = 2;
		System.out.println("Level 1 (Root)");
		int rootAndFirst = 0;
		while (!queue.isEmpty()) {
			current = queue.poll();
			if (current == null) {
				queue.add(null);
				if (queue.peek() == null) {
					break;
				}
				height = levelNumber;
				levelNumber++;
				if(rootAndFirst < 2) {
					System.out.println("\n" + "Level " + (levelNumber-1));
				}
				continue;
			}
			if(rootAndFirst == 0) {
				displayNode(current);
			}
			else if(rootAndFirst == 1) {
				System.out.println("First Child Node:");
				displayNode(current);
			}
			numOfNodes++;

			if (current.getChildren().isEmpty()) {
				break;
			}
			for (int i = 0; i < current.getChildren().size(); i++) {
				queue.add(current.getChildren().get(i));
			}
			rootAndFirst++;
		}

		current = current.getNext();
		while (current != null) {
//			displayNode(current);
			numOfNodes++;
			current = current.getNext();
		}
		System.out.println("\nTotal number of nodes in B+ tree is: " + numOfNodes);
		System.out.println("Total number of records in B+ tree is: " + totalRecordsCount);

		displayHeight();
	}

	public void displayPartialTree(){
		int numOfNodes = 0;
		totalRecordsCount = 0;
		nodeRecordCount = 0;
		height = 0;
		uniqueKeysCount = 1;
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(this.rootNode);
		queue.add(null);
		Node current = null;
		int levelNumber = 2;
		System.out.println("Level 1 (Root)");
		int rootAndFirst = 0;
		while (!queue.isEmpty()) {
			current = queue.poll();
			if (current == null) {
				queue.add(null);
				if (queue.peek() == null) {
					break;
				}
				height = levelNumber;
				levelNumber++;
				if(rootAndFirst < 2) {
					System.out.println("\n" + "Level " + (levelNumber-1));
				}
				continue;
			}
			if(rootAndFirst == 0) {
				displayNode(current);
			}
			else if(rootAndFirst == 1) {
				System.out.println("First Child Node:");
				displayNode(current);
			}
			numOfNodes++;

			if (current.getChildren().isEmpty()) {
				break;
			}
			for (int i = 0; i < current.getChildren().size(); i++) {
				queue.add(current.getChildren().get(i));
			}
			rootAndFirst++;
		}

		current = current.getNext();
		while (current != null) {
			numOfNodes++;
			current = current.getNext();
		}
		System.out.println("\nTotal number of nodes in B+ tree is: " + numOfNodes);
	}

	private void displayNode(Node current) {

		for (int i = 0; i < current.getKeys().size(); i++) {
			nodeRecordCount = 0;
			System.out.print(current.getKeys().get(i).getKey() + ":[");
			String records = "";
			
			for (int j = 0; j < current.getKeys().get(i).getRecords().size(); j++) {
				records = records + current.getKeys().get(i).getRecords().get(j) + ",";
				nodeRecordCount++;
			}

			totalRecordsCount += nodeRecordCount;
			System.out.print(records.isEmpty() ? "], " : nodeRecordCount + "], ");
		}

		if (current.getKeys().size() != 0) {
			System.out.print("\n");
		}

	}

	public void printIndexNodesAccessed() {
		System.out.println("Number of Index Nodes Access: " + indexNodesAccess);
	}

	public void printDataBlocksAccessed() {
		System.out.println("Number of Data Block Access: " + dataBlocksAccess);
	}
	
	public void printMaxKeysInNode() {
		System.out.println("Paramater n of Tree(Max Number of keys in a Node) " + (maxPointer - 1));
	}

	public void printNumberOfNodes() {
		System.out.println("Number of Nodes in Tree " + numNodes);
	}
	public void printRangeQueries(List<Key> search) {
		int x = 0;
		int dataBlocks = 0;
		// System.out.println(search.size());
		float totalAverageRating = 0;
		System.out.println("===================Part 2: Display Data Block ===================");
		for (int j = 0; j < search.size(); j++) {
			System.out.println("\nnumVotes -> [Key: " + search.get(j).getKey() + "]");
			dataBlocks++;
			for (int y = 0; y < search.get(j).getRecords().size(); y++) {
				x++;
				System.out.print("  tConst: ");
				System.out.print(search.get(j).getRecords().get(y).getTConst() + ",");
				System.out.print(" Average Rating: " + search.get(j).getRecords().get(y).getAverageRating() + "\n");
				totalAverageRating += search.get(j).getRecords().get(y).getAverageRating();
				if (y % 100 == 0 && y != 0) {
					System.out.print("\n");
				}

			}
			// System.out.print("\n");
		}

		System.out.println("\nTotal Data Blocks: " + dataBlocks);
		System.out.println("\n===================Part 3: Average of 'avgRatings' ===================");
		System.out.println("Total Records: " + x);
		System.out.printf("Average of avgRatings: %.1f\n\n",totalAverageRating/x);
	}

}
