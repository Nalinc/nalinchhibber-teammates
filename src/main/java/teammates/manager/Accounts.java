package teammates.manager;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.mortbay.log.Log;

import teammates.Datastore;
import teammates.api.EntityAlreadyExistsException;
import teammates.exception.AccountExistsException;
import teammates.persistent.Account;
import teammates.persistent.Coordinator;
import teammates.persistent.Student;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * Accounts handles all operations related to a Teammates account.
 * 
 * @author Gerald GOH
 * @see Account
 * @see Coordinator
 * @see Student
 * 
 */
public class Accounts {

	private static UserService userService;
	private static Accounts instance = null;
	private static final Logger log = Logger.getLogger(Accounts.class
			.getSimpleName());

	/**
	 * Constructs an Accounts object. Initialises userService for Google User
	 * Service and obtains an instance of PersistenceManager class to handle
	 * datastore transactions.
	 */
	private Accounts() {
		userService = UserServiceFactory.getUserService();
	}
	private PersistenceManager getPM() {
		return Datastore.getPersistenceManager();
	}

	public static Accounts inst() {
		if (instance == null)
			instance = new Accounts();
		return instance;
	}

	/**
	 * See if the current user is authenticated
	 * 
	 * @return
	 */
	public boolean isLoggedOn() {
		return userService.getCurrentUser() != null;
	}

	/**
	 * Adds a Coordinator object.
	 * 
	 * @param googleID
	 *            the coordinator's Google ID (Precondition: Must not be null)
	 * 
	 * @param name
	 *            the coordinator's name (Precondition: Must not be null)
	 * 
	 * @param email
	 *            the coordinator's email (Precondition: Must not be null)
	 * 
	 * @throws AccountExistsException
	 *             if a coordinator with the specified Google ID already exists
	 * 
	 */
	public void addCoordinator(String googleID, String name, String email)
			throws EntityAlreadyExistsException {
		if (getCoordinator(googleID) != null) {
			throw new EntityAlreadyExistsException("Coordinator already exists :" + googleID);
		}
		Coordinator coordinator = new Coordinator(googleID, name, email);
		getPM().makePersistent(coordinator);
	}

	/**
	 * Returns a Coordinator object.
	 * 
	 * @param googleID
	 *            the coordinator's Google ID (Precondition: Must not be null)
	 * 
	 * @return the coordinator with the specified Google ID, or null if not
	 *         found
	 */
	public Coordinator getCoordinator(String googleID) {
		String query = "select from " + Coordinator.class.getName()
				+ " where googleID == '" + googleID + "'";

		@SuppressWarnings("unchecked")
		List<Coordinator> coordinatorList = (List<Coordinator>) getPM()
				.newQuery(query).execute();

		if (coordinatorList.isEmpty()) {
			log.warning("Trying to get non-existent Coord : " + googleID);
			return null;
		}

		return coordinatorList.get(0);
	}
	
	public Student getStudent(String courseId, String email) {
		String query = "select from " + Student.class.getName()
				+ " where (email == '" + email + "')"
				+ " && (courseID == '" + courseId + "')";

		@SuppressWarnings("unchecked")
		List<Student> studentList = (List<Student>) getPM()
				.newQuery(query).execute();

		if (studentList.isEmpty()) {
			log.warning("Trying to get non-existent Student : " + courseId
					+ "/" + email);
			return null;
		}
		return studentList.get(0);
	}
	
	public Student getStudentWithID(String googleId) {
		String query = "select from " + Student.class.getName() + " where ID == \"" + googleId + "\"";

		@SuppressWarnings("unchecked")
		List<Student> studentList = (List<Student>) getPM().newQuery(query).execute();

		if (studentList.isEmpty()) {
			return null;
		}

		return studentList.get(0);
	}
	
	public boolean isStudent(String googleId){
		return getStudentWithID(googleId)!= null;
	}

	/**
	 * Returns the list of Coordinator objects.
	 * 
	 * @return the list of coordinators
	 */
	private List<Coordinator> getCoordinatorList() {
		String query = "select from " + Coordinator.class.getName();

		@SuppressWarnings("unchecked")
		List<Coordinator> coordinatorList = (List<Coordinator>) getPM()
				.newQuery(query).execute();

		return coordinatorList;
	}

	/**
	 * Returns the name of the Coordinator object given his googleID.
	 * 
	 * @param googleID
	 *            the coordinator's Google ID (Precondition: Must not be null)
	 * 
	 * @return the name of the coordinator
	 */
	public String getCoordinatorName(String googleID) {
		Coordinator coordinator = getCoordinator(googleID);

		return coordinator.getName();
	}

	/**
	 * Returns the login page to the user, or the designated redirect page if
	 * the user is already logged in.
	 * 
	 * @param redirectPage
	 *            the page to redirect the user (Precondition: Must not be null)
	 * 
	 * @return the login page or redirect page if the user is already logged in
	 */
	public String getLoginPage(String redirectPage) {
		// Check if user is already logged in
		User user = userService.getCurrentUser();

		if (user != null) {
			// Direct user to his chosen page
			return redirectPage;
		}

		else {
			// Direct user to Google Login first before chosen page
			return userService.createLoginURL(redirectPage);
		}
	}

	/**
	 * Returns the logout page to the user.
	 * 
	 * @param redirectPage
	 *            the page to redirect the user (Precondition: Must not be null)
	 * 
	 * @return the redirect page after logout
	 */
	public String getLogoutPage(String redirectPage) {
		return userService.createLogoutURL(redirectPage);
	}

	/**
	 * Returns the Google User object.
	 * 
	 * @return the user
	 */
	public User getUser() {		
		return userService.getCurrentUser();
	}

	/**
	 * Returns the Administrator status of the user.
	 * 
	 * @return <code>true</code> if the user is an administrator,
	 *         <code>false</code> otherwise.
	 */
	public boolean isAdministrator() {
		if (userService.isUserAdmin()) {
			return true;
		}

		return false;
	}

	/**
	 * Returns the Coordinator status of the user.
	 * 
	 * @return <code>true</code> if the user is an coordinator,
	 *         <code>false</code> otherwise.
	 */
	//TODO: This seems very inefficient. Is there a better way? -damith
	public boolean isCoordinator() {
		// Get list of coordinators
		List<Coordinator> coordinatorList = getCoordinatorList();

		User user = userService.getCurrentUser();

		if (user == null)
			return false;

		// Check if user is in the list of coordinators
		for (Coordinator c : coordinatorList) {
			if (user.getNickname().equalsIgnoreCase(c.getGoogleID())) {
				return true;
			}
		}

		return false;
	}
	
	@Deprecated
	public void deleteCoordinatorNonCascade(String coordId) throws Exception {
		deleteCoord(coordId);
	}


	public void deleteCoord(String coordId){
		Coordinator coord = getCoordinator(coordId);
		if (coord == null) {
			String errorMessage = "Trying to delete non-existent coordinator: "
					+ coordId;
			Log.warn(errorMessage);
		}
		getPM().deletePersistent(coord);
	}

}