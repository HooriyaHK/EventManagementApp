package com.example.goldencarrot.views;

import static android.content.ContentValues.TAG;

import static com.example.goldencarrot.data.model.user.UserUtils.PARTICIPANT_TYPE;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goldencarrot.R;
import com.example.goldencarrot.controller.NotificationController;
import com.example.goldencarrot.controller.RanBackground;
import com.example.goldencarrot.data.db.NotificationRepository;
import com.example.goldencarrot.data.db.UserRepository;
import com.example.goldencarrot.data.model.event.Event;
import com.example.goldencarrot.data.model.event.EventRecyclerArrayAdapter;
import com.example.goldencarrot.data.model.notification.Notification;
import com.example.goldencarrot.data.model.user.UserImpl;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;/**
 * This activity represents the home screen for the event organizer. It displays the organizer's profile
 * and a list of events they have created. The user can manage their profile, create new events, or view details
 * of existing events. The events are fetched from Firestore, and the RecyclerView displays the event list.
 * The activity also handles user data loading and display.
 */
public class OrganizerHomeView extends AppCompatActivity {

    private Button manageProfileButton, createEventButton, sendAllNotifsBtn;
    private TextView usernameTextView;
    private RecyclerView recyclerView;
    private EventRecyclerArrayAdapter eventAdapter;

    // Firestore
    private FirebaseFirestore firestore;
    private String deviceId;
    private List<Event> eventList = new ArrayList<>();
    private UserRepository userRepository;
    private NotificationRepository notifRepo;

