
public class Record {
	private String tconst;
	private float averageRating;
	private int numVotes;
	
	public Record(String tconst, float averageRating, int numVotes) {
		this.tconst = tconst;
		this.averageRating = averageRating;
		this.numVotes = numVotes;
	}
	
	public String getTConst() {
		return tconst;
	}
	
	public float getAverageRating() {
		return averageRating;
	}
	
	public int getNumVotes() {
		return numVotes;
	}
}
