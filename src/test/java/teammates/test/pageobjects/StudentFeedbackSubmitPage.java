package teammates.test.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import teammates.common.util.Constants;

public class StudentFeedbackSubmitPage extends AppPage {

	public StudentFeedbackSubmitPage(Browser browser) {
		super(browser);
	}

	@Override
	protected boolean containsExpectedPageContents() {
		return getPageSource().contains("<h1>Submit Feedback</h1>");
	}
	
	public void selectRecipient(int qnNumber, int responseNumber, String recipientName) {
		browser.selenium.select("name=" + Constants.PARAM_FEEDBACK_RESPONSE_RECIPIENT + 
				"-" + qnNumber + "-" + responseNumber, "label=" + recipientName);
	}
	
	public void fillQuestionTextBox(int qnNumber, int responseNumber, String text) {
		WebElement element = browser.driver.findElement(
				By.name(Constants.PARAM_FEEDBACK_RESPONSE_TEXT + "-" + qnNumber + "-" + responseNumber));
		fillTextBox(element, text);
	}
	
	public void clickSubmitButton() {
		WebElement button = browser.driver.findElement(By.id("response_submit_button"));
		button.click();
	}

}
