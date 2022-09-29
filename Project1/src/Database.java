import java.io.UnsupportedEncodingException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Database {

	public static int recordSize; // Record Size

	private int poolSize; // Memory Pool Size
	private int blockSize; // Block Size
	private int freeSize; // Free Size
	private int sizeUsed; // Size Used
	private int allocated; // Allocated Size
	private int remaining; // Number of Blocks Remaining
	private int block; // Number of Blocks
	public int recordsPerBlock; // Number of Records Per Block
	public int totalNoOfRecords; // Total Number of Records
	public int totalBlockSize; // Total Block Size
	public int totalRecordSize; // Total Record Size
	public int recordCounter = 0; // Number of Record
	public int databaseSize = 0; // Database Size
	public int indexNodes = 0; // Number of Index Nodes

	private List<Block> listBlk; // Pointer to Data Block
	private Block blk; // Block

	public Database(int poolSize, int blockSize) {
		this.poolSize = poolSize;
		this.freeSize = poolSize;
		this.block = poolSize;

		this.blockSize = blockSize;

		this.remaining = poolSize / blockSize;

		this.sizeUsed = 0;
		this.allocated = 0;
		listBlk = new ArrayList<>();
	}

	public void allocateBlock(Block newBlk, Record rec) {

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
			this.sizeUsed += blockSize;
			totalBlockSize += blockSize;
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

	public void allocateRecord(Record rec) {
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
		this.recordsPerBlock = (int) Math.floor((double) blockSize / (double) recordSize);

	}

	public void deallocateBlock() {
		// Number of blocks remaining plus 1
		this.remaining++;

		// Number of blocks deallocated minus 1
		this.allocated--;

		this.sizeUsed -= recordSize;

		totalRecordSize -= recordSize;
	}

	public void printDatabaseInfo() {
		System.out.println("Memory Size: " + poolSize + " bytes");
		System.out.println("Block Size: " + blockSize + " bytes");
		System.out.println("Record Size: " + recordSize + " bytes");
		System.out.println("Size Used: " + sizeUsed + " bytes");
		System.out.println("Remaining: " + remaining + " Blocks");
		System.out.println("Allocated: " + allocated + " Blocks");
		System.out.println("Total number of records: " + totalNoOfRecords);
		System.out.println("Number of records per block: " + recordsPerBlock);
		System.out.println("Number of Blocks: " + block);
		System.out.println("Total record size: " + totalRecordSize);
		System.out.println("Total block size: " + totalBlockSize);

		// Sum of the size of the relational data and b+ tree structure
		databaseSize = totalRecordSize + (indexNodes * blockSize);
		System.out.println("The Size of database: " + databaseSize);
	}

	public void setRecord(int totalNoOfRecords) {
		this.totalNoOfRecords = totalNoOfRecords;

		// round up to the nearest whole number to get minimum number of blocks
		this.block = (int) Math.ceil((double) this.totalNoOfRecords / (double) recordsPerBlock);
	}

	public int getRecord() {
		return totalNoOfRecords;
	}

	public void printDataRecords() {

		// System.out.println("List blk contents size: " + listBlk.size());

		for (int i = 0; i < listBlk.size(); i++) {
			// System.out.println("Each blk contents size: " +
			// listBlk.get(i).getRecords().size());

			for (int j = 0; j < listBlk.get(i).getRecords().size(); j++) {
				/*
				 * System.out.println("Blk record tconst: " +
				 * listBlk.get(i).getRecords().get(j).getTConst());
				 * System.out.println("Blk record average Rating: " +
				 * listBlk.get(i).getRecords().get(j).getAverageRating());
				 * System.out.println("Blk record numVotes: " +
				 * listBlk.get(i).getRecords().get(j).getNumVotes());
				 */
			}
			// System.out.println();
		}

	}
}
