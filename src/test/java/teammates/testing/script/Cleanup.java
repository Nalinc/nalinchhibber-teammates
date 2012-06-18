package teammates.testing.script;

import java.io.IOException;

import teammates.api.EntityDoesNotExistException;
import teammates.testing.lib.TMAPI;

/**
 * Clean up the remote's Teammates server's data
 * 
 * @author nvquanghuy
 * 
 */
public class Cleanup {
	public static void main(String args[]) throws IOException, EntityDoesNotExistException {
		TMAPI.cleanup();
		TMAPI.updateBumpRatio();
	}
}