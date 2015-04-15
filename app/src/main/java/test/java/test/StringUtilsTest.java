package test;

import com.alvarosantisteban.pathos.utils.StringUtils;

import junit.framework.Assert;
import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

    public void testConvertTo24Hours() throws Exception {
        String result;

        // Check bad arguments
        result = StringUtils.convertTo24Hours("sde");
        Assert.assertEquals("", result);

        result = StringUtils.convertTo24Hours("12:13");
        Assert.assertEquals("", result);

        // Check no conversion needed
        result = StringUtils.convertTo24Hours("06:13am");
        Assert.assertEquals("06:13", result);

        result = StringUtils.convertTo24Hours("00:13am");
        Assert.assertEquals("00:13", result);

        // Check conversion
        result = StringUtils.convertTo24Hours("06:13pm");
        Assert.assertEquals("18:13", result);

        result = StringUtils.convertTo24Hours("00:13pm");
        Assert.assertEquals("12:13", result);

        // Unexpected results
        result = StringUtils.convertTo24Hours("18:13am");
        Assert.assertEquals("18:13", result);

        result = StringUtils.convertTo24Hours("18:13pm");
        Assert.assertEquals("06:13", result);
    }

    public void testFormatDate() throws Exception {
        String formattedDate;

        // Check bad arguments
        formattedDate = StringUtils.formatDate("");
        Assert.assertEquals("",formattedDate);

        formattedDate = StringUtils.formatDate("sddsdsfg efe");
        Assert.assertEquals("", formattedDate);

        // Check configurations
        formattedDate = StringUtils.formatDate("July 13 2013");
        Assert.assertEquals("13/07/2013", formattedDate);

        formattedDate = StringUtils.formatDate("july 13 2013");
        Assert.assertEquals("13/07/2013", formattedDate);

        formattedDate = StringUtils.formatDate("JULY 13 2013");
        Assert.assertEquals("13/07/2013", formattedDate);

        formattedDate = StringUtils.formatDate("15 january 2013");
        Assert.assertEquals("15/01/2013", formattedDate);

        formattedDate = StringUtils.formatDate("15 January 2013");
        Assert.assertEquals("15/01/2013", formattedDate);

        formattedDate = StringUtils.formatDate("15 Jan 2013");
        Assert.assertEquals("15/01/2013", formattedDate);
    }
}