import java.util.ArrayList;
import java.util.List;

public class Key {
	
	private int key; // Key Value

	List<Record> values; // Data Value: Array List of Record
	
	// Initialize Key
	public Key(int key, Record value) {
		this.key = key;
		if (null == this.values) {
			values = new ArrayList<>();
		}
		this.values.add(value);
	}
	
	// Initialize Key
	public Key(int key) {
		this.key = key;
		this.values = new ArrayList<>();
	}

	// Return Key Value
	public int getKey() {
		return key;
	}

	// Set Key Value
	public void setKey(int key) {
		this.key = key;
	}

	// Get Key Data Values
	public List<Record> getValues() {
		return values;
	}

	// Set Key Data Values
	public void setValues(List<Record> values) {
		this.values = values;
	}

	// Print Key Information
	public String toString() {
		//return "<KEY>[Key= " + key + ", DataPointer= " + values + "] ";
		return "<KEY>[Key= " + key +  "] ";
	}
}
