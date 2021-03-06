package com.conupods.IndoorMaps;

import android.util.Utility;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import com.conupods.MapsActivity;
import com.conupods.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class IndoorPathTest {
    private final static String TAG = "INDOOR_PATH_TEST";
    UiDevice mDevice = null;
    IndoorOverlayTest HBuildingTest = new IndoorOverlayTest();

    @Rule
    public ActivityTestRule<MapsActivity> mapsActivityRule =
            new ActivityTestRule<>(MapsActivity.class);

    @Before
    public void setUpEach() {
        Utility.turnOnDeviceLocation(TAG);
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    //Checks the appropriate text is displayed and performs click action
    public void performClick(int resource, String text) {
        onView(withText(text)).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(resource)).perform(click());
    }

    //Types value into search bar and selects the first item within recycler
    public void executeSearch(int resourceView, int recycler, String text) {
        onView(withId(resourceView)).perform(typeText(text));
        onView(withId(recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    private void indoorSearcherDefaultStartPoint(String endPoint, String movementMode, int modeId) {
        //Obtain Search Bar UI object
        UiObject searchBar = mDevice.findObject(new UiSelector().resourceId(("android:id/search_bar")));

        //Ensure search bar exists
        Assert.assertTrue("Search bar exists", searchBar.exists());

        //Type destination in search bar
        executeSearch(R.id.searchBar, R.id.recycler_view, endPoint);

        //Press the get directions button
        performClick(R.id.get_directions_btn, "Get Directions");

        performClick(modeId, movementMode);
    }

    private void indoorSearcher(String startPoint, String endPoint, String movementMode, int modeId) {
        //Obtain Search Bar UI object
        UiObject searchBar = mDevice.findObject(new UiSelector().resourceId(("android:id/search_bar")));

        //Ensure search bar exists
        Assert.assertTrue("Search bar exists", searchBar.exists());

        //Type destination in search bar
        executeSearch(R.id.searchBar, R.id.recycler_view, endPoint);

        //Press the get directions button
        performClick(R.id.get_directions_btn, "Get Directions");

        //Change current location to a classroom on 9th floor of H Building
        performClick(R.id.modeSelect_from, "From: Current Location");
        executeSearch(R.id.fromSearchBar, R.id.recycler_view, startPoint);

        //Select walking as the mode of transportation
        try{
            Thread.sleep(1000);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        performClick(modeId, movementMode);
    }

    //Tests the indoor path between two classrooms
    //within the same building (HALL) and the same floor (9th floor)
    @Test
    public void pathBetweenTwoClassroomsOnTheSameFloorTest() {
        String currentLocation = "H 917";
        String destination = "H 921";
        String movementMode = "Walking";
        indoorSearcher(currentLocation, destination, movementMode, R.id.modeSelect_walkingButton);
    }

    @Test
    public void pathBetweenTwoClassroomsOnDifferentFloorTest() {
        String currentLocation = "H 120";
        String destination = "H 921";
        String movementMode = "Walking";
        indoorSearcher(currentLocation, destination, movementMode, R.id.modeSelect_walkingButton);
    }

    @Test
    public void pathBetweenTwoClassroomsOnNonFirstFloorTest() {
        String currentLocation = "H 231";
        String destination = "H 849";
        String movementMode = "Walking";
        indoorSearcher(currentLocation, destination, movementMode, R.id.modeSelect_walkingButton);
    }

    @Test
    public void pathBetweenTwoBuildingsEntranceFloorTest() {
        String currentLocation = "H 122";
        String destination = "MB 1.210";
        String movementMode = "Walking";
        indoorSearcher(currentLocation, destination, movementMode, R.id.modeSelect_walkingButton);
    }

    @Test
    public void pathBetweenTwoBuildingsNonEntranceToNonEntranceTest() {
        String currentLocation = "H 231";
        String destination = "MB S2.210";
        String movementMode = "Walking";
        indoorSearcher(currentLocation, destination, movementMode, R.id.modeSelect_walkingButton);
    }

    @Test
    public void pathBetweenTwoBuildingsEntranceToNonEntranceTest() {
        String currentLocation = "H 122";
        String destination = "MB S2.210";
        String movementMode = "Walking";
        indoorSearcher(currentLocation, destination, movementMode, R.id.modeSelect_walkingButton);
    }

    @Test
    public void pathBetweenTwoBuildingsNonEntranceToEntranceTest() {
        String currentLocation = "H 921";
        String destination = "VL 197";
        String movementMode = "Driving";
        indoorSearcher(currentLocation, destination, movementMode, R.id.modeSelect_drivingButton);
    }
    
    @Test
    public void endPointToEntranceTest() {
        String destination = "H 150";
        String movementMode = "Walking";
        indoorSearcherDefaultStartPoint(destination, movementMode, R.id.modeSelect_walkingButton);
    }

    @Test
    public void endPointToNonEntranceTest() {
        String destination = "H 855";
        String movementMode = "Walking";
        indoorSearcherDefaultStartPoint(destination, movementMode, R.id.modeSelect_walkingButton);
    }


}
