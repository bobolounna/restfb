/*
 * Copyright (c) 2010-2011 Mark Allen.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.restfb.util;

import static com.restfb.util.InsightUtils.buildQueries;
import static com.restfb.util.InsightUtils.convertToMidnightInPacificTimeZone;
import static com.restfb.util.InsightUtils.convertToUnixTimeAtPacificTimeZoneMidnight;
import static com.restfb.util.InsightUtils.createBaseQuery;
import static com.restfb.util.InsightUtils.executeInsightQueriesByDate;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.restfb.ClasspathWebRequestor;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.FacebookClient;
import com.restfb.WebRequestor;
import com.restfb.json.JsonArray;
import com.restfb.util.InsightUtils.Period;


/**
 * Unit tests that exercise {@link com.restfb.util.InsightUtils}.
 * 
 * @author Andrew Liles
 */
public class InsightUtilsTest {

  private static final String JSON_RESOURCES_PREFIX = "/json/insight/";
  private static Locale DEFAULT_LOCALE;
  private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("Etc/UTC");
  private static final TimeZone PST_TIMEZONE = TimeZone.getTimeZone("PST");
  private static SimpleDateFormat sdfUTC;
  private static SimpleDateFormat sdfPST;
  private static final String TEST_PAGE_OBJECT = "31698190356";
  private static Date d20101205_0000pst;
  private FacebookClient defaultNoAccessTokenClient;

  @BeforeClass
  public static void beforeClass() throws ParseException {
    for (Locale locale : Locale.getAvailableLocales()) {
      if (locale.toString().equals("en_US")) {
        DEFAULT_LOCALE = locale;
        break;
      }
    }
    Assert.assertNotNull(DEFAULT_LOCALE);
    sdfUTC = newSimpleDateFormat("yyyyMMdd_HHmm", DEFAULT_LOCALE, UTC_TIMEZONE);
    sdfPST = newSimpleDateFormat("yyyyMMdd_HHmm", DEFAULT_LOCALE, PST_TIMEZONE);
    d20101205_0000pst = sdfPST.parse("20101205_0000");
  }

  @Before
  public void before() {
    defaultNoAccessTokenClient = new DefaultFacebookClient();
  }

  @Test
  public void convertToMidnightInPacificTimeZone1() throws ParseException {
    Date d20030630_0221utc = sdfUTC.parse("20030630_0221");
    Date d20030629_0000pst = sdfPST.parse("20030629_0000");

    Date actual = convertToMidnightInPacificTimeZone(d20030630_0221utc);
    Assert.assertEquals(d20030629_0000pst, actual);
  }

  @Test
  public void convertToMidnightInPacificTimeZone2() throws ParseException {
    Date d20030630_1503utc = sdfUTC.parse("20030630_1503");
    Date d20030630_0000pst = sdfPST.parse("20030630_0000");

    Date actual = convertToMidnightInPacificTimeZone(d20030630_1503utc);
    Assert.assertEquals(d20030630_0000pst, actual);
  }

  @Test
  public void convertToMidnightInPacificTimeZoneSet1() throws ParseException {
    Date d20030630_0221utc = sdfUTC.parse("20030630_0221");
    Date d20030629_0000pst = sdfPST.parse("20030629_0000");
    Date d20030630_1503utc = sdfUTC.parse("20030630_1503");
    Date d20030630_0000pst = sdfPST.parse("20030630_0000");
    Set<Date> inputs = new HashSet<Date>();
    inputs.add(d20030630_0221utc);
    inputs.add(d20030630_1503utc);

    SortedSet<Date> actuals = convertToMidnightInPacificTimeZone(inputs);
    Assert.assertEquals(2, actuals.size());
    Iterator<Date> it = actuals.iterator();
    Assert.assertEquals(d20030629_0000pst, it.next());
    Assert.assertEquals(d20030630_0000pst, it.next());
  }

  @Test
  public void getUnixTimeAtPSTMidnight1() throws ParseException {
    // From http://developers.facebook.com/docs/reference/fql/insights/
    // Example: To obtain data for the 24-hour period starting on
    // September 15th at 00:00 (i.e. 12:00 midnight) and ending on
    // September 16th at 00:00 (i.e. 12:00 midnight),
    // specify 1284620400 as the end_time and 86400 as the period.

    Date d20100916_1800utc = sdfUTC.parse("20100916_1800");

    long actual = convertToUnixTimeAtPacificTimeZoneMidnight(d20100916_1800utc);
    Assert.assertEquals(1284620400L, actual);
  }

