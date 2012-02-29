package teammates.jdo;

/**
 * CourseSummaryForStudent is a data class that contains some information from a
 * Course object and an additional field which is a particular student's team
 * name in the course.
 * 
 * @author Gerald
 * 
 */
public class CourseSummaryForStudent {
	private String ID;
	private String name;
	private String teamName;
	private boolean archived;

	/**
	 * Constructs a CourseSummaryForStudent object.
	 * 
	 * @param ID
	 * @param name
	 * @param teamName
	 * @param archived
	 */
	public CourseSummaryForStudent(String ID, String name, String teamName,
			boolean archived) {
		this.setID(ID);
		this.setName(name);
		this.setTeamName(teamName);
		this.setArchived(archived);
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public String getID() {
		return ID;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public boolean isArchived() {
		return archived;
	}
}
