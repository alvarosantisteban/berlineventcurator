package test.java.test;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.TextView;

import com.alvarosantisteban.pathos.preferences.AboutActivity;

import java.lang.reflect.Field;

import junit.framework.Assert;

/**
 * @author Alvaro Santisteban Dieguez 16/04/15 - alvarosantisteban@gmail.com
 */
public class AboutActivityTest extends ActivityInstrumentationTestCase2{

    AboutActivity aboutActivity;

    public AboutActivityTest() {
        super(AboutActivity.class);
    }

    @UiThreadTest
    public void testOnCreate() throws Exception {
        TextView email = null;
        TextView gitHubUrl = null;

        Assert.assertNull(email);
        Assert.assertNull(gitHubUrl);

        aboutActivity = (AboutActivity) getActivity();
        getInstrumentation().callActivityOnCreate(aboutActivity, null);

        Field privateTextViewFieldEmail = AboutActivity.class.
                getDeclaredField("email");
        Field privateTextViewFieldUrl = AboutActivity.class.
                getDeclaredField("gitHubUrl");

        privateTextViewFieldEmail.setAccessible(true);
        privateTextViewFieldUrl.setAccessible(true);

        email = (TextView) privateTextViewFieldEmail.get(aboutActivity);
        gitHubUrl = (TextView) privateTextViewFieldUrl.get(aboutActivity);

        // Check that fields are initialized
        Assert.assertNotNull(email);
        Assert.assertNotNull(gitHubUrl);

        // Check that the text being displayed is the wanted one
        Assert.assertEquals("alvarosantisteban@gmail.com", email.getText().toString());
        Assert.assertEquals("My GitHub account", gitHubUrl.getText().toString());
    }
}
