package teammates;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

/**
 * New Datastore class
 * 
 * Code taken from:
 * http://stackoverflow.com/questions/4185382/how-to-use-jdo-persistence-manager
 * 
 * @author nvquanghuy
 * 
 */
public class Datastore {
	private static PersistenceManagerFactory PMF = null;
	private static final ThreadLocal<PersistenceManager> PER_THREAD_PM = new ThreadLocal<PersistenceManager>();

	public static void initialize() {
		if (PMF != null) {
			throw new IllegalStateException("initialize() already called");
		}
		PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	}

	public static PersistenceManager getPersistenceManager() {

		PersistenceManager pm = PER_THREAD_PM.get();
		if (pm == null) {
			pm = PMF.getPersistenceManager();
			PER_THREAD_PM.set(pm);

		} else if (pm.isClosed()) {

			PER_THREAD_PM.remove();
			pm = PMF.getPersistenceManager();
			PER_THREAD_PM.set(pm);

		}
		return pm;
	}

	public static void finishRequest() {

		PersistenceManager pm = PER_THREAD_PM.get();
		if (pm != null) {
			PER_THREAD_PM.remove();

			//TODO: this line moved here due to unit testing problem
			//  to be reconsidered.
			if (!pm.isClosed()){
				Transaction tx = pm.currentTransaction();
				if (tx.isActive()) {
					tx.rollback();
				}
			//if (!pm.isClosed()){
				pm.close();
			}
		}
	}
}
