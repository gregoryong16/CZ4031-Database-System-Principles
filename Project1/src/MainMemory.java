import java.io.UnsupportedEncodingException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainMemory {

	public static int recordSize; // Record Size

	public int memPoolSize; // Memory Pool Size
	public int block_size; // Block Size
	public int freeSize; // Free Size
	public int sizeUsed; // Size Used
	public int allocated; // Allocated Size
	public int remaining; // Number of Blocks Remaining
	public int block; // Number of Blocks
	public int recordsPerBlock; // Number of Records Per Block
	public int totalNoOfRecords; // Total Number of Records
	public int totalBlockSize; // Total Block Size
	public int totalRecordSize; // Total Record Size
	public int recordCounter = 0; // Number of Record
	public int databaseSize = 0; // Database Size
	public int indexNodes = 0; // Number of Index Nodes

	private List<Block> listBlk; // Pointer to Data Block
	private Block blk; // Block

	public MainMemory(int poolSize, int blockSize) {
		this.memPoolSize = poolSize;
		this.freeSize = poolSize;
		this.block = poolSize;

		this.block_size = blockSize;

		this.remaining = poolSize / blockSize;

		this.sizeUsed = 0;
		this.allocated = 0;
		listBlk = new ArrayList<>();
	}

	public void AddBlock(Block newBlk, Record rec) {

		// Block newBlk = new Block();
		blk = newBlk;
		blk.getRecords().add(rec);
		// System.out.println("Each Block records size: " + blk.getRecords().size());
		// System.out.println("***************************************");

		// List<Block> currentBlk = blk;
		// System.out.println("blk contents size: " + listBlk.size());

		/*
		 * for(int i = 0; i < currentBlk.size(); i ++) {
		 * //System.out.println("records added content: " + currentBlk.get(i));
		 * //System.out.println("*********************************"); }
		 */
		// if the number of records is full in a block, reset the record counter
		if (recordCounter % recordsPerBlock == 0) {
			recordCounter = 0;
			this.sizeUsed += block_size;
			totalBlockSize += block_size;
			listBlk.add(blk);
			// System.out.println("list block created: " + listBlk.size());

			// Number of blocks remaining minus 1
			this.remaining--;

			// Number of blocks allocated plus 1
			this.allocated++;
		}

		this.sizeUsed += recordSize;

		totalRecordSize += recordSize;
		// System.out.println(this.recordCounter + " Cumulative size used: " +
		// this.sizeUsed);

		// Keep track of the number of records in a block
		this.recordCounter++;
		// System.out.println("Record counter: " + recordCounter);
	}

	public void AllocateRecordToPool(Record rec) {
		// String s = "tt0017626";
		// float averageRating = 5.6f;
		// int numVotes = 1024;
		byte[] b = null;

		try {

			b = rec.getTConst().getBytes("UTF-8");
			FloatBuffer fb = FloatBuffer.allocate(1);
			fb.put(rec.getAverageRating());
			IntBuffer ib = IntBuffer.allocate(1);
			ib.put(rec.getNumVotes());
			// System.out.println("FloatBuffer " + Arrays.toString(fb.array()));
			// System.out.println("IntBuffer " + Arrays.toString(ib.array()));

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// String 9 bytes, Float 4 bytes, Integer 4 bytes
		// recordSize = (Float.SIZE/8) + (Integer.SIZE/8) + b.length;

		// String 10 bytes, Float 4 bytes, Integer 4 bytes
		recordSize = (Float.SIZE / 8) + (Integer.SIZE / 8) + 10;

		// round down to the nearest whole number to get maximum number of records able
		// to fit into a block
		this.recordsPerBlock = (int) Math.floor((double) block_size / (double) recordSize);

	}

	public void DeallocateFromThePool() {

		this.remaining++;

		this.allocated--;

		this.sizeUsed -= recordSize;

		totalRecordSize -= recordSize;
	}

	public void saveRecordIntoMemory(int totalNoOfRecords) {
		this.totalNoOfRecords = totalNoOfRecords;

		// round up to the nearest whole number to get minimum number of blocks
		this.block = (int) Math.ceil((double) this.totalNoOfRecords / (double) recordsPerBlock);
	}

	public int getRecordNo() {
		return totalNoOfRecords;
	}
	
	public List<Block> GetPointerBlockList(){
		
		
		return listBlk;
	}

	
}
