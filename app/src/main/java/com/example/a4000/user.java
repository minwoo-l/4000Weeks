package com.example.a4000;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

public class user extends AppCompatActivity {

    // Member variables for Firebase and UI
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private ValueEventListener eventListener;

    // The one and only RecyclerView
    private RecyclerView grandParentRecyclerView;

    // Variables for date calculation
    private String dob;
    private int day, month, year;
    private Period age;
    private LocalDate birthday, today;

    private static final String USERS = "users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initialize UI components and Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference(USERS);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Logout button functionality
        // Check if user is logged in
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), login.class);
            startActivity(intent);
            finish();
        } else {
            // User is logged in, fetch their data
            setupUserDataListener();
        }

        // Inside onCreate() in user.java, after your other setup code

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // This sets the "Home" icon as the selected one when the activity starts
        bottomNav.setSelectedItemId(R.id.navigation_home);

        // Listener for item clicks
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // User clicked Profile, start the ProfileActivity
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                // This prevents a flashy animation between activities, making it feel like a single app
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });


        FloatingActionButton fab = findViewById(R.id.fab_add_image);
        fab.setOnClickListener(v -> {
            // 1. Get today's date
            LocalDate today = LocalDate.now();
            int currentYear = today.getYear();
            int currentMonth = today.getMonthValue();
            int dayOfMonth = today.getDayOfMonth();

            // 2. Calculate the current week number (1-4)
            int currentWeek = (dayOfMonth - 1) / 7 + 1;

            // 3. Format the strings exactly as the ImageAdd activity expects them
            String ymString = currentYear + "." + currentMonth;
            String weekString = "Week " + currentWeek;

            // 4. Create an intent to open the ImageAdd screen
            Intent intent = new Intent(user.this, ImageAdd.class);

            // 5. Pass the current week's data to the next screen
            intent.putExtra("YM", ymString);
            intent.putExtra("WEEK", weekString);

            // 6. Launch the activity
            startActivity(intent);
        });
    }

    private void setupUserDataListener() {
        // Create a NEW reference that points directly to the logged-in user's data
        DatabaseReference userSpecificRef = databaseReference.child(user.getUid());

        // Attach the listener to this SPECIFIC reference
        eventListener = userSpecificRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the data actually exists before trying to read it
                if (!snapshot.exists()) {
                    Toast.makeText(user.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // The snapshot is now the data for the user
                dob = snapshot.child("dob").getValue(String.class);

                // Add a null check for dob, in case it doesn't exist for a user
                if (dob == null || dob.isEmpty()) {
                    Toast.makeText(user.this, "Date of birth not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] parts_of_dob = dob.split("/");
                month = Integer.parseInt(parts_of_dob[0]);
                day = Integer.parseInt(parts_of_dob[1]);
                year = Integer.parseInt(parts_of_dob[2]);

                birthday = LocalDate.of(year, month, day);
                today = LocalDate.now();
                age = Period.between(birthday, today);

                // Find the RecyclerView in the layout
                grandParentRecyclerView = findViewById(R.id.grandParentRecyclerView);
                grandParentRecyclerView.setLayoutManager(new LinearLayoutManager(user.this));

                // Call prepareData to build the flat list of items
                ArrayList<DisplayableItem> allItems = prepareData();

                // Create and set the new UnifiedAdapter
                UnifiedAdapter adapter = new UnifiedAdapter(allItems);
                grandParentRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // This will now clearly show permission errors if they happen
                Log.e("FirebaseError", "Database access was cancelled: ", error.toException());
                Toast.makeText(user.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private ArrayList<DisplayableItem> prepareData() {
        ArrayList<DisplayableItem> allItems = new ArrayList<>();
        LocalDate startDate = birthday;
        LocalDate endDate = today;

        for (int currentYear = startDate.getYear(); currentYear <= endDate.getYear(); currentYear++) {
            ArrayList<ParentItem> parentsForThisYear = new ArrayList<>();
            int startMonth = (currentYear == startDate.getYear()) ? startDate.getMonthValue() : 1;
            int endMonth = (currentYear == endDate.getYear()) ? endDate.getMonthValue() : 12;

            // Loop through the months of this year
            for (int currentMonth = startMonth; currentMonth <= endMonth; currentMonth++) {
                // Default to 4 weeks for all past months
                int numberOfWeeksToGenerate = 4;

                // If we are in the CURRENT year AND the CURRENT month...
                if (currentYear == endDate.getYear() && currentMonth == endDate.getMonthValue()) {
                    // ...calculate which week of the month it is.
                    int dayOfMonth = endDate.getDayOfMonth();
                    // This simple formula calculates the week number (e.g., day 1-7 is week 1, 8-14 is week 2)
                    numberOfWeeksToGenerate = (dayOfMonth - 1) / 7 + 1;
                }

                ArrayList<ChildItem> weeksInMonth = new ArrayList<>();
                for (int weekNum = 1; weekNum <= numberOfWeeksToGenerate; weekNum++) {
                    weeksInMonth.add(new ChildItem("Week " + weekNum, R.drawable.ic_action_name));
                }

                if (!weeksInMonth.isEmpty()) {
                    String monthTitle = currentYear + "." + currentMonth;
                    parentsForThisYear.add(new ParentItem(monthTitle, R.drawable.calendar, weeksInMonth));
                }
            }

            if (!parentsForThisYear.isEmpty()) {
                String yearTitle = String.valueOf(currentYear);
                GrandParentItem grandParentItem = new GrandParentItem(yearTitle, parentsForThisYear, R.drawable.ic_action_name);
                allItems.add(grandParentItem);
            }
        }
        return allItems;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // It's good practice to remove the listener when the activity is destroyed
        // to prevent memory leaks
        if (databaseReference != null && eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }
    }
}


//package com.example.a4000;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.lang.reflect.Array;
//import java.time.LocalDate;
//import java.time.Period;
//import java.util.ArrayList;
//import java.util.List;
//
//public class user extends AppCompatActivity
//{
//
//    FirebaseAuth mAuth;
//    Button button;
//    TextView textView;
//    FirebaseUser user;
//    FirebaseDatabase firebaseDatabase;
//    DatabaseReference databaseReference;
//
//    ValueEventListener eventListener;
//
//    private RecyclerView grandParentRecyclerView;
//
//
//    private String userName;
//
//    String dob;
//    private static final String USERS = "users";
//
//    private int day,month,year;
//
//    private Period age;
//
//    private LocalDate birthday, today;
//
//    BottomNavigationView bottomNavigationView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_user);
//        button = findViewById(R.id.logout);
//
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FirebaseAuth.getInstance().signOut();
//                Intent intent = new Intent(getApplicationContext(), login.class);
//                startActivity(intent);
//                finish();
//            }
//        });
//        firebaseDatabase = FirebaseDatabase.getInstance();
//        databaseReference = firebaseDatabase.getReference(USERS);
//
//        mAuth = FirebaseAuth.getInstance();
//        user = mAuth.getCurrentUser();
//
////        textView = findViewById(R.id.user_details);
//
//        if (user == null)
//        {
//            Intent intent = new Intent(getApplicationContext(), login.class);
//            startActivity(intent);
//            finish();
//        }
//        else
//        {
////            textView.setText(user.getEmail());
//
//            userName = user.getDisplayName();
//
//            eventListener = databaseReference.addValueEventListener(new ValueEventListener()
//            {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot)
//                {
////                    dob = snapshot.child(userName).child("dob").getValue(String.class);
//                    dob = snapshot.child(user.getUid()).child("dob").getValue(String.class);
//                    Log.d("dob", "dob" + dob);
//                    String parts_of_dob[] = dob.split("/");
//
//                    month = Integer.parseInt(parts_of_dob[0]);
//                    day = Integer.parseInt(parts_of_dob[1]);
//                    year = Integer.parseInt(parts_of_dob[2]);
//
//                    Log.d("age1", "year " + year);
//                    Log.d("age1", "month " + month);
//                    Log.d("age1", "day " + day);
//
//
//
//                    birthday = LocalDate.of(year,month,day);
//                    today = LocalDate.now();
//
//                    Log.d("age1", "birthday" + birthday);
//                    Log.d("age1", "today" + today);
//
//                    age = Period.between(birthday, today);
//
//                    Log.d("age1", "age" + age);
//
//                    // ADD THIS NEW, CORRECTED CODE IN ITS PLACE
//
//                    // We only need one RecyclerView now
//                    grandParentRecyclerView = findViewById(R.id.grandParentRecyclerView);
//                    grandParentRecyclerView.setLayoutManager(new LinearLayoutManager(user.this));
//
//                    // 1. Call your new prepareData method to get the list
//                    ArrayList<DisplayableItem> allItems = prepareData();
//
//                    // 2. Create an instance of your new UnifiedAdapter, passing it the new list
//                    UnifiedAdapter adapter = new UnifiedAdapter(allItems);
//
//                    // 3. Set the new adapter on your RecyclerView
//                    grandParentRecyclerView.setAdapter(adapter);
//
//
////                    parentRecyclerView = findViewById(R.id.parentRecyclerView);
////                    parentRecyclerView.setHasFixedSize(true);
////                    parentRecyclerView.setLayoutManager(new LinearLayoutManager(user.this));
////                    parentList = new ArrayList<>();
//////                    prepareData();
//////                    ParentRecyclerViewAdapter adapter = new ParentRecyclerViewAdapter(parentList);
//////                    parentRecyclerView.setAdapter(adapter);
////
////                    grandParentRecyclerView = findViewById(R.id.grandParentRecyclerView);
////                    grandParentRecyclerView.setHasFixedSize(true);
////                    grandParentRecyclerView.setLayoutManager(new LinearLayoutManager(user.this));
////                    grandParentItemList = new ArrayList<>();
////                    prepareData();
////                    GrandParentRecyclerViewAdapter adapter = new GrandParentRecyclerViewAdapter(grandParentItemList);
////                    grandParentRecyclerView.setAdapter(adapter);
//
//
//
//                }
//
//
//
//
//
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Toast.makeText(user.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//
//
//
//    }
//
//    // Add this new method to user.java
//    private ArrayList<DisplayableItem> prepareData() {
//        ArrayList<DisplayableItem> allItems = new ArrayList<>();
//
//        LocalDate startDate = birthday;
//        LocalDate endDate = today;
//
//        // Loop through each year of the user's life
//        for (int currentYear = startDate.getYear(); currentYear <= endDate.getYear(); currentYear++) {
//
//            // This list will hold the "Month" items for the current year
//            ArrayList<ParentItem> parentsForThisYear = new ArrayList<>();
//
//            // Determine the correct start and end months for this specific year
//            int startMonth = (currentYear == startDate.getYear()) ? startDate.getMonthValue() : 1;
//            int endMonth = (currentYear == endDate.getYear()) ? endDate.getMonthValue() : 12;
//
//            // Loop through the months of this year
//            for (int currentMonth = startMonth; currentMonth <= endMonth; currentMonth++) {
//
//                // This list holds the "Week" items for the current month
//                ArrayList<ChildItem> weeksInMonth = new ArrayList<>();
//                weeksInMonth.add(new ChildItem("Week 1", R.drawable.ic_action_name));
//                weeksInMonth.add(new ChildItem("Week 2", R.drawable.ic_action_name));
//                weeksInMonth.add(new ChildItem("Week 3", R.drawable.ic_action_name));
//                weeksInMonth.add(new ChildItem("Week 4", R.drawable.ic_action_name));
//
//                String monthTitle = currentYear + "." + currentMonth;
//                // Create the ParentItem (month) and add it to THIS YEAR's list
//                parentsForThisYear.add(new ParentItem(monthTitle, R.drawable.calendar, weeksInMonth));
//            }
//
//            // Create the GrandParentItem (year), giving it the list of months we just made
//            if (!parentsForThisYear.isEmpty()) {
//                String yearTitle = String.valueOf(currentYear);
//                // We store the list of months inside the GrandParentItem object itself
//                GrandParentItem grandParentItem = new GrandParentItem(yearTitle, parentsForThisYear, R.drawable.ic_action_name);
//
//                // IMPORTANT: We only add the YEAR to the main list initially
//                allItems.add(grandParentItem);
//            }
//        }
//        return allItems;
//    }
    // <------------->


//    private void prepareData() {
//        // Clear the main list before populating
//        grandParentItemList.clear();
//
//        LocalDate startDate = birthday;
//        LocalDate endDate = today;
//
//        // Outer loop: Iterate through each year from the birth year to today's year
//        for (int currentYear = startDate.getYear(); currentYear <= endDate.getYear(); currentYear++) {
//
//            // Create a NEW list to hold the months for THIS year only
//            ArrayList<ParentItem> parentsForThisYear = new ArrayList<>();
//
//            // Determine the start and end months for the current year in the loop
//            int startMonth = 1;
//            int endMonth = 12;
//
//            if (currentYear == startDate.getYear()) {
//                // If it's the very first year, start from the birth month
//                startMonth = startDate.getMonthValue();
//            }
//            if (currentYear == endDate.getYear()) {
//                // If it's the current year, end at this month
//                endMonth = endDate.getMonthValue();
//            }
//
//            // Inner loop: Iterate through the months of the currentYear
//            for (int currentMonth = startMonth; currentMonth <= endMonth; currentMonth++) {
//                // This list holds the 4 weeks for each month
//                ArrayList<ChildItem> weeksInMonth = new ArrayList<>();
//                weeksInMonth.add(new ChildItem("Week 1", R.drawable.ic_action_name));
//                weeksInMonth.add(new ChildItem("Week 2", R.drawable.ic_action_name));
//                weeksInMonth.add(new ChildItem("Week 3", R.drawable.ic_action_name));
//                weeksInMonth.add(new ChildItem("Week 4", R.drawable.ic_action_name));
//
//                // Create the ParentItem (e.g., "2009.9") and add it to THIS YEAR'S list
//                String monthTitle = currentYear + "." + currentMonth;
//                parentsForThisYear.add(new ParentItem(monthTitle, R.drawable.calendar, weeksInMonth));
//            }
//
//            // After looping through all the months for the currentYear,
//            // create the GrandParentItem for that year and add it to the main list.
//            // Make sure the list of parents is not empty before adding.
//            if (!parentsForThisYear.isEmpty()) {
//                String yearTitle = String.valueOf(currentYear);
//                grandParentItemList.add(new GrandParentItem(yearTitle, parentsForThisYear, R.drawable.ic_action_name));
//            }
//        }
//    }
//    private void prepareData()
//    {
//
//        int user_months = (int) age.toTotalMonths();
//        int user_year = year;
//        int user_countedYear = 0;
//        int month_count = month;
//        Log.d("gettingMonth", "Months" + age.getMonths());
//        ArrayList<ChildItem>[] months = new ArrayList[user_months];
//        ArrayList<ParentItem>[] years = new ArrayList[user_months/12];
//
//        Log.d("real", "months" + user_months);
//        Log.d("real", "year" + user_year);
//
//        for(int i=0; i<user_months; i++)
//        {
//            months[i] = new ArrayList<>();
//
//            months[i].add(new ChildItem("week1",R.drawable.ic_action_name));
//            months[i].add(new ChildItem("week2",R.drawable.ic_action_name));
//            months[i].add(new ChildItem("week3",R.drawable.ic_action_name));
//            months[i].add(new ChildItem("week4",R.drawable.ic_action_name));
//
//            parentList.add(new ParentItem((Integer.toString(user_year) + "." + Integer.toString(month_count)), R.drawable.calendar ,months[i]));
//            if(month_count == 12)
//            {
//                user_year++;
//                month_count = 1;
//                grandParentItemList.add(new GrandParentItem(Integer.toString(user_year), parentList, R.drawable.ic_action_name));
//            }
//
//            else
//                month_count++;
//
//        }
//
//
//
//    }


//}