    /**
     * Called when the activity is created. Initializes Firestore, UI components, and loads user data.
     * It also sets up listeners for managing profile, creating events, and displaying events.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.organizer_home_view);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        Log.d(TAG, "Firestore initialized");
        deviceId = getDeviceId(this);
        userRepository = new UserRepository();
        notifRepo = new NotificationRepository(firestore);

        // Apply RNG Background
        LinearLayout rootLayout = findViewById(R.id.root_layout);
        Drawable randomBackground = RanBackground.getRandomBackground(this);
        if (randomBackground != null) {
            rootLayout.setBackground(randomBackground);
        }

        // Initialize the views from layout file
        manageProfileButton = findViewById(R.id.manageFacilityProfileBtn);
        createEventButton = findViewById(R.id.button_create_event);
        usernameTextView = findViewById(R.id.organizer_user_name_textView);
        sendAllNotifsBtn = findViewById(R.id.sendNotificationToAllEntrantsButton);

        // Event lists and adapter Initialization
        recyclerView = findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // Set adapters to listview
        eventAdapter = new EventRecyclerArrayAdapter(this, eventList, event -> {
            // Long click to open details
            Intent intent = new Intent(OrganizerHomeView.this, OrganizerEventDetailsActivity.class);
            Log.d("OrganizerHomeView", "opening event: " + event.getEventName());
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });

        recyclerView.setAdapter(eventAdapter);
        // Set an OnClickListener for the button
        manageProfileButton.setOnClickListener(v -> {
            // Start ManageProfileActivity
            Intent intent = new Intent(OrganizerHomeView.this, FacilityProfileActivity.class);
            intent.putExtra("userId", deviceId);
            startActivity(intent);
        });

        createEventButton.setOnClickListener(v -> {
            createEvent();
        });
        sendAllNotifsBtn.setOnClickListener(view -> sendAllEntrantsNotification());

        // Set user name
        loadUserData();

    }
    @Override
    protected void onResume() {
        super.onResume();
        fetchEventData(); // Refresh event data when returning to the activity
    }

    private void fetchEventData() {
        firestore.collection("events").whereEqualTo("organizerId", deviceId)
                .get().addOnSuccessListener(querySnapshot -> {
                    eventList.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String eventId = document.getId();
                        String eventName = document.getString("eventName");
                        String location = document.getString("location");
                        String eventDetails = document.getString("eventDetails");
                        String dateString = document.getString("date");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                        try {
                            Date eventDate = dateFormat.parse(dateString);
                            Event event = new Event(null, eventName, location, eventDate, eventDetails, R.drawable.poster_placeholder);
                            event.setEventId(eventId);
                            loadPoster(event, eventId);
                            eventList.add(event);
                        } catch (ParseException e) {
                            Log.e(TAG, "Date parsing error: " + e.getMessage(), e);
                        }
                    }
                    eventAdapter.notifyDataSetChanged();
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }
    private void loadPoster(Event event, String eventId) {
        String posterPath = "posters/" + eventId + "_poster.jpg";
        String updatedPosterPath = "posters/" + eventId + "_updated_poster.jpg";
        StorageReference updatedPosterRef = FirebaseStorage.getInstance().getReference(updatedPosterPath);

        updatedPosterRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    event.setPosterUrl(uri.toString());
                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    StorageReference posterRef = FirebaseStorage.getInstance().getReference(posterPath);
                    posterRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                event.setPosterUrl(uri.toString());
                                eventAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e1 -> Log.w(TAG, "Failed to fetch poster URL", e1));
                });
    }


    /**
     * Loads user data from Firestore based on the device ID.
     * If data is found, it sets the username and proceeds to load the organizer's events.
     * Displays appropriate error messages if user data is missing or the document does not exist.
     */
    private void loadUserData() {
        firestore.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Manually construct UserImpl with data from Firestore to handle Optional fields
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String userType = documentSnapshot.getString("userType");
                        String phoneNumber = documentSnapshot.getString("phoneNumber"); // Firestore stores as String
                        Boolean notificationAdministrator = documentSnapshot.getBoolean("administratorNotification");
                        Boolean notificationOrganizer = documentSnapshot.getBoolean("organizerNotification");
                        String profileImage = documentSnapshot.getString("userProfileImage");

                        // Phone number to optional string
                        Optional<String> optionalPhoneNumber = (phoneNumber != null && !phoneNumber.isEmpty())
                                ? Optional.of(phoneNumber)
                                : Optional.empty();
                        try {
                            UserImpl user = new UserImpl(email, userType, name, optionalPhoneNumber, notificationAdministrator, notificationOrganizer, profileImage);
                            if (user.getName() != null) {
                                usernameTextView.setText(user.getName());
                                Log.d(TAG, "Username loaded: " + user.getName());
                                loadEventsForOrganizer(user);
                            } else {
                                Log.w(TAG, "Username field is missing in the document");
                                usernameTextView.setText("Error: Username not found");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error creating UserImpl object: " + e.getMessage(), e);
                            usernameTextView.setText("Error loading user data");
                        }
                    } else {
                        Log.e(TAG, "Document does not exist");
                        usernameTextView.setText("Error: User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data", e);
                    usernameTextView.setText("Error fetching user data");
                });
    }

    /**
     * Loads events associated with the organizer from Firestore.
     * The events are filtered based on the organizer's device ID and displayed in a RecyclerView.
     *
     * @param organizer The organizer whose events are to be loaded.
     */
    private void loadEventsForOrganizer(UserImpl organizer) {
        firestore.collection("events").whereEqualTo("organizerId", deviceId)
                .get().addOnSuccessListener(querySnapshot -> {
                    eventList.clear(); // Clear the list to avoid duplicates

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String eventId = document.getId();
                        String eventName = document.getString("eventName");
                        String location = document.getString("location");
                        String eventDetails = document.getString("eventDetails");
                        String dateString = document.getString("date");
                        String posterPath = "posters/" + eventId + "_poster.jpg"; // Firebase Storage path for the original poster
                        String updatedPosterPath = "posters/" + eventId + "_updated_poster.jpg"; // Firebase Storage path for the updated poster
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                        try {
                            Date eventDate = dateFormat.parse(dateString);
                            Event event = new Event(organizer, eventName, location, eventDate, eventDetails, R.drawable.poster_placeholder);
                            event.setEventId(eventId);

                            // Try fetching the updated poster URL first
                            StorageReference updatedPosterRef = FirebaseStorage.getInstance().getReference(updatedPosterPath);
                            updatedPosterRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        event.setPosterUrl(uri.toString());
                                        eventAdapter.notifyDataSetChanged(); // Notify adapter of the changes
                                    })
                                    .addOnFailureListener(e -> {
                                        // If the updated poster doesn't exist, fall back to the original one
                                        StorageReference posterRef = FirebaseStorage.getInstance().getReference(posterPath);
                                        posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                            event.setPosterUrl(uri.toString());
                                            eventAdapter.notifyDataSetChanged(); // Notify adapter of the changes
                                        }).addOnFailureListener(e1 -> {
                                            Log.w(TAG, "Failed to fetch poster URL for event: " + eventName, e1);
                                        });
                                    });

                            eventList.add(event);
                        } catch (ParseException e) {
                            Log.e(TAG, "Date parsing error: " + e.getMessage(), e);
                        }
                    }

                    eventAdapter.notifyDataSetChanged(); // Notify adapter of the updates
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events for organizer", e);
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Retrieves the Android device ID.
     *
     * @param context The application context.
     * @return The device ID as a string.
     */
    private String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * sends all entrants a notification
     */
    private void sendAllEntrantsNotification() {
        userRepository.getAllUsersFromFirestore(new UserRepository.FirestoreCallbackAllUsers() {
            @Override
            public void onSuccess(List<DocumentSnapshot> listOfUsers) {
                for (DocumentSnapshot user : listOfUsers) {
                    if (user.getString("userType").equals(PARTICIPANT_TYPE))
                        if (user.getBoolean("organizerNotification")) {
                            sendNotification(user.getId());
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("OrganizerHomeView", "failed to get all users");
            }
        });
    }
    private void sendNotification(String userId) {
        NotificationController notifController = new NotificationController();
        Notification notification = notifController.getOrCreateNotification(userId);
        notifRepo.addNotification(notification, new NotificationRepository.NotificationCallback<Notification>() {
            @Override
            public void onSuccess(Notification result) {
                Toast.makeText(OrganizerHomeView.this, "added notification", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerHomeView.this, "Notification not added", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void createEvent() {
        firestore.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.getString("facilityName") != null) {
                            Intent intent = new Intent(OrganizerHomeView.this, OrganizerCreateEvent.class);
                            intent.putExtra("userId", deviceId);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "To create an event, first create a facility profile!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG,"error getting current user");
                    }
                });
    }
}
