import java.util.List;

//Printing class for the Main Memory

public class Printing {
	
	
	public static void PrintDatabaseInfo(MainMemory _Mm) {
		System.out.println("################ DATABASE INFO ################");
		System.out.println("Memory Size: " + _Mm.memPoolSize + " bytes");
		System.out.println("The Size of database: " + _Mm.sumRecSize + (_Mm.ind_N * _Mm.block_size));
		System.out.println("Block Size: " + _Mm.block_size + " bytes");
		System.out.println("Record Size: " + _Mm.recordSize + " bytes");
		System.out.println("Number of records per block: " + _Mm.recPerBlock);
		System.out.println("Number of Blocks: " + _Mm.block);
		System.out.println("################################");
		System.out.println("Total number of records: " + _Mm.sumNoOfRecords);
		System.out.println("Total record size: " + _Mm.sumRecSize);
		System.out.println("Total block size: " + _Mm.sumBlockSize);
		System.out.println("################################");
		System.out.println("Size Used: " + _Mm.sise_used + " bytes");
		System.out.println("Remaining: " + _Mm.remaining + " Blocks");
		System.out.println("Allocated: " + _Mm.allocated + " Blocks");





		
		System.out.println("################ DATABASE INFO END ################");
	}
	
	public static void PrintRecordsInformation(MainMemory _Mm) {


		List<Block> used = _Mm.GetPointerBlockList();

		System.out.println("############# List blok content size: " + used.size() + "#############");
		
		
		for (Block i : used) {
			 System.out.println("Each blok contents: " +  i.getRecords().size());

			for (Record j : i.getRecords()) {

				
				Record tmpRecord = new Record(j);
				
				System.out.println("Blk records details:");
				System.out.println("TConst: " + tmpRecord.getTConst());
				System.out.println("AverageRating: " + tmpRecord.getAverageRating());
				System.out.println("Votes: " + tmpRecord.getNumVotes());
			}
			
			System.out.println("##########################END##########################");
		}

	}

}
