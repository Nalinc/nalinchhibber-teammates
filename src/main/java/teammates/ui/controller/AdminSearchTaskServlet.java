// Copyright 2011 Google Inc. All Rights Reserved.

package teammates.ui.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.mortbay.log.Log;

import teammates.common.Common;
import teammates.common.datatransfer.CoordData;
import teammates.storage.api.AccountsDb;
import teammates.storage.entity.Student;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.ListRequest;
import com.google.appengine.api.search.ListResponse;
import com.google.appengine.api.search.SearchServiceFactory;

public class AdminSearchTaskServlet extends ActionServlet<AdminHomeHelper> {

	private static final long serialVersionUID = 1L;

	private static final Index INDEX = SearchServiceFactory.getSearchService()
			.getIndex(IndexSpec.newBuilder().setName("coord_search_index"));

	

	private static final Logger LOG = Logger.getLogger(AdminSearchTaskServlet.class
			.getName());

	@Override
	protected AdminHomeHelper instantiateHelper() {
		return new AdminHomeHelper();
	}

	@Override
	public void doAction(HttpServletRequest req, AdminHomeHelper helper) {
		Log.debug("start rebuild search document");
		cleanupDocuments();
	    buildExistingDocument();
	    Log.debug("done rebuild search document");
		
	}

	/**
	 * Indexes a document built from the current request on behalf of the
	 * specified user. Each document has three fields in it. The content field
	 * stores used entered text. The email, and domain are extracted from the
	 * current user.
	 */
	private void buildExistingDocument() {

		AccountsDb accounts = new AccountsDb();
		List<CoordData> coords = accounts.getCoordinators();
		
		Iterator<CoordData> it = coords.iterator();
		while (it.hasNext()) {
			CoordData coord = it.next();
			addDocument(coord.name, coord.email, coord.id, Common.PAGE_COORD_HOME);
		}
		
		List<Student> students = accounts.getStudentEntities();
		Iterator<Student> it2 = students.iterator();
		while (it2.hasNext()) {
			Student stu = it2.next();
			addDocument(stu.getName(), stu.getEmail(), stu.getID(), Common.PAGE_STUDENT_HOME);
		}

	}
	
	private void addDocument(String name, String email, String id, String url) {
		Document.Builder docBuilder = Document
				.newBuilder()
				.addField(
						Field.newBuilder().setName("name")
								.setText(name))
				.addField(
						Field.newBuilder().setName("email")
								.setText(email))
				.addField(
						Field.newBuilder().setName("link").setHTML(
							String.format("<a href=\"%s\">View</a>",url+"?"+Common.PARAM_USER_ID+"="+id)
								))
				;

		Document doc = docBuilder.build();
		LOG.info("Adding document:\n" + doc.toString());
		try {
			INDEX.add(doc);
		} catch (RuntimeException e) {
			LOG.log(Level.SEVERE, "Failed to add " + doc, e);
		}
	}


	/**
	 * Removes documents with IDs specified in the given request. In the demo
	 * application we do not perform any authorization checks, thus no user
	 * information is necessary.
	 */
	private void cleanupDocuments() {
		
		try {
		    while (true) {
		        List<String> docIds = new ArrayList<String>();
		        // Return a set of document IDs.
		        ListRequest request = ListRequest.newBuilder().build();
		        ListResponse<Document> response = INDEX.listDocuments(request);
		        if (response.getResults().isEmpty()) {
		            break;
		        }
		        for (Document doc : response) {
		            docIds.add(doc.getId());
		        }
		        INDEX.remove(docIds);
		    }
		} catch (RuntimeException e) {
		    LOG.log(Level.SEVERE, "Failed to remove documents", e);
		}
	}


	@Override
	protected String getDefaultForwardUrl() {
		return Common.JSP_ADMIN_SEARCH;
	}

}