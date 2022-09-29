import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Block {

	private List<Record> records; //Pointer to Records
	
	public Block ()
	{
		this.records = new ArrayList<>();
	}
	
	public List<Record> getRecords() {
		return records;
	}

	public void setRecords(List<Record> records) {
		Iterator<Record> iter = records.iterator();
		while (iter.hasNext()) {
			this.records.add(iter.next());
		}
	}
}
