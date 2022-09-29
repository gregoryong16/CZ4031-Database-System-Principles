import java.io.File;
import java.util.List;
import java.util.Scanner;

public class main {

	public static void main(String[] args) {

		// 1: Load Data File
		String workingDir = System.getProperty("user.dir");
		//String fileName = workingDir + "\\" + "Project-BPlusTree\\Project-BPlusTree"
		// + "\\" + "data.tsv";
		String fileName = workingDir + "\\" + "data.tsv";
		File inputFile = new File(fileName);

		// 2: Set Database and B+ Tree Size
		BPTree tree = null;
		Database db = null;

		boolean exit1 = false;
		boolean selected = false;
		do {

			System.out.println("============================Select Byte Size============================");
			System.out.println("1: Select 200 Bytes");
			System.out.println("2: Select 500 Bytes");
			System.out.println("3: Quit");
			Scanner scan = new Scanner(System.in);
			int choice = scan.nextInt();

			switch (choice) {
				case 1:
					// Experiment 1
					tree = new BPTree(11); // 100 Bytes
					db = new Database(500000000, 200); // 200 Bytes
					selected = true;
//					exit1 = true;
					break;
				case 2:
					tree = new BPTree(27); // 500 Bytes
					db = new Database(500000000, 500); // 500 Bytes
					selected = true;
//					exit1 = true;
					break;
				case 3:
					scan.close();
					exit1 = true;
					break;

//			default:
//				scan.close();
//				exit1 = true;
//				break;
			}

			if (selected == true && choice != 3) {

				Block newBlk = null;

				// 3: Run Scanner
				try {
					Scanner sc = new Scanner(inputFile);
					int recordCounter = 0;
					// int i = 0;

					System.out.println("[Reading File]");
					while (sc.hasNextLine()) {

						if (recordCounter == 0) {
							sc.nextLine(); // Skip the column line
							// i++;
							// newBlk = new Block();
						}

						if (recordCounter >= 0) {
							String newLine = sc.nextLine();
							String[] input = newLine.split("\t"); // Split Line with the tab

							Record rec = new Record(input[0], Integer.parseInt(String.valueOf(input[2])),
									Integer.parseInt(String.valueOf(input[2])));

							db.allocateRecord(rec);

							if (recordCounter % db.recordsPerBlock == 0) {
								// System.out.println("i index: " + recordCounter);
								newBlk = new Block();
							}

							db.allocateBlock(newBlk, rec);

							tree.insertKey(Integer.parseInt(String.valueOf(input[2])), rec);

							recordCounter++;
							/*
							 * if(i == 1000) { break; }
							 */

						}

					}
					System.out.println(recordCounter);

					db.setRecord(recordCounter);
					sc.close();
					System.out.println("[Done Loading]");

					// 4: Main Menu
					boolean exit = false;
					do {

						System.out.println("============================Experiments============================");
						System.out.println("1: Database Info");
						System.out.println("2: B+ Tree Info");
						System.out.println("3: Search Key with numVotes of 500");
						System.out.println("4: Search Key Range with numVotes of 30000 to 40000");
						System.out.println("5: Delete Key with numVotes of 1000");
						System.out.println("6: Quit");
//						Scanner scan = new Scanner(System.in);
						int choice1 = scan.nextInt();

						switch (choice1) {
							case 1:
								// Experiment 1
								System.out.println("============================Experiment 1============================");

								int indexNodes = tree.countTreeIndexNodes();
								db.indexNodes = indexNodes;
								db.printDatabaseInfo();
								break;
							case 2:
								// Experiment 2
								System.out.println("============================Experiment 2============================");
								tree.displayTreeInfo();
								tree.displayHeightInfo();
								System.out.print("\n");
								break;

							case 3:
								// Experiment 3
								System.out.println("============================Experiment 3============================");
								System.out.println("Enter search key: ");
								int searchKey = scan.nextInt();
								List<Record> searchValues = tree.searchKey(searchKey); //500

								System.out.println("List of tconst: ");
								for (int j = 0; j < searchValues.size(); j++) {

									System.out.print(searchValues.get(j).getTConst() + " ");

									if (j % 100 == 0 && j != 0) {
										System.out.print("\n");
									}

								}

								System.out.print("\n");
								System.out.println("Total Records: " + searchValues.size());

								tree.printIndexNodeAccess();
								tree.printDataBlockAccess();
								break;

							case 4:
								// Experiment 4
								System.out.println("============================Experiment 4============================");
								System.out.println("Enter search key 1: ");
								int searchKey1 = scan.nextInt();
								System.out.println("Enter search key 2: ");
								int searchKey2 = scan.nextInt();
								List<Key> searchRange = tree.searchRange(searchKey1, searchKey2); //30000,40000

								int x = 0;
								// System.out.println(searchRange.size());
								for (int j = 0; j < searchRange.size(); j++) {
									System.out.println("numVotes -> [Key: " + searchRange.get(j).getKey() + "]");
									System.out.print("tConst: ");
									for (int y = 0; y < searchRange.get(j).getValues().size(); y++) {
										x++;
										System.out.print(searchRange.get(j).getValues().get(y).getTConst() + " ");

										if (y % 100 == 0 && y != 0) {
											System.out.print("\n");
										}

									}
									// System.out.print("\n");
								}

								System.out.println("Total Records: " + x);

								tree.printIndexNodeAccess();
								tree.printDataBlockAccess();
								break;

							case 5:
								// Experiment 5
								System.out.println("============================Experiment 5============================");
								tree.deleteKey(1000);
								tree.displayDeleteInfo();
								break;

							case 6:
//								scan.close();
								exit = true;
								break;

//					default:
//						scan.close();
//						exit = true;
//						break;
						}

					} while (!exit);

					/*
					 * System.out.
					 * println("============================FIELD INFO============================"
					 * ); System.out.println("Size of byte: " + (Byte.SIZE/8) + " bytes.");
					 * System.out.println("Size of short: " + (Short.SIZE/8) + " bytes.");
					 * System.out.println("Size of int: " + (Integer.SIZE/8) + " bytes.");
					 * System.out.println("Size of long: " + (Long.SIZE/8) + " bytes.");
					 * System.out.println("Size of char: " + (Character.SIZE/8) + " bytes.");
					 * System.out.println("Size of float: " + (Float.SIZE/8) + " bytes.");
					 * System.out.println("Size of double: " + (Double.SIZE/8) + " bytes.");
					 *
					 * String s = "tt0017626"; byte[] b = s.getBytes("UTF-8");
					 * System.out.println("Size of String: " + b.length + " bytes.");
					 */

				} catch (Exception e) {
					e.printStackTrace(); // Catch errors
				}
			}

		} while (!exit1);

//		if (selected == true) {
//
//			Block newBlk = null;
//
//			// 3: Run Scanner
//			try {
//				Scanner sc = new Scanner(inputFile);
//				int recordCounter = 0;
//				// int i = 0;
//
//				System.out.println("[Reading File]");
//				while (sc.hasNextLine()) {
//
//					if (recordCounter == 0) {
//						sc.nextLine(); // Skip the column line
//						// i++;
//						// newBlk = new Block();
//					}
//
//					if (recordCounter >= 0) {
//						String newLine = sc.nextLine();
//						String[] input = newLine.split("\t"); // Split Line with the tab
//
//						Record rec = new Record(input[0], Integer.parseInt(String.valueOf(input[2])),
//								Integer.parseInt(String.valueOf(input[2])));
//
//						db.allocateRecord(rec);
//
//						if (recordCounter % db.recordsPerBlock == 0) {
//							// System.out.println("i index: " + recordCounter);
//							newBlk = new Block();
//						}
//
//						db.allocateBlock(newBlk, rec);
//
//						tree.insertKey(Integer.parseInt(String.valueOf(input[2])), rec);
//
//						recordCounter++;
//						/*
//						 * if(i == 1000) { break; }
//						 */
//
//					}
//
//				}
//				System.out.println(recordCounter);
//
//				db.setRecord(recordCounter);
//				sc.close();
//				System.out.println("[Done Loading]");
//
//				// 4: Main Menu
//				boolean exit = false;
//				do {
//
//					System.out.println("============================Experiments============================");
//					System.out.println("1: Database Info");
//					System.out.println("2: B+ Tree Info");
//					System.out.println("3: Search Key with numVotes of 500");
//					System.out.println("4: Search Key Range with numVotes of 30000 to 40000");
//					System.out.println("5: Delete Key with numVotes of 1000");
//					System.out.println("6: Quit");
//					Scanner scan = new Scanner(System.in);
//					int choice = scan.nextInt();
//
//					switch (choice) {
//					case 1:
//						// Experiment 1
//						System.out.println("============================Experiment 1============================");
//
//						int indexNodes = tree.countTreeIndexNodes();
//						db.indexNodes = indexNodes;
//						db.printDatabaseInfo();
//						break;
//					case 2:
//						// Experiment 2
//						System.out.println("============================Experiment 2============================");
//						tree.displayTreeInfo();
//						tree.displayHeightInfo();
//						System.out.print("\n");
//						break;
//
//					case 3:
//						// Experiment 3
//						System.out.println("============================Experiment 3============================");
//						System.out.println("Enter search key: ");
//						int searchKey = scan.nextInt();
//						List<Record> searchValues = tree.searchKey(searchKey); //500
//
//						System.out.println("List of tconst: ");
//						for (int j = 0; j < searchValues.size(); j++) {
//
//							System.out.print(searchValues.get(j).getTConst() + " ");
//
//							if (j % 100 == 0 && j != 0) {
//								System.out.print("\n");
//							}
//
//						}
//
//						System.out.print("\n");
//						System.out.println("Total Records: " + searchValues.size());
//
//						tree.printIndexNodeAccess();
//						tree.printDataBlockAccess();
//						break;
//
//					case 4:
//						// Experiment 4
//						System.out.println("============================Experiment 4============================");
//						System.out.println("Enter search key 1: ");
//						int searchKey1 = scan.nextInt();
//						System.out.println("Enter search key 2: ");
//						int searchKey2 = scan.nextInt();
//						List<Key> searchRange = tree.searchRange(searchKey1, searchKey2); //30000,40000
//
//						int x = 0;
//						// System.out.println(searchRange.size());
//						for (int j = 0; j < searchRange.size(); j++) {
//							System.out.println("numVotes -> [Key: " + searchRange.get(j).getKey() + "]");
//							System.out.print("tConst: ");
//							for (int y = 0; y < searchRange.get(j).getValues().size(); y++) {
//								x++;
//								System.out.print(searchRange.get(j).getValues().get(y).getTConst() + " ");
//
//								if (y % 100 == 0 && y != 0) {
//									System.out.print("\n");
//								}
//
//							}
//							// System.out.print("\n");
//						}
//
//						System.out.println("Total Records: " + x);
//
//						tree.printIndexNodeAccess();
//						tree.printDataBlockAccess();
//						break;
//
//					case 5:
//						// Experiment 5
//						System.out.println("============================Experiment 5============================");
//						tree.deleteKey(1000);
//						tree.displayDeleteInfo();
//						break;
//
//					case 6:
//						scan.close();
//						exit = true;
//						break;
//
////					default:
////						scan.close();
////						exit = true;
////						break;
//					}
//
//				} while (!exit);
//
//				/*
//				 * System.out.
//				 * println("============================FIELD INFO============================"
//				 * ); System.out.println("Size of byte: " + (Byte.SIZE/8) + " bytes.");
//				 * System.out.println("Size of short: " + (Short.SIZE/8) + " bytes.");
//				 * System.out.println("Size of int: " + (Integer.SIZE/8) + " bytes.");
//				 * System.out.println("Size of long: " + (Long.SIZE/8) + " bytes.");
//				 * System.out.println("Size of char: " + (Character.SIZE/8) + " bytes.");
//				 * System.out.println("Size of float: " + (Float.SIZE/8) + " bytes.");
//				 * System.out.println("Size of double: " + (Double.SIZE/8) + " bytes.");
//				 *
//				 * String s = "tt0017626"; byte[] b = s.getBytes("UTF-8");
//				 * System.out.println("Size of String: " + b.length + " bytes.");
//				 */
//
//			} catch (Exception e) {
//				e.printStackTrace(); // Catch errors
//			}
//		}

	}

}
