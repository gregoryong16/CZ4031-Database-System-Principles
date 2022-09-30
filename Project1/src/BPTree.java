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
			Node curr = this.rootNode;

			// Traverse to leaf node
			while (!curr.getChildren().isEmpty()) {
				curr = curr.getChildren().get(searchInternalNode(key, curr.getKeys()));
			}

			insertLeafNode(key, value, curr);

			// External node is full => Split node
			if (curr.getKeys().size() == this.maxPointer) {
				splitLeafNode(curr, this.maxPointer);
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
			node.getKeys().get(index - 1).getValues().add(value);
		} else {
			// Key is null => Add key and data
			Key newKey = new Key(key, value);
			node.getKeys().add(index, newKey);

			node.isLeaf = true; // Set Key isLeaf value
		}
	}

	private void splitLeafNode(Node curr, int maxPointer) {

		// Set isLeaf of External Node
		curr.isLeaf = true;

		// Find the middle index
		int midIndex = maxPointer / 2;

		Node newParentNode = new Node();
		Node rightNode = new Node();

		// Internal nodes do not contain values => Set only Keys
		newParentNode.getKeys().add(new Key(curr.getKeys().get(midIndex).getKey()));
		newParentNode.getChildren().add(rightNode);

		// Shift elements from midIndex onwards to rightNode
		rightNode.setKeys(curr.getKeys().subList(midIndex, curr.getKeys().size()));
		rightNode.setParent(newParentNode);

		// Update curr node to contain only elements before midIndex
		curr.getKeys().subList(midIndex, curr.getKeys().size()).clear();

		boolean leaf = true;
		splitInternalNode(curr.getParent(), curr, maxPointer, newParentNode, leaf);

	}

	private void splitInternalNode(Node curr, Node childNode, int maxPointer, Node insertedNode, boolean leaf) {

		// If current node is null
		if (null == curr) {
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
			mergeInternalNodes(insertedNode, curr);

			// Split if internal node is full
			if (curr.getKeys().size() == maxPointer) {
				int midIndex = (int) Math.ceil(maxPointer / 2.0) - 1;
				Node newParentNode = new Node();
				Node rightNode = new Node();

				newParentNode.getKeys().add(curr.getKeys().get(midIndex));
				newParentNode.getChildren().add(rightNode);

				rightNode.setKeys(curr.getKeys().subList(midIndex + 1, curr.getKeys().size()));
				rightNode.setParent(newParentNode);

				List<Node> currChildren = curr.getChildren();
				List<Node> rightChildren = new ArrayList<>();

				int leftChild = currChildren.size() - 1;

				// Shift children to rightNode
				for (int i = currChildren.size() - 1; i >= 0; i--) {
					List<Key> currKeysList = currChildren.get(i).getKeys();
					if (newParentNode.getKeys().get(0).getKey() <= currKeysList.get(0).getKey()) {
						currChildren.get(i).setParent(rightNode);
						rightChildren.add(0, currChildren.get(i));
						leftChild--;
					} else {
						break;
					}
				}

				rightNode.setChildren(rightChildren);

				// Update the node to contain just the left part and its children
				curr.getChildren().subList(leftChild + 1, currChildren.size()).clear();
				curr.getKeys().subList(midIndex, curr.getKeys().size()).clear();

				splitInternalNode(curr.getParent(), curr, maxPointer, newParentNode, false);
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

	public List<Record> searchKey(int key) {

		// Set access numbers to 0
		dataBlocksAccess = 0;
		indexNodesAccess = 0;

		List<Record> searchValues = null;

		Node curr = this.rootNode;
		indexNodesAccess++;
		System.out.println("Index Node Access: Node= " + curr.getKeys());

		// Traverse to the corresponding external node that would contain this key
		while (curr.getChildren().size() != 0) {
			curr = curr.getChildren().get(searchInternalNode(key, curr.getKeys()));
			indexNodesAccess++;
			System.out.println("Index Node Access: Node= " + curr.getKeys());
		}

		List<Key> keyList = curr.getKeys();

		// Do a linear search in this node for the key
		for (int i = 0; i < keyList.size(); i++) {

			// dataBlocksAccess++;

			if (key == keyList.get(i).getKey()) {

				System.out.println("Data Block Access: Key=" + keyList.get(i).getKey());
				System.out.println("Value Size=" + keyList.get(i).getValues().size() + " Records");
				System.out.println("Value (0)=" + keyList.get(i).getValues().get(0));
				dataBlocksAccess++;

				searchValues = keyList.get(i).getValues();

			}
			if (key < keyList.get(i).getKey()) {
				break;
			}
		}

		return searchValues;
	}

	public List<Key> searchRange(int minKey, int maxKey) {

		// Set access numbers to 0
		indexNodesAccess = 0;
		dataBlocksAccess = 0;
		List<Key> searchKeys = new ArrayList<>();
		Node curr = this.rootNode;

		indexNodesAccess++;
		System.out.println("Index Node Access: Node= " + curr.getKeys());

		while (curr.getChildren().size() != 0) {
			indexNodesAccess++;
			curr = curr.getChildren().get(searchInternalNode(minKey, curr.getKeys()));
			System.out.println("Index Node Access: Node= " + curr.getKeys());
		}

		// Stop if value encountered in list is greater than key2
		boolean endSearch = false;

		while (null != curr && !endSearch) {
			for (int i = 0; i < curr.getKeys().size(); i++) {
				if(curr.getKeys().get(i).getKey() >= minKey && curr.getKeys().get(i).getKey() <= maxKey){
					dataBlocksAccess++;
//					System.out.println("Data Block Access: Key=" + curr.getKeys().get(i).getKey()
//							+ " |\n Value=" + curr.getKeys().get(i).getValues());
//
//					System.out.println("Data Block Access: Key= " + curr.getKeys().get(i).getKey());
//					System.out.println("Value Size= " + curr.getKeys().get(i).getValues().size() + " Records");
//					System.out.println("Value (0)= " + curr.getKeys().get(i).getValues().get(0));
				}


				if (curr.getKeys().get(i).getKey() >= minKey && curr.getKeys().get(i).getKey() <= maxKey)
					searchKeys.add(curr.getKeys().get(i));
				if (curr.getKeys().get(i).getKey() > maxKey) {
					endSearch = true;
				}
			}
			curr = curr.getNext();
		}

		return searchKeys;
	}
	
	public int countTreeIndexNodes() {
		int countIndexNodes = 0;
		
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(this.rootNode);
		queue.add(null);
		Node curr = null;
		
		while (!queue.isEmpty()) {
			curr = queue.poll();
			if (null == curr) {
				queue.add(null);
				if (queue.peek() == null) {
					break;
				}
				continue;
			}

			countIndexNodes++;
			
			if (curr.getChildren().isEmpty()) {
				break;
			}
			for (int i = 0; i < curr.getChildren().size(); i++) {
				queue.add(curr.getChildren().get(i));
			}
		}

		curr = curr.getNext();
		while (null != curr) {
			countIndexNodes++;
			curr = curr.getNext();
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
//		Node curr = this.rootNode;
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
//		while (curr.getChildren().size() != 0) {
//			curr = curr.getChildren().get(searchInternalNode(key, curr.getKeys()));
//			System.out.println("curr: " + curr.getKeys());
//			System.out.println("curr children: " + curr.getChildren() + " | Size: " + curr.getChildren().size());
//
//			List<Key> keys = curr.getKeys();
//
//			if (curr.isLeaf == false) {
//				System.out.println("=Internal=");
//				System.out.println("Here: " + keys + " | Size: " + keys.size());
//
//				System.out.println("Children: " + curr.getChildren());
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
//					Node firstChildrenNode = curr.getChildren().get(0);
//					System.out.println("Children Key: " + firstChildrenNode.getKeys());
//					curr.setKeys(firstChildrenNode.getKeys());
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
//				List<Node> nodeList = curr.getParent().getChildren();
//
//				System.out.println("Curr: " + curr.getParent().getChildren());
//				for (int i = 0; i < nodeList.size(); i++) {
//					if (nodeList.get(i).getKeys().size() == 0) {
//						nodeList.remove(i);
//					}
//				}
//
//				// 1: If node has less than ceil(maxPointer/2):
//				if (keys.size() < minLeafKeys) {
//					System.out.println("Not enough keys in node");
//					System.out.println("Parent: " + curr.getParent());
//					System.out.println("Next Sibling: " + curr.getNext());
//					System.out.println("Prev Sibling: " + curr.getPrev());
//
//					// See if sibling can lend a key
//					// If can borrow, borrow key and adjust parent node keys
//					Node nextNode = curr.getNext();
//					Node prevNode = curr.getPrev();
//					Node parentNode = curr.getParent();
//
//					// If cannot borrow, merge with sibling & adjust parent node keys
//
//					if (parentNode != null) {
//						System.out.println("Parent Children: " + curr.getParent().getChildren());
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

		Node curr = this.rootNode;
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
		 * if(curr.getChildren().isEmpty()) { List<Key> keys = curr.getKeys();
		 * 
		 * //If key is a rootNode node, remove key for(int i = 0; i < keys.size(); i++) {
		 * if(keys.get(i).getKey() == key) { System.out.println("Removed");
		 * keys.remove(i); } }
		 * 
		 * // if key size is more than 1 keys required in rootNode node if(keys.size() > 1)
		 * { return; } if(keys.size() == 1) }
		 */

		while (curr.getChildren().size() != 0) {
			curr = curr.getChildren().get(searchInternalNode(key, curr.getKeys()));
			System.out.println("curr: " + curr.getKeys());
			System.out.println("curr children: " + curr.getChildren() + " | Size: " + curr.getChildren().size());

			List<Key> keys = curr.getKeys();

			if (curr.isLeaf == false) {
				System.out.println("=Internal=");
				System.out.println("Here: " + keys + " | Size: " + keys.size());

				System.out.println("Children: " + curr.getChildren());

				// If key is in non-leaf node, remove key
				for (int i = 0; i < keys.size(); i++) {
					if (keys.get(i).getKey() == key) {
						System.out.println("Removed");
						keys.remove(i);
					}
				}

				// if key size is less than min. keys required in non leaf node
				if (keys.size() < minInternalKeys) {
					Node firstChildrenNode = curr.getChildren().get(0);
					System.out.println("Children Key: " + firstChildrenNode.getKeys());
					curr.setKeys(firstChildrenNode.getKeys());

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

				List<Node> nodeList = curr.getParent().getChildren();

				System.out.println("Updated children nodes: " + curr.getParent().getChildren());
				for (int i = 0; i < nodeList.size(); i++) {
					if (nodeList.get(i).getKeys().size() == 0) {
						nodeList.remove(i);
					}
				}

				// 1: If node has less than ceil(maxPointer+1/2):
				if (keys.size() < minLeafKeys) {
					System.out.println("Not enough keys in node");
					System.out.println("Parent: " + curr.getParent());
					System.out.println("Next Sibling: " + curr.getNext());
					System.out.println("Prev Sibling: " + curr.getPrev());

					// See if sibling can lend a key
					// If can borrow, borrow key and adjust parent node keys
					Node nextNode = curr.getNext();
					Node prevNode = curr.getPrev();
					Node parentNode = curr.getParent();

					/*
					 * // If can borrow a key from left or right sibling, adjust keys in leaf node
					 * and its parent node //1) Check left sibling first, if cannot borrow //2)
					 * Check right sibling
					 * 
					 * int st = 0; int end = prevNode.getKeys().size() - 1;
					 * if(prevNode.getKeys().size() - 1 > minLeafKeys) { // Add the last element
					 * key of left sibling to current node keys.add(st,
					 * prevNode.getKeys().get(end));
					 * 
					 * // Update smallest key of current node to parent node for(int i = 0; i <
					 * parentNode.getKeys().size(); i++) { // Find the previous smallest key of
					 * current node in the parent node if(parentNode.getKeys().get(i).getKey() ==
					 * key) { // Get the new smallest key of current node at index position 0 and
					 * update the new smallest key in the parent node
					 * parentNode.getKeys().get(i).setKey(keys.get(st).getKey()); } } } else
					 * if(nextNode.getKeys().size() - 1 > minLeafKeys) { // Add the first
					 * element key of right sibling to current node
					 * keys.add(nextNode.getKeys().get(st)); }
					 */

					// If cannot borrow, merge with sibling & adjust parent node keys

					if (parentNode != null) {
						System.out.println("Parent Children: " + curr.getParent().getChildren());
						int noChildren = curr.getParent().getChildren().size();
						System.out.println("Number of children for parent node: " + noChildren);
						int count = 0;
						while (count < noChildren){
							if(prevNode != curr.getParent().getChildren().get(count)){
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
								ableRightBorrow = borrowRight(curr, nextNode, parentNode, minLeafKeys, keys);
							}
						}
						// Check if able to borrow from right
						else{
							System.out.println("Previous node does not share same parent");
							//check if possible to borrow key from next sibling
							ableRightBorrow = borrowRight(curr, nextNode, parentNode, minLeafKeys, keys);
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

					Node parentNode = curr.getParent();
					for (int i = 0; i < parentNode.getKeys().size(); i++) {
						// Find the previous smallest key of current node in the parent node
						if (parentNode.getKeys().get(i).getKey() == key) {
							// Get the new smallest key of current node at index position 0 and update the
							// new smallest key in the parent node
							parentNode.getKeys().get(i).setKey(keys.get(0).getKey());
						}
					}
				}
			}
		}

	}

	public boolean borrowRight(Node curr, Node nextNode, Node parentNode, int minLeafKeys, List<Key> keys){
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
			System.out.println("Updated current node keys: " + keys);
			System.out.println("Updated right sibling node keys: " + nextNode.getKeys());
			//propagate changes to parent node
			int newSmallestNextNodeKey = nextNode.getKeys().get(0).getKey();
			System.out.println("Smallest Next Node Key: " + newSmallestNextNodeKey);
			for (int i = 0; i < parentNode.getKeys().size(); i++) {
				// Find the previous smallest key of current node in the parent node
				if (parentNode.getKeys().get(i).getKey() == prevNextNodeSmallestKey) {
					// Get the new smallest key of current node at index position 0 and update the
					// new smallest key in the parent node
					parentNode.getKeys().get(i).setKey(newSmallestNextNodeKey);
				}
			}
			System.out.println("Parent: " + curr.getParent());
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

	public void displayNumNodesInfo() {
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
		Node curr = null;
		int levelNumber = 2;
		System.out.println("Printing level 1 (Root)");
		while (!queue.isEmpty()) {
			curr = queue.poll();
			if (null == curr) {
				queue.add(null);
				if (queue.peek() == null) {
					break;
				}
				height = levelNumber;
				System.out.println("\n" + "Printing level " + levelNumber++);

				continue;
			}

			displayNode(curr);
			numOfNodes++;

			if (curr.getChildren().isEmpty()) {
				break;
			}
			for (int i = 0; i < curr.getChildren().size(); i++) {
				queue.add(curr.getChildren().get(i));
			}
		}

		curr = curr.getNext();
		while (null != curr) {
			displayNode(curr);
			numOfNodes++;
			curr = curr.getNext();
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
		Node curr = null;
		int levelNumber = 2;
		System.out.println("Printing level 1 (Root)");
		int rootAndFirst = 0;
		while (!queue.isEmpty()) {
			curr = queue.poll();
			if (null == curr) {
				queue.add(null);
				if (queue.peek() == null) {
					break;
				}
				height = levelNumber;
				levelNumber++;
				if(rootAndFirst < 2) {
					System.out.println("\n" + "Printing level " + (levelNumber-1));
				}
				continue;
			}
			if(rootAndFirst == 0) {
				displayNode(curr);
			}
			else if(rootAndFirst == 1) {
				System.out.println("Printing First Child Node:");
				displayNode(curr);
			}
			numOfNodes++;

			if (curr.getChildren().isEmpty()) {
				break;
			}
			for (int i = 0; i < curr.getChildren().size(); i++) {
				queue.add(curr.getChildren().get(i));
			}
			rootAndFirst++;
		}

		curr = curr.getNext();
		while (null != curr) {
//			displayNode(curr);
			numOfNodes++;
			curr = curr.getNext();
		}
		System.out.println("\nTotal number of nodes in B+ tree is: " + numOfNodes);
		System.out.println("Total number of records in B+ tree is: " + totalRecordsCount);

		displayHeight();
	}

	private void displayNode(Node curr) {

		for (int i = 0; i < curr.getKeys().size(); i++) {
			nodeRecordCount = 0;
			System.out.print(curr.getKeys().get(i).getKey() + ":(");
			String values = "";
			for (int j = 0; j < curr.getKeys().get(i).getValues().size(); j++) {
				values = values + curr.getKeys().get(i).getValues().get(j) + ",";
				nodeRecordCount++;

				/*
				 * if(!values.isEmpty()) {
				 * System.out.print(curr.getKeys().get(i).getValues().get(j).getTConst() + ",");
				 * }
				 */

			}

			totalRecordsCount += nodeRecordCount;
			// System.out.print(values.isEmpty() ? ");" : uniqueKeysCount++ + ")" + "(" +
			// nodeRecordCount + ")" + values.substring(0, values.length() - 1) +
			// ");\n");
			// System.out.print(values.isEmpty() ? ");" : uniqueKeysCount++ + ")" + "(" +
			// nodeRecordCount + ");");
			System.out.print(values.isEmpty() ? ");" : nodeRecordCount + ");");
		}

		if (curr.getKeys().size() != 0) {
			System.out.print("||");
		}

	}

	public void printIndexNodesAccessed() {
		System.out.println("Number of Index Nodes Access: " + indexNodesAccess + "\n");
	}

	public void printDataBlocksAccessed() {
		System.out.println("Number of Data Block Access: " + dataBlocksAccess);
	}

	public void printRangeQueries(List<Key> searchRange) {
		int x = 0;
		int dataBlocks = 0;
		// System.out.println(searchRange.size());
		float totalAverageRating = 0;
		System.out.println("===================Part 2: Display Data Block ===================");
		for (int j = 0; j < searchRange.size(); j++) {
			System.out.println("\nnumVotes -> [Key: " + searchRange.get(j).getKey() + "]");
			dataBlocks++;
			for (int y = 0; y < searchRange.get(j).getValues().size(); y++) {
				x++;
				System.out.print("  tConst: ");
				System.out.print(searchRange.get(j).getValues().get(y).getTConst() + ",");
				System.out.print(" Average Rating: " + searchRange.get(j).getValues().get(y).getAverageRating() + "\n");
				totalAverageRating += searchRange.get(j).getValues().get(y).getAverageRating();
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
