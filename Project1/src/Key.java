import java.util.ArrayList;
import java.util.List;

public class Key {
	
	private int key; // Key Value

	List<Record> records; // Data Value: Array List of Record
	
	// Initialize Key
	public Key(int key, Record value) {
		this.key = key;
		if (null == this.records) {
			records = new ArrayList<>();
		}
		this.records.add(value);
	}
	
	// Initialize Key
	public Key(int key) {
		this.key = key;
		this.records = new ArrayList<>();
	}

	// Return Key Value
	public int getKey() {
		return key;
	}

	// Set Key Value
	public void setKey(int key) {
		this.key = key;
	}

	// Get Key Data records
	public List<Record> getRecords() {
		return records;
	}

	// Set Key Data records
	public void setRecords(List<Record> records) {
		this.records = records;
	}

	// Print Key Information
	public String toString() {
		//return "<KEY>[Key= " + key + ", DataPointer= " + records + "] ";
		return "<KEY>[Key= " + key +  "] ";
	}
}
