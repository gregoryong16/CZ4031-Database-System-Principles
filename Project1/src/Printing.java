import java.util.List;

//Printing class for the Main Memory

public class Printing {
	
	
	public static void PrintDatabaseInfo(MainMemory _Mm) {
		System.out.println("Memory Size: " + _Mm.memPoolSize + " bytes");
		System.out.println("Block Size: " + _Mm.block_size + " bytes");
		System.out.println("Record Size: " + _Mm.recordSize + " bytes");
		System.out.println("Size Used: " + _Mm.sizeUsed + " bytes");
		System.out.println("Remaining: " + _Mm.remaining + " Blocks");
		System.out.println("Allocated: " + _Mm.allocated + " Blocks");
		System.out.println("Total number of records: " + _Mm.totalNoOfRecords);
		System.out.println("Number of records per block: " + _Mm.recordsPerBlock);
		System.out.println("Number of Blocks: " + _Mm.block);
		System.out.println("Total record size: " + _Mm.totalRecordSize);
		System.out.println("Total block size: " + _Mm.totalBlockSize);

		System.out.println("The Size of database: " + _Mm.totalRecordSize + (_Mm.indexNodes * _Mm.block_size));
	}
	
	public void PrintRecordsInformation(MainMemory _Mm) {


		List<Block> used = _Mm.GetPointerBlockList();

		System.out.println("############# List blok content size: " + used.size() + "#############");
		
		
		for (int i = 0; i < used.size(); i++) {
			 System.out.println("Each blok contents: " +  used.get(i).getRecords().size());

			for (int j = 0; j < used.get(i).getRecords().size(); j++) {

				
				Record tmpRecord = new Record(used.get(i).getRecords().get(j));
				
				System.out.println("Blk records details:");
				System.out.println("TConst: " + tmpRecord.getTConst());
				System.out.println("AverageRating: " + tmpRecord.getAverageRating());
				System.out.println("Votes: " + tmpRecord.getNumVotes());
			}
			
			System.out.println("##########################END##########################");
		}

	}

}
