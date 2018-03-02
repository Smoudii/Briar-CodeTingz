package org.briarproject.briar.android.contact;


import org.briarproject.bramble.api.contact.Contact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class TestableContactListFragment extends ContactListFragment {

	public List<ContactListItem> filter(String charText, List<ContactListItem> contactsListItems) {
		List<ContactListItem> contacts = new ArrayList<>();
		final String CHAR_TEXT_LOWER = charText.toLowerCase(Locale.getDefault());
		for (ContactListItem c : contactsListItems) {
			if (c.getContact().getAuthor().getName().toLowerCase(Locale.getDefault())
					.contains(CHAR_TEXT_LOWER)) {
				contacts.add(c);
			}
		}
		return contacts;
	}
}