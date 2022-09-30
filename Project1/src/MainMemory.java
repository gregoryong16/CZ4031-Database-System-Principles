import java.io.UnsupportedEncodingException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainMemory {

	public static int recordSize; // Record Size

	public int memPoolSize; // Memory Pool Size
	public int block_size; // Block Size
	public int free_Size; // Free Size
	public int sise_used; // Size Used
	public int allocated; // Allocated Size
	public int remaining; // Number of Blocks Remaining
	public int block; // Number of Blocks
	public int recPerBlock; // Number of Records Per Block
	public int sumNoOfRecords; // Total Number of Records
	public int sumBlockSize; // Total Block Size
	public int sumRecSize; // Total Record Size
	public int record_Count = 0; // Number of Record
	public int db_S = 0; // Database Size
	public int ind_N = 0; // Number of Index Nodes

	private List<Block> listBlk; // Pointer to Data Block
	private Block blk; // Block

	public MainMemory(int poolSize, int blockSize) {
		this.memPoolSize = poolSize;
		this.free_Size = poolSize;
		this.block = poolSize;

		this.block_size = blockSize;

		this.remaining = poolSize / blockSize;

		this.sise_used = 0;
		this.allocated = 0;
		listBlk = new ArrayList<>();
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
		this.recPerBlock = (int) Math.floor((double) block_size / (double) recordSize);

	}

	public void AddBlock(Block newBlk, Record rec) {

		blk = newBlk;
		blk.getRecords().add(rec);

		if (record_Count % recPerBlock == 0) {
			record_Count = 0;
			this.sise_used += block_size;
			sumBlockSize += block_size;
			listBlk.add(blk);
			
			this.remaining--;

			this.allocated++;
		}

		this.sise_used += recordSize;

		sumRecSize += recordSize;
		
		this.record_Count++;
	}

	
	public void DeallocateFromThePool() {

		this.remaining++;

		this.allocated--;

		this.sise_used -= recordSize;

		sumRecSize -= recordSize;
	}

	public void saveRecordIntoMemory(int totalNoOfRecords) {
		this.sumNoOfRecords = totalNoOfRecords;

		this.block = (int) Math.ceil((double) this.sumNoOfRecords / (double) recPerBlock);
	}

	public int getRecordNo() {
		return sumNoOfRecords;
	}
	
	public List<Block> GetPointerBlockList(){
		
		return listBlk;
	}

	
}
