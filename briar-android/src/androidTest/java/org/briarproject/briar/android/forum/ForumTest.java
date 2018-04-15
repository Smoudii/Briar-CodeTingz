package org.briarproject.briar.android.forum;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.briarproject.briar.R;
import org.briarproject.briar.android.LoginTestSetup;
import org.briarproject.briar.android.ToolbarEspressoHelper;
import org.briarproject.briar.android.splash.SplashScreenActivity;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ForumTest {

	@Rule
	public ActivityTestRule<SplashScreenActivity> mActivityTestRule =
			new ActivityTestRule<>(SplashScreenActivity.class);

	@Before
	public void setUp() {

		// SECTION: Log in if necessary

		Boolean loginTestSetup = LoginTestSetup.isUserAlreadyLoggedIn();

		if(!loginTestSetup) {
			// Allow app to open up properly in emulator
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Select Log in instead
			ViewInteraction appCompatButton = onView(allOf(withId(R.id.btn_log_in),
					childAtPosition(childAtPosition(withClassName(is("android.widget.ScrollView")),
							0), 5)));
			appCompatButton.perform(scrollTo(), click());

			// Allow page to be redirected
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Enter email address
			ViewInteraction textInputEditText2 = onView(allOf(withId(R.id.edit_email),
					childAtPosition(childAtPosition(withId(R.id.email_layout), 0), 0)));
			textInputEditText2.perform(scrollTo(), replaceText("laxman@laxman.lax"), closeSoftKeyboard());

			// Enter password
			ViewInteraction textInputEditText4 = onView(allOf(withId(R.id.edit_password),
					childAtPosition(childAtPosition(withId(R.id.password_layout), 0), 0)));
			textInputEditText4.perform(scrollTo(), replaceText("onetwothree"), closeSoftKeyboard());

			// Click log in
			ViewInteraction appCompatButton2 = onView(allOf(withId(R.id.btn_sign_in),
					childAtPosition(childAtPosition(withClassName(is("android.widget.ScrollView")),
							0), 5)));
			appCompatButton2.perform(scrollTo(), click());


		}
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// SECTION 2: Open Blog in navigation for each test

		// Click navigation menu
		ViewInteraction appCompatImageButton = onView(allOf(childAtPosition(allOf(
				withId(R.id.toolbar), childAtPosition(withClassName(
						is("android.support.design.widget.AppBarLayout")), 0)), 1)));
		appCompatImageButton.perform(click());

		// Select the blogs option in the navigation menu
		ViewInteraction navigationMenuItemView3 = onView(childAtPosition(allOf(withId(
				R.id.design_navigation_view), childAtPosition(
				withId(R.id.navigation), 0)), 3));
		navigationMenuItemView3.perform(scrollTo(), click());

		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assert that the title of the toolbar is "Forums"
		ToolbarEspressoHelper.matchToolbarTitle("Forums").check(matches(isDisplayed()));
	}

	@Test
	public void A_CreateForum() {

		// Wait for page to load
		try {
			Thread.sleep(7500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assert that create forum button is displayed
		ViewInteraction createForumButton = onView(allOf(withId(R.id.action_create_forum),
				withContentDescription("Create Forum"), childAtPosition(childAtPosition(
						withId(R.id.toolbar), 2), 0)));
		createForumButton.check(matches(isDisplayed()));

		// Click create forum button
		createForumButton.perform(click());

		// Wait for create forum page to load
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assert that the back button is displayed
		ViewInteraction backButton = onView(allOf(withContentDescription("Navigate up")));
		backButton.check(matches(isDisplayed()));

		// Assert that the title of the toolbar is "Create Forum"
		ToolbarEspressoHelper.matchToolbarTitle("Create Forum").check(matches(isDisplayed()));

		// Assert that the forum name input is displayed
		ViewInteraction forumNameField = onView(allOf(withId(R.id.createForumNameEntry),
				childAtPosition(childAtPosition(withId(R.id.createForumNameLayout), 0), 0)));
		forumNameField.check(matches(isDisplayed()));

		// Assert that the form description is displayed
		ViewInteraction forumDescriptionField = onView(allOf(withId(R.id.createForumDescriptionEntry),
				childAtPosition(childAtPosition(withId(R.id.createForumDescriptionLayout), 0), 0)));
		forumDescriptionField.check(matches(isDisplayed()));

		// Write "FormTest" as the forum name
		ViewInteraction forumNameInput = onView(allOf(withId(R.id.createForumNameEntry),
				childAtPosition(childAtPosition(withId(R.id.createForumNameLayout), 0), 0)));
		forumNameInput.perform(replaceText("ForumTest"), closeSoftKeyboard());

		// Write "Test Description" as the forum description
		ViewInteraction forumDescriptionInput = onView(allOf(withId(R.id.createForumDescriptionEntry),
				childAtPosition(childAtPosition(withId(R.id.createForumDescriptionLayout), 0), 0)));
		forumDescriptionInput.perform(replaceText("TestDescription"), closeSoftKeyboard());

		// Wait to allow create button to be displayable
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Click the create forum button
		ViewInteraction createForum = onView(allOf(withId(R.id.createForumButton),
				withText("Create Forum"), childAtPosition(childAtPosition(withClassName(
						is("org.briarproject.briar.android.widget.TapSafeFrameLayout")), 0), 2),
						isDisplayed()));
		createForum.perform(click());

		// Allow the forum list to load
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assert that the back button is displayed
		ViewInteraction backButtonTwo = onView(allOf(withContentDescription("Navigate up")));
		backButtonTwo.check(matches(isDisplayed()));

		// Assert that the title of the toolbar is "ForumTest"
		ToolbarEspressoHelper.matchToolbarTitle("ForumTest").check(matches(isDisplayed()));

		// Assert that the back button is displayed
		ViewInteraction backButtonThree = onView(allOf(withContentDescription("Navigate up")));
		backButtonThree.perform(click());

		// Wait for the forum list page to load
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assert that the forum exists in the list
		ViewInteraction forumGroup = onView(allOf(childAtPosition(allOf(withId(R.id.recyclerView),
				childAtPosition(IsInstanceOf.<View>instanceOf(android.widget.RelativeLayout.class),
						0)), 0)));
		forumGroup.check(matches(isDisplayed()));

		// Assert the name of the forum
		ViewInteraction forumName = onView(allOf(withId(R.id.forumNameView), withText("ForumTest"),
				childAtPosition(childAtPosition(withId(R.id.recyclerView), 0), 1)));
		forumName.check(matches(isDisplayed()));

		// Assert the description of the forum
		ViewInteraction forumDescription = onView(allOf(withId(R.id.forumDescView),
				withText("TestDescription"), childAtPosition(childAtPosition(
						withId(R.id.recyclerView), 0), 2)));
		forumDescription.check(matches(isDisplayed()));
	}

	@Test
	public void B_SendForumMessage() {

		// Wait for page to load
		try {
			Thread.sleep(7500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assert the forum exists
		ViewInteraction forumGroupList = onView(allOf(childAtPosition(allOf(
				withId(R.id.recyclerView), childAtPosition(IsInstanceOf.<View>instanceOf(
						android.widget.RelativeLayout.class), 0)), 0)));
		forumGroupList.check(matches(isDisplayed()));

		// Assert the forum name in the list
		ViewInteraction forumName = onView(allOf(withId(R.id.forumNameView), withText("ForumTest"),
				childAtPosition(childAtPosition(withId(R.id.recyclerView), 0), 1)));
		forumName.check(matches(isDisplayed()));

		// Assert the forum description in the list
		ViewInteraction forumDescription = onView(allOf(withId(R.id.forumDescView),
				withText("TestDescription"), childAtPosition(childAtPosition(withId(
						R.id.recyclerView), 0), 2)));
		forumDescription.check(matches(isDisplayed()));

		// Click the forum group
		ViewInteraction forumGroupSelect = onView(allOf(withId(R.id.recyclerView), childAtPosition(
								withClassName(is("android.widget.RelativeLayout")), 0)));
		forumGroupSelect.perform(actionOnItemAtPosition(0, click()));

		// Wait for the forum group to load
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assert that the back button is displayed
		ViewInteraction backButton = onView(allOf(withContentDescription("Navigate up")));
		backButton.check(matches(isDisplayed()));

		// Assert that the title of the toolbar is "ForumTest"
		ToolbarEspressoHelper.matchToolbarTitle("ForumTest").check(matches(isDisplayed()));

		// Assert that the emoji button is displayed
		ViewInteraction emojiButton = onView(allOf(withId(R.id.emoji_toggle), childAtPosition(
				childAtPosition(withId(R.id.text_input_container), 1), 0)));
		emojiButton.check(matches(isDisplayed()));

		// Assert that the input box is displayed
		ViewInteraction forumMessageInput = onView(allOf(withId(R.id.input_text)));
		forumMessageInput.check(matches(isDisplayed()));

		// Assert that the send button is displayed
		ViewInteraction forumSendMessageButton = onView(allOf(withId(R.id.btn_send),
				withContentDescription("Send"), childAtPosition(childAtPosition(
						withId(R.id.text_input_container), 1), 2)));
		forumSendMessageButton.check(matches(isDisplayed()));

		// Write message "Forum test" in input box
		ViewInteraction forumMessage = onView(allOf(withId(R.id.input_text), childAtPosition(
				childAtPosition(withId(R.id.text_input_container), 1), 1), isDisplayed()));
		forumMessage.perform(replaceText("Forum test"), closeSoftKeyboard());

		// Click the send message button
		ViewInteraction sendMessageButton = onView(allOf(withId(R.id.btn_send),
				withContentDescription("Send"), isDisplayed()));
		sendMessageButton.perform(click());

		// Wait for the message to appear in list
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assert that the message was displayed
		ViewInteraction displayedMessage = onView(allOf(withId(R.id.text), withText("Forum test")));
		displayedMessage.check(matches(isDisplayed()));

		// Assert that the author is correct
		ViewInteraction displayedAuthor = onView(allOf(withId(R.id.authorName),
				withText("laxman@laxman.lax")));
		displayedAuthor.check(matches(isDisplayed()));

		// Assert that the reply button is displayed
		ViewInteraction forumMessageReplyButton = onView(allOf(withId(R.id.btn_reply),
				withText("Reply")));
		forumMessageReplyButton.check(matches(isDisplayed()));

		// Assert that the back button is displayed
		ViewInteraction backButtonTwo = onView(allOf(withContentDescription("Navigate up")));
		backButtonTwo.perform(click());
	}

	@Test
	public void C_ReplyForumMessage() {

		// Wait for page to load
		try {
			Thread.sleep(7500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assert that the forum group is in the list
		ViewInteraction forumGroupList = onView(allOf(childAtPosition(allOf(
				withId(R.id.recyclerView), childAtPosition(IsInstanceOf.<View>instanceOf(
						android.widget.RelativeLayout.class), 0)), 0)));
		forumGroupList.check(matches(isDisplayed()));

		// Assert that the forum name is correct
		ViewInteraction forumName = onView(allOf(withId(R.id.forumNameView), withText("ForumTest"),
				childAtPosition(childAtPosition(withId(R.id.recyclerView), 0), 1)));
		forumName.check(matches(isDisplayed()));

		// Assert that the test description is correct
		ViewInteraction forumDescription = onView(allOf(withId(R.id.forumDescView),
				withText("TestDescription"), childAtPosition(childAtPosition(withId(
						R.id.recyclerView), 0), 2)));
		forumDescription.check(matches(isDisplayed()));

		// Click the forum group
		ViewInteraction selectForumGroup = onView(allOf(withId(R.id.recyclerView), childAtPosition(
				withClassName(is("android.widget.RelativeLayout")), 0)));
		selectForumGroup.perform(actionOnItemAtPosition(0, click()));

		// Allow the forum group page to load
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assert that the back button is displayed
		ViewInteraction backButton = onView(allOf(withContentDescription("Navigate up")));
		backButton.check(matches(isDisplayed()));

		// Assert that the emoji button is displayed
		ViewInteraction emojiButton = onView(allOf(withId(R.id.emoji_toggle), childAtPosition(
				childAtPosition(withId(R.id.text_input_container), 1), 0)));
		emojiButton.check(matches(isDisplayed()));

		// Assert that the forum message input is displayed
		ViewInteraction forumMessageInputBox = onView(allOf(withId(R.id.input_text)));
		forumMessageInputBox.check(matches(isDisplayed()));

		// Assert that the send button is displayed
		ViewInteraction sendMessageButton = onView(allOf(withId(R.id.btn_send),
				withContentDescription("Send"), childAtPosition(childAtPosition(
						withId(R.id.text_input_container), 1), 2)));
		sendMessageButton.check(matches(isDisplayed()));

		// Assert that the reply button is displayed
		ViewInteraction replyForumButton = onView(allOf(withId(R.id.btn_reply), withText("Reply")));
		replyForumButton.check(matches(isDisplayed()));

		// Click reply
		replyForumButton.perform(click());

		// Enter "Forum reply" as the reply to the message
		ViewInteraction forumReplyMessage = onView(allOf(withId(R.id.input_text), childAtPosition(
				childAtPosition(withId(R.id.text_input_container), 1), 1), isDisplayed()));
		forumReplyMessage.perform(replaceText("Forum reply"), closeSoftKeyboard());

		// Click the send button
		ViewInteraction sendReplyButton = onView(allOf(withId(R.id.btn_send),
				withContentDescription("Send"), isDisplayed()));
		sendReplyButton.perform(click());

		// Wait for the reply to appear in the list
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assert that the the reply message appeared correctly
		ViewInteraction forumReplyDisplayed = onView(allOf(withId(R.id.text), withText(
				"Forum reply"), childAtPosition(childAtPosition(withId(R.id.layout), 1), 0)));
		forumReplyDisplayed.check(matches(isDisplayed()));

		// Assert that the back button is displayed
		ViewInteraction appCompatImageButton8 = onView(allOf(withContentDescription("Navigate up")));
		appCompatImageButton8.perform(click());
	}

	private static Matcher<View> childAtPosition(
			final Matcher<View> parentMatcher, final int position) {

		return new TypeSafeMatcher<View>() {
			@Override
			public void describeTo(Description description) {
				description.appendText(
						"Child at position " + position + " in parent ");
				parentMatcher.describeTo(description);
			}

			@Override
			public boolean matchesSafely(View view) {
				ViewParent parent = view.getParent();
				return parent instanceof ViewGroup &&
						parentMatcher.matches(parent)
						&&
						view.equals(((ViewGroup) parent).getChildAt(position));
			}
		};
	}
}
