package org.briarproject.briar.android.contact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.briarproject.bramble.api.contact.Contact;
import org.briarproject.bramble.api.contact.ContactId;
import org.briarproject.bramble.api.contact.ContactManager;
import org.briarproject.bramble.api.contact.event.ContactRemovedEvent;
import org.briarproject.bramble.api.contact.event.ContactStatusChangedEvent;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.db.NoSuchContactException;
import org.briarproject.bramble.api.event.Event;
import org.briarproject.bramble.api.event.EventBus;
import org.briarproject.bramble.api.event.EventListener;
import org.briarproject.bramble.api.nullsafety.MethodsNotNullByDefault;
import org.briarproject.bramble.api.nullsafety.ParametersNotNullByDefault;
import org.briarproject.bramble.api.plugin.ConnectionRegistry;
import org.briarproject.bramble.api.plugin.event.ContactConnectedEvent;
import org.briarproject.bramble.api.plugin.event.ContactDisconnectedEvent;
import org.briarproject.briar.R;
import org.briarproject.briar.android.activity.ActivityComponent;
import org.briarproject.briar.android.contact.BaseContactListAdapter.OnContactClickListener;
import org.briarproject.briar.android.fragment.BaseFragment;
import org.briarproject.briar.android.keyagreement.KeyAgreementActivity;
import org.briarproject.briar.android.view.BriarRecyclerView;
import org.briarproject.briar.api.android.AndroidNotificationManager;
import org.briarproject.briar.api.client.BaseMessageHeader;
import org.briarproject.briar.api.client.MessageTracker.GroupCount;
import org.briarproject.briar.api.introduction.IntroductionRequest;
import org.briarproject.briar.api.introduction.IntroductionResponse;
import org.briarproject.briar.api.introduction.event.IntroductionRequestReceivedEvent;
import org.briarproject.briar.api.introduction.event.IntroductionResponseReceivedEvent;
import org.briarproject.briar.api.messaging.ConversationManager;
import org.briarproject.briar.api.messaging.PrivateMessageHeader;
import org.briarproject.briar.api.messaging.event.PrivateMessageReceivedEvent;
import org.briarproject.briar.api.sharing.InvitationRequest;
import org.briarproject.briar.api.sharing.InvitationResponse;
import org.briarproject.briar.api.sharing.event.InvitationRequestReceivedEvent;
import org.briarproject.briar.api.sharing.event.InvitationResponseReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.briarproject.briar.android.contact.ConversationActivity.CONTACT_ID;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ContactListFragment extends BaseFragment implements EventListener {

	public static final String TAG = ContactListFragment.class.getName();
	private static final Logger LOG = Logger.getLogger(TAG);

	@Inject
	ConnectionRegistry connectionRegistry;
	@Inject
	EventBus eventBus;
	@Inject
	AndroidNotificationManager notificationManager;

	private ContactListAdapter adapter;
	private BriarRecyclerView list;
	private DatabaseReference mRootRef;
	private ChildEventListener childEventListener;

	// Fields that are accessed from background threads must be volatile
	@Inject
	volatile ContactManager contactManager;
	@Inject
	volatile ConversationManager conversationManager;

	private DatabaseReference mUsersDatabase;
	private FirebaseAuth mAuth;

	public static ContactListFragment newInstance() {
		Bundle args = new Bundle();
		ContactListFragment fragment = new ContactListFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public ContactListAdapter getAdapter() {
		return adapter;
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		FirebaseApp.initializeApp(this.getContext());
		mAuth = FirebaseAuth.getInstance();
		mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("messages");
		mUsersDatabase.keepSynced(true);

		getActivity().setTitle(R.string.contact_list_button);

		View contentView = inflater.inflate(R.layout.list, container, false);

		mRootRef = FirebaseDatabase.getInstance().getReference();

		OnContactClickListener<ContactListItem> onContactClickListener =
				(view, item) -> {

					String contactName = item.getContact().getAuthor().getName();

					UserDetails.changeChatWithEmail(contactName.replaceAll("\\s",""));

					// Variables to get user from email
					String tempEmail;
					Pattern pattern;
					Matcher matcher;
					String emailToUserName= "";

					// Get username from email(i.e, ignore everything after @ inclusive from email)
					tempEmail = contactName.replaceAll("\\s","");
					pattern = Pattern.compile("([^@]+)");
					matcher = pattern.matcher(tempEmail);
					if (matcher.find()) {
						emailToUserName = matcher.group(1);
					}
					String dbSafeEmail = contactName.replaceAll("\\.", ",");
					UserDetails.changeChatWith(dbSafeEmail);

					Intent i = new Intent(getActivity(),
							ChatActivity.class);
					ContactId contactId = item.getContact().getId();
					i.putExtra(CONTACT_ID, contactId.getInt());

					startActivity(i);
				};
		adapter = new ContactListAdapter(getContext(), onContactClickListener);
		list = contentView.findViewById(R.id.list);
		list.setLayoutManager(new LinearLayoutManager(getContext()));
		list.setAdapter(adapter);
		list.setEmptyText(getString(R.string.no_contacts));

		return contentView;
	}

	@Override
	public void onResume(){
		super.onResume();
		UserDetails.changeChatWith("");
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.contact_list_actions, menu);
		super.onCreateOptionsMenu(menu, inflater);

		SearchView searchView = (SearchView) menu.findItem(R.id.action_search_contacts).getActionView();
		int id = searchView.getContext()
				.getResources()
				.getIdentifier("android:id/search_src_text", null, null);
		EditText searchEditText = (EditText) searchView.findViewById(id);
		searchEditText.setTextColor(getResources().getColor(R.color.briar_text_primary_inverse));
		searchEditText.setHintTextColor(getResources().getColor(R.color.briar_text_primary_inverse));

		searchView.setOnQueryTextListener(
				new SearchView.OnQueryTextListener() {
					@Override
					public boolean onQueryTextChange (String newText) {
						if (TextUtils.isEmpty(newText)) {
							filter("");
						}
						return true;
					}

					@Override
					public boolean onQueryTextSubmit(String query) {
						filter(query);
						return true;
					}
				}
		);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_add_contact:
				Intent intent =
						new Intent(getContext(), KeyAgreementActivity.class);
				startActivity(intent);
				return true;
			case R.id.action_sort_contact_alpha:
				loadContactsSortedAlpha();
				return true;
			case R.id.action_sort_contact_recent:
				loadContactsSortedRecent();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		eventBus.addListener(this);
		notificationManager.clearAllContactNotifications();
		notificationManager.clearAllIntroductionNotifications();
		loadContacts();
		list.startPeriodicUpdate();
	}

	@Override
	public void onStop() {
		super.onStop();
		eventBus.removeListener(this);
		adapter.clear();
		list.showProgressBar();
		list.stopPeriodicUpdate();
	}

	private void loadContactsSortedAlpha(){
		adapter.setSort("ALPHA");
		loadContacts();
	}

	private void loadContactsSortedRecent(){
		adapter.setSort("RECENT");
		loadContacts();
	}

	private void loadContacts() {
		int revision = adapter.getRevision();
		listener.runOnDbThread(() -> {
			try {
				long now = System.currentTimeMillis();
				List<ContactListItem> contacts = new ArrayList<>();
				for (Contact c : contactManager.getActiveContacts()) {
					try {
						ContactId id = c.getId();
						String email = c.getAuthor().getName();
						String dbEmail = email.replaceAll("\\.", ",");
						GroupCount count =
								conversationManager.getGroupCount(id);
						boolean connected = connectionRegistry.isConnected(c.getId());
						contacts.add(new ContactListItem(c, connected, count));
						System.out.println(mUsersDatabase.child(dbEmail).child("online"));

						mUsersDatabase.addValueEventListener (new ValueEventListener() {
							@Override
							public void onDataChange(DataSnapshot dataSnapshot) {
								if(dataSnapshot.child(dbEmail).child("online").getValue() == null) {
									//Do nothing, creating test data
								} else if (dataSnapshot.child(dbEmail).child("online").getValue().toString().equals("true")){
									setConnected(c.getId(), true);
								} else if(dataSnapshot.child(dbEmail).child("online").getValue().toString().equals("false")){
									setConnected(c.getId(), false);
								}
							}

							@Override
							public void onCancelled(DatabaseError databaseError) {

							}
						});

						setLatestMessage(c);
            
					} catch (NoSuchContactException e) {
						//continue
					}
				}
				long duration = System.currentTimeMillis() - now;
				if (LOG.isLoggable(INFO))
					LOG.info("Full load took " + duration + " ms");
				displayContacts(revision, contacts);
			} catch (DbException e) {
				if (LOG.isLoggable(WARNING)) LOG.log(WARNING, e.toString(), e);
			}
		});
	}

	private void displayContacts(int revision, List<ContactListItem> contacts) {
		runOnUiThreadUnlessDestroyed(() -> {
			if (revision == adapter.getRevision()) {
				adapter.incrementRevision();
				if (contacts.isEmpty()) list.showData();
				else adapter.setItems(contacts);
			} else {
				LOG.info("Concurrent update, reloading");
				loadContacts();
			}
		});
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ContactStatusChangedEvent) {
			ContactStatusChangedEvent c = (ContactStatusChangedEvent) e;
			if (c.isActive()) {
				LOG.info("Contact activated, reloading");
				loadContacts();
			} else {
				LOG.info("Contact deactivated, removing item");
				removeItem(c.getContactId());
			}
		} else if (e instanceof ContactConnectedEvent) {
			setConnected(((ContactConnectedEvent) e).getContactId(), true);
		} else if (e instanceof ContactDisconnectedEvent) {
			setConnected(((ContactDisconnectedEvent) e).getContactId(), false);
		} else if (e instanceof ContactRemovedEvent) {
			LOG.info("Contact removed, removing item");
			removeItem(((ContactRemovedEvent) e).getContactId());
		} else if (e instanceof PrivateMessageReceivedEvent) {
			LOG.info("Private message received, updating item");
			PrivateMessageReceivedEvent p = (PrivateMessageReceivedEvent) e;
			PrivateMessageHeader h = p.getMessageHeader();
			updateItem(p.getContactId(), h);
		} else if (e instanceof IntroductionRequestReceivedEvent) {
			LOG.info("Introduction request received, updating item");
			IntroductionRequestReceivedEvent m =
					(IntroductionRequestReceivedEvent) e;
			IntroductionRequest ir = m.getIntroductionRequest();
			updateItem(m.getContactId(), ir);
		} else if (e instanceof IntroductionResponseReceivedEvent) {
			LOG.info("Introduction response received, updating item");
			IntroductionResponseReceivedEvent m =
					(IntroductionResponseReceivedEvent) e;
			IntroductionResponse ir = m.getIntroductionResponse();
			updateItem(m.getContactId(), ir);
		} else if (e instanceof InvitationRequestReceivedEvent) {
			LOG.info("Invitation Request received, update item");
			InvitationRequestReceivedEvent m =
					(InvitationRequestReceivedEvent) e;
			InvitationRequest ir = m.getRequest();
			updateItem(m.getContactId(), ir);
		} else if (e instanceof InvitationResponseReceivedEvent) {
			LOG.info("Invitation response received, updating item");
			InvitationResponseReceivedEvent m =
					(InvitationResponseReceivedEvent) e;
			InvitationResponse ir = m.getResponse();
			updateItem(m.getContactId(), ir);
		}
	}

	private void updateItem(ContactId c, BaseMessageHeader h) {
		runOnUiThreadUnlessDestroyed(() -> {
			adapter.incrementRevision();
			int position = adapter.findItemPosition(c);
			ContactListItem item = adapter.getItemAt(position);
			if (item != null) {
				ConversationItem i = ConversationItem.from(getContext(), h);
				item.addMessage(i);
				adapter.updateItemAt(position, item);
			}
		});
	}

	private void removeItem(ContactId c) {
		runOnUiThreadUnlessDestroyed(() -> {
			adapter.incrementRevision();
			int position = adapter.findItemPosition(c);
			ContactListItem item = adapter.getItemAt(position);
			if (item != null) adapter.remove(item);
		});
	}

	private void setConnected(ContactId c, boolean connected) {
		runOnUiThreadUnlessDestroyed(() -> {
			adapter.incrementRevision();
			int position = adapter.findItemPosition(c);
			ContactListItem item = adapter.getItemAt(position);
			if (item != null) {
				item.setConnected(connected);
				adapter.notifyItemChanged(position);
			}
		});
	}

	private void setLastMessageAndDate(ContactId c, String lastMessage, long date) {
		runOnUiThreadUnlessDestroyed(() -> {
			adapter.incrementRevision();
			int position = adapter.findItemPosition(c);
			ContactListItem item = adapter.getItemAt(position);
			if (item != null) {
				item.setDate(date);
				item.setLastMessage(lastMessage);
				adapter.notifyItemChanged(position);
			}
		});
	}

	private void filter(String charText) {
		int revision = adapter.getRevision();
		final String CHAR_TEXT_LOWER = charText.toLowerCase(Locale.getDefault());
		listener.runOnDbThread(() -> {
			if (CHAR_TEXT_LOWER.length() == 0) {
				loadContacts();
			} else {
				try {
					long now = System.currentTimeMillis();
					List<ContactListItem> contacts = new ArrayList<>();
					for (Contact c : contactManager.getActiveContacts()) {
						try {
							ContactId id = c.getId();
							GroupCount count =
									conversationManager.getGroupCount(id);
							boolean connected =
									connectionRegistry.isConnected(c.getId());
							if (c.getAuthor().getName().toLowerCase(Locale.getDefault()).contains(CHAR_TEXT_LOWER)) {
								contacts.add(new ContactListItem(c, connected, count));
							}
						} catch (NoSuchContactException e) {
							// Continue
						}
					}
					long duration = System.currentTimeMillis() - now;
					if (LOG.isLoggable(INFO))
						LOG.info("Full load took " + duration + " ms");
					displayContacts(revision, contacts);
				} catch (DbException e) {
					if (LOG.isLoggable(WARNING)) LOG.log(WARNING, e.toString(), e);
				}
			}
		});
	}

	// Retrieve and set the latest message in the conversation
	private void setLatestMessage(Contact c){
		String chatWith = c.getAuthor().getName()
				.replaceAll("\\s","")
				.replaceAll("\\.", ",");
		String username = UserDetails.username;

		DatabaseReference messageRef = mRootRef.child("messages").child(username).child(chatWith);
		Query messageQuery = messageRef.orderByKey().limitToLast(1);
		messageRef.keepSynced(true);

		if (childEventListener != null) {
			messageQuery.removeEventListener(childEventListener);
		}


		childEventListener = new ChildEventListener() {
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				Message lastMessage = dataSnapshot.getValue(Message.class);
				if (lastMessage.getFrom().equals(UserDetails.username)) {
					setLastMessageAndDate(c.getId(), "You: " + lastMessage.getMessage(), lastMessage.getTime());
				} else {
					setLastMessageAndDate(c.getId(), lastMessage.getMessage(), lastMessage.getTime());
				}
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s) {

			}

			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot) {

			}

			@Override
			public void onChildMoved(DataSnapshot dataSnapshot, String s) {

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		};

		messageQuery.addChildEventListener(childEventListener);
	}
}