  @Test
  public void getUnixTimeAtPSTMidnight2() throws ParseException {
    // in this test we are still in the previous PST day - the difference is 7
    // hours from UTC to PST

    Date d20100917_0659utc = sdfUTC.parse("20100917_0659");

    long actual = convertToUnixTimeAtPacificTimeZoneMidnight(d20100917_0659utc);
    Assert.assertEquals(1284620400L, actual);

    Date d20100917_0700utc = sdfUTC.parse("20100917_0700");

    actual = convertToUnixTimeAtPacificTimeZoneMidnight(d20100917_0700utc);
    Assert.assertEquals(1284620400L + (60 * 60 * 24), actual);
  }

  @Test
  public void createBaseQuery0metrics() {
    Set<String> metrics = Collections.emptySet();
    Assert.assertEquals("SELECT metric, value FROM insights WHERE object_id='31698190356' AND "
      + "period=604800 AND end_time=", createBaseQuery(Period.WEEK, TEST_PAGE_OBJECT, metrics));

    // what about all empties/nulls in the list?
    metrics = new LinkedHashSet<String>();
    metrics.add(null);
    metrics.add("");
    metrics.add("");
    Assert.assertEquals("SELECT metric, value FROM insights WHERE object_id='31698190356' AND "
      + "period=604800 AND end_time=", createBaseQuery(Period.WEEK, TEST_PAGE_OBJECT, metrics));
  }

  @Test
  public void createBaseQuery1metric() {
    Set<String> metrics = Collections.singleton("page_active_users");
    Assert.assertEquals("SELECT metric, value FROM insights WHERE object_id='31698190356' AND metric IN "
      + "('page_active_users') AND period=604800 AND end_time=", createBaseQuery(Period.WEEK, TEST_PAGE_OBJECT, metrics));
  }

  @Test
  public void createBaseQuery3metrics() {
    Set<String> metrics = new LinkedHashSet<String>();
    metrics.add("page_comment_removes");
    metrics.add("page_active_users");
    metrics.add("page_like_adds_source_unique");
    Assert.assertEquals("SELECT metric, value FROM insights WHERE object_id='31698190356' AND metric IN "
      + "('page_comment_removes','page_active_users','page_like_adds_source_unique') AND period=86400 AND end_time=",
      createBaseQuery(Period.DAY, TEST_PAGE_OBJECT, metrics));
  }

  @Test
  public void createBaseQuery4metrics() {
    // are null/empty metrics removed from the list?
    Set<String> metrics = new LinkedHashSet<String>();
    metrics.add("");
    metrics.add("page_comment_removes");
    metrics.add("");
    metrics.add("page_like_adds_source_unique");
    metrics.add(null);
    Assert.assertEquals("SELECT metric, value FROM insights WHERE object_id='31698190356' AND metric IN "
      + "('page_comment_removes','page_like_adds_source_unique') AND period=86400 AND end_time=",
      createBaseQuery(Period.DAY, TEST_PAGE_OBJECT, metrics));
  }

  @Test
  public void buildQueries1() throws ParseException {
    long t20101205_0000 = 1291536000L;
    Assert.assertEquals(t20101205_0000, convertToUnixTimeAtPacificTimeZoneMidnight(d20101205_0000pst));
    Assert.assertEquals(t20101205_0000, d20101205_0000pst.getTime() / 1000L);

    List<Date> datesByQueryIndex = new ArrayList<Date>();
    datesByQueryIndex.add(d20101205_0000pst);

    String baseQuery =
      "SELECT metric, value FROM insights WHERE object_id='31698190356' AND metric IN "
      + "('page_active_users') AND period=604800 AND end_time=";

    Map<String, String> fqlByQueryIndex = buildQueries(baseQuery, datesByQueryIndex);
    Assert.assertEquals(1, fqlByQueryIndex.size());

    String fql = fqlByQueryIndex.values().iterator().next();
    Assert
    .assertEquals(
      "SELECT metric, value FROM insights WHERE object_id='31698190356' AND metric IN ('page_active_users') AND period=604800 AND end_time="
      + t20101205_0000, fql);
  }

