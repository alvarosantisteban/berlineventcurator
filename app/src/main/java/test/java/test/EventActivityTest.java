package test.java.test;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.CheckBox;

import com.alvarosantisteban.pathos.EventActivity;
import com.alvarosantisteban.pathos.R;
import com.alvarosantisteban.pathos.model.Event;
import com.alvarosantisteban.pathos.utils.Constants;

import java.lang.reflect.Field;

import junit.framework.Assert;

/**
 * @author Alvaro Santisteban Dieguez 16/04/15 - alvarosantisteban@gmail.com
 */
public class EventActivityTest extends ActivityInstrumentationTestCase2 {

    EventActivity eventActivity;
    CheckBox interestingCheck;

    public EventActivityTest() {
        super(EventActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Create a mock event
        Event event = new Event();
        event.setName("Prueba");
        event.setDay("13/07/2013");

        // Put the mocked event in an intent
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.putExtra(Constants.EXTRA_EVENT, event);

        // Set it to the Activity
        setActivityIntent(intent);

        eventActivity = (EventActivity) getActivity();
        interestingCheck =
                (CheckBox) eventActivity
                        .findViewById(R.id.checkbox_interesting);
    }

    @UiThreadTest
    public void testOnCheckboxClicked() throws Exception{
        boolean isChecked = interestingCheck.isChecked();

        // Get the Event from the Activity through reflection
        Field privateEventField = EventActivity.class.
                getDeclaredField("event");
        privateEventField.setAccessible(true);
        Event mEvent = (Event) privateEventField.get(eventActivity);

        // Ensure that the CheckBox is not checked
        Assert.assertFalse(isChecked);
        // Ensure that the event is not marked as interesting
        Assert.assertFalse(mEvent.getIsInteresting());

        // Click the CheckBox
        getInstrumentation().callActivityOnCreate(eventActivity, null);
        interestingCheck.performClick();
        isChecked = interestingCheck.isChecked();

        // Check if the CheckBox has been correctly checked
        Assert.assertTrue(isChecked);
        // Check if the event has been marked as interesting
        Assert.assertTrue(mEvent.getIsInteresting());
        }
    }