  @Test
  public void prepareQueries30() throws Exception {
    // produce a set of days in UTC timezone from 1st Nov 9am to 30th Nov 9am
    // inclusive
    Date d20101101_0900utc = sdfUTC.parse("20101101_0900");
    Calendar c = new GregorianCalendar();
    c.setTimeZone(UTC_TIMEZONE);
    c.setTime(d20101101_0900utc);
    Set<Date> utcDates = new TreeSet<Date>();
    for (int dayNum = 1; dayNum <= 30; dayNum++) {
      utcDates.add(c.getTime());
      c.add(Calendar.DAY_OF_MONTH, 1);
    }
    Assert.assertEquals(30, utcDates.size());

    // convert into PST and convert into a list
    List<Date> datesByQueryIndex = new ArrayList<Date>(convertToMidnightInPacificTimeZone(utcDates));
    Assert.assertEquals(30, datesByQueryIndex.size());

    // Mon Nov 01 00:00:00 2010 PST
    long day0 = sdfPST.parse("20101101_0000").getTime() / 1000L;
    // Sun Nov 07 00:00:00 2010 PST
    long day6 = sdfPST.parse("20101107_0000").getTime() / 1000L;
    // Tue Nov 30 00:00:00 2010 PST
    long day29 = sdfPST.parse("20101130_0000").getTime() / 1000L;

    String baseQuery =
      "SELECT metric, value FROM insights WHERE object_id='31698190356' AND metric IN "
      + "('page_active_users','page_audio_plays') AND period=86400 AND end_time=";

    Map<String, String> fqlByQueryIndex = buildQueries(baseQuery, datesByQueryIndex);
    Assert.assertEquals(30, fqlByQueryIndex.size());

    Assert.assertEquals("SELECT metric, value FROM insights WHERE object_id='31698190356' AND metric IN "
      + "('page_active_users','page_audio_plays') AND period=86400 AND end_time=" + day0, fqlByQueryIndex.get("0"));
    Assert.assertEquals("SELECT metric, value FROM insights WHERE object_id='31698190356' AND metric IN "
      + "('page_active_users','page_audio_plays') AND period=86400 AND end_time=" + day6, fqlByQueryIndex.get("6"));
    Assert.assertEquals("SELECT metric, value FROM insights WHERE object_id='31698190356' AND metric IN "
      + "('page_active_users','page_audio_plays') AND period=86400 AND end_time=" + day29, fqlByQueryIndex.get("29"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void executeInsightQueriesByDate_badArgs1() {
    executeInsightQueriesByDate(null, TEST_PAGE_OBJECT, null, Period.DAY, Collections.singleton(d20101205_0000pst));
  }

  @Test(expected=IllegalArgumentException.class)
  public void executeInsightQueriesByDate_badArgs2() {
    executeInsightQueriesByDate(defaultNoAccessTokenClient, "", null, Period.DAY, Collections.singleton(d20101205_0000pst));
  }

  @Test(expected=IllegalArgumentException.class)
  public void executeInsightQueriesByDate_badArgs3() {
    executeInsightQueriesByDate(defaultNoAccessTokenClient, TEST_PAGE_OBJECT, null, null, Collections.singleton(d20101205_0000pst));
  }

  @Test(expected=IllegalArgumentException.class)
  public void executeInsightQueriesByDate_badArgs4() {
    executeInsightQueriesByDate(defaultNoAccessTokenClient, TEST_PAGE_OBJECT, null, Period.DAY, new HashSet<Date>());
  }

  @Test
  public void executeInsightQueries1() throws IOException {
    //note that the query that is passed to the FacebookClient WebRequestor is ignored,
    //so arguments {String pageObjectId, Set<String> metrics, Period period} are 
    //effectively ignored.  In this test we are validating the WebRequestor's json
    //is properly procssed
    SortedMap<Date, JsonArray> results = executeInsightQueriesByDate(
      createFixedResponseFacebookClient("multiResponse_2metrics_1date.json"), 
      TEST_PAGE_OBJECT, null, Period.DAY, Collections.singleton(d20101205_0000pst));
    Assert.assertNotNull(results);
    Assert.assertEquals(1, results.size());
    JsonArray ja = results.get(d20101205_0000pst);
    Assert.assertNotNull(ja);
    //not ideal that this test requires on a stable JsonArray.toString()
    Assert.assertEquals(
      "[{\"metric\":\"page_fans\",\"value\":3777},{\"metric\":\"page_fans_gender\",\"value\":{\"U\":58,\"F\":1656,\"M\":2014}}]",
      ja.toString());
  }

  /**
   * As there is no easy constructor for making a SimpleDateFormat specifying
   * both a Locale and Timezone a utility is provided here
   * 
   * @param pattern
   * @param locale
   * @param timezone
   * @return
   */
  public static SimpleDateFormat newSimpleDateFormat(String pattern, Locale locale, TimeZone timezone) {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
    sdf.setTimeZone(timezone);
    return sdf;
  }

  private static FacebookClient createFixedResponseFacebookClient(String pathToJson) throws IOException {
    WebRequestor wr = new ClasspathWebRequestor(JSON_RESOURCES_PREFIX + pathToJson);
    String jsonBody = wr.executeGet(null).getBody();
    Assert.assertTrue("path to json not found:" + JSON_RESOURCES_PREFIX + pathToJson, 
      (jsonBody!=null) && (jsonBody.length()>0));
    return new DefaultFacebookClient(null, wr, new DefaultJsonMapper());
  }
}