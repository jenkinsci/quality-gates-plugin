package quality.gates.sonar.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import quality.gates.jenkins.plugin.QGException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class QualityGateResponseParserTest {

    public static final String COM_OPENSOURCE_QUALITY_GATES = "com.opensource:quality-gates";
    public static final String GREEN_WAS_RED = "Green (was Red)";
    public static final String ALERT = "Alert";
    public static final String T12_01_31_0100 = "2016-03-25T12:01:31+0100";
    public static final String DT = "dt";
    private QualityGateResponseParser qualityGateResponseParser;

    private String jsonArrayString;
    private String jsonObjectString;

    @Before
    public void init() {
        qualityGateResponseParser = new QualityGateResponseParser();
        jsonArrayString = "[\n{\nid: \"455\",\nrk: \"com.opensource:quality-gates\",\nn: \"Green (was Red)\",\nc: \"Alert\",\ndt: \"2016-03-25T12:01:31+0100\",\nds: \"\"\n},\n{\nid: \"430\",\nrk: \"com.opensource:quality-gates\",\nn: \"Red (was Green)\",\nc: \"Alert\",\ndt: \"2016-03-24T16:28:40+0100\",\nds: \"Major issues variation > 2 over 30 days (2016 Mar 15), Coverage variation < 60 since previous analysis (2016 Mar 24)\"\n}]";
        jsonObjectString = "{paging:{pageIndex:1,pageSize:100,total:2},analyses:[{key:\"AWDWWzYlMRo0rJ7-ewqe\",date:\"2018-01-08T15:19:33+0000\",events:[{key:\"AWDWW0VGMRo0rJ7-ewqf\",category:\"QUALITY_GATE\",name:\"Green (was Orange)\",description:\"Vulnerabilities > 2\"},{key:\"AWDWW0VMMRo0rJ7-ewqg\",category:\"VERSION\",name:\"1.0.0-SNAPSHOT\"}]},{key:\"AWDVgjN8MRo0rJ7-ewmz\",date:\"2018-01-08T11:22:20+0000\",events:[{key:\"AWDVgkzGMRo0rJ7-ewqZ\",category:\"QUALITY_GATE\",name:\"Orange\",description:\"Vulnerabilities > 2\"}]}]}";
    }


    @Test
    public void testGetQualityGateResultFromJSONWithOneObjectShouldReturnStatusErrorNewAPI() {
        String jsonObject = "{paging:{pageIndex:1,pageSize:100,total:2},analyses:[{key:\"AWDWWzYlMRo0rJ7-ewqe\",date:\"2018-01-08T15:19:33+0000\",events:[{key:\"AWDWW0VGMRo0rJ7-ewqf\",category:\"QUALITY_GATE\",name:\"Red (was Orange)\",description:\"Vulnerabilities > 2\"},{key:\"AWDWW0VMMRo0rJ7-ewqg\",category:\"VERSION\",name:\"1.0.0-SNAPSHOT\"}]},{key:\"AWDVgjN8MRo0rJ7-ewmz\",date:\"2018-01-08T11:22:20+0000\",events:[{key:\"AWDVgkzGMRo0rJ7-ewqZ\",category:\"QUALITY_GATE\",name:\"Orange\",description:\"Vulnerabilities > 2\"}]}]}";
        assertEquals(QualityGatesStatus.RED, qualityGateResponseParser.getQualityGateResultFromJSON(jsonObject, true));
    }
    
    @Test
    public void testGetQualityGateResultFromJSONWithOneObjectShouldReturnStatusErrorOldAPI() {
        String jsonArray = "[\n{\nid: \"455\",\nrk: \"com.opensource:quality-gates\",\nn: \"Red (was Orange)\",\nc: \"Alert\",\ndt: \"2016-03-25T12:01:31+0100\",\nds: \"\"\n},\n{\nid: \"430\",\nrk: \"com.opensource:quality-gates\",\nn: \"Red (was Green)\",\nc: \"Alert\",\ndt: \"2016-03-24T16:28:40+0100\",\nds: \"Major issues variation > 2 over 30 days (2016 Mar 15), Coverage variation < 60 since previous analysis (2016 Mar 24)\"\n}]";
        assertEquals(QualityGatesStatus.RED, qualityGateResponseParser.getQualityGateResultFromJSON(jsonArray, false));
    }

    @Test
    public void testGetQualityGateResultFromJSONWithMultipleObjectsShouldReturnStatusOKNewAPI() {
        assertEquals(QualityGatesStatus.GREEN, qualityGateResponseParser.getQualityGateResultFromJSON(jsonObjectString, true));
    }
    
    @Test
    public void testGetQualityGateResultFromJSONWithMultipleObjectsShouldReturnStatusOKOldAPI() {
        assertEquals(QualityGatesStatus.GREEN, qualityGateResponseParser.getQualityGateResultFromJSON(jsonArrayString, false));
    }

    @Test
    public void testGetQualityGateResultFromJSONWithMultipleObjectsShouldReturnStatusErrorNewAPI() {
        jsonObjectString = "{paging:{pageIndex:1,pageSize:100,total:2},analyses:[{key:\"AWDWWzYlMRo0rJ7-ewqe\",date:\"2018-01-08T15:19:33+0000\",events:[{key:\"AWDWW0VGMRo0rJ7-ewqf\",category:\"QUALITY_GATE\",name:\"Red (was Orange)\",description:\"Vulnerabilities > 2\"},{key:\"AWDWW0VMMRo0rJ7-ewqg\",category:\"VERSION\",name:\"1.0.0-SNAPSHOT\"}]},{key:\"AWDVgjN8MRo0rJ7-ewmz\",date:\"2018-01-08T11:22:20+0000\",events:[{key:\"AWDVgkzGMRo0rJ7-ewqZ\",category:\"QUALITY_GATE\",name:\"Orange\",description:\"Vulnerabilities > 2\"}]}]}";
        assertEquals(QualityGatesStatus.RED, qualityGateResponseParser.getQualityGateResultFromJSON(jsonObjectString, true));
    }
    
    @Test
    public void testGetQualityGateResultFromJSONWithMultipleObjectsShouldReturnStatusErrorOldAPI() {
        jsonArrayString = "[\n{\nid: \"455\",\nrk: \"com.opensource:quality-gates\",\nn: \"Red (was Orange)\",\nc: \"Alert\",\ndt: \"2016-03-25T12:01:31+0100\",\nds: \"\"\n},\n{\nid: \"430\",\nrk: \"com.opensource:quality-gates\",\nn: \"Red (was Green)\",\nc: \"Alert\",\ndt: \"2016-03-24T16:28:40+0100\",\nds: \"Major issues variation > 2 over 30 days (2016 Mar 15), Coverage variation < 60 since previous analysis (2016 Mar 24)\"\n}]";
        assertEquals(QualityGatesStatus.RED, qualityGateResponseParser.getQualityGateResultFromJSON(jsonArrayString, false));
    }


    @Test
    public void testGetLatestEventResultWhenFirstObjectIsntWithLatestDate() throws JSONException {
        
        JSONObject jsonObject = new JSONObject();
        
        JSONObject paging = new JSONObject();
        paging.put("pageIndex", 1);
        paging.put("pageSize", 100);
        paging.put("total", 2);
        
        JSONArray analyses = new JSONArray();
        
        JSONObject analysis1 = new JSONObject();
        analysis1.put("key", "AWDWWzYlMRo0rJ7-ewqe");
        analysis1.put("date", "2018-01-08T15:19:33+0000");
        JSONArray events1 = new JSONArray();
        JSONObject ev1 = new JSONObject();
        ev1.put("key", "AWDWW0VGMRo0rJ7-ewqf");
        ev1.put("category", "QUALITY_GATE");
        ev1.put("name", "Red");
        ev1.put("description", "Vulnerabilities > 2");
        events1.put(ev1);
        JSONObject ev2 = new JSONObject();
        ev2.put("key", "AWDWW0VMMRo0rJ7-ewqg");
        ev2.put("category", "VERSION");
        ev2.put("name", "1.0.0-SNAPSHOT");
        events1.put(ev2);
        analysis1.put("events", events1);
        analyses.put(analysis1);
        
        JSONObject analysis2 = new JSONObject();
        analysis2.put("key", "AWDWWzYlMRo0rJ7-ewqe");
        analysis2.put("date", "2018-01-08T11:22:20+0000");
        JSONArray events2 = new JSONArray();
        JSONObject ev3 = new JSONObject();
        ev3.put("key", "AWDVgkzGMRo0rJ7-ewqZ");
        ev3.put("category", "QUALITY_GATE");
        ev3.put("name", "Orange");
        ev3.put("description", "Vulnerabilities > 2");
        events2.put(ev3);
        analysis2.put("events", events2);
        analyses.put(analysis2);
        
        jsonObject.put("paging", paging);
        jsonObject.put("analyses", analyses);
        
        assertEquals(ev1.toString(), qualityGateResponseParser.getLastestEventResultNewAPI(jsonObject).toString());
    }

    @Test
    public void testCreateJSONArrayFromString(){
        JSONArray expected= new JSONArray();
        assertEquals(expected.toString(), qualityGateResponseParser.createJSONArrayFromString("[]").toString());
    }

    @Test(expected = QGException.class)
    public void testCreateJSONArrayFromStringWhenStringNotInJSONFormatShouldThrowQGException(){
        qualityGateResponseParser.createJSONArrayFromString("Random string as a response");
    }

    @Test(expected = QGException.class)
    public void testCreateJSONArrayFromStringThrowsExceptionWhenStringISAJSONObjectShouldThrowQGException(){
        qualityGateResponseParser.createJSONArrayFromString("{\n" +
                "err_code: 404,\n" +
                "err_msg: \"Resource not found: wrongProjectKey\"\n" +
                "}");
    }

    @Test
    public void testGetJSONObjectFromArray() throws JSONException {
        JSONArray array = qualityGateResponseParser.createJSONArrayFromString(jsonArrayString);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "455");
        jsonObject.put("rk", COM_OPENSOURCE_QUALITY_GATES);
        jsonObject.put("n", GREEN_WAS_RED);
        jsonObject.put("c", ALERT);
        jsonObject.put(DT, T12_01_31_0100);
        jsonObject.put("ds", "");
        assertEquals(jsonObject.toString(), qualityGateResponseParser.getJSONObjectFromArray(array, 0).toString());
    }

    @Test(expected = QGException.class)
    public void testGetJSONObjectFromArrayThrowsExceptionDueToArrayOutOfBounds(){
        JSONArray array = qualityGateResponseParser.createJSONArrayFromString(jsonArrayString);
        qualityGateResponseParser.getJSONObjectFromArray(array, 2);
    }

    @Test
    public void testCreateObjectWithStatusGreenWhenEmptyArrayShouldReturnJSONObjectWithStatusGreen() throws JSONException{
        JSONObject expectedObject = new JSONObject();
        expectedObject.put("id", "1");
        expectedObject.put(DT, "2000-01-01T12:00:00+0100");
        expectedObject.put("n", "Green");
        JSONObject actual = qualityGateResponseParser.createObjectWithStatusGreen();
        assertEquals(expectedObject.toString(), actual.toString());
    }

    @Test
    public void testGetValueForJSONKeyGivenArrayAndIndex() throws JSONException {
        List<JSONObject> list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DT, T12_01_31_0100);
        list.add(jsonObject);
        String expected = T12_01_31_0100;
        assertEquals(expected, qualityGateResponseParser.getValueForJSONKey(list, 0, DT));
    }

    @Test(expected = QGException.class)
    public void testGetValueForJSONKeyGivenArrayAndIndexNonExistentKey() throws JSONException {
        List<JSONObject> list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DT, T12_01_31_0100);
        list.add(jsonObject);
        String expected = T12_01_31_0100;
        String actual = qualityGateResponseParser.getValueForJSONKey(list, 0, "dateeee");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetValueForJSONKeyGivenJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DT, T12_01_31_0100);
        String expected = T12_01_31_0100;
        assertEquals(expected, qualityGateResponseParser.getValueForJSONKey(jsonObject, DT));
    }

    @Test(expected = QGException.class)
    public void testGetValueForJSONKeyNonExistentKeyGivenJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DT, T12_01_31_0100);
        String expected = T12_01_31_0100;
        String actual = qualityGateResponseParser.getValueForJSONKey(jsonObject, "dateeee");
        assertEquals(expected, actual);
    }
    
    @Test
    public void testFromISO8601UTC() {
    	String date = "2018-01-08T15:19:33+0100";
    	Date result = qualityGateResponseParser.fromISO8601UTC(date);
    	Calendar cal = Calendar.getInstance();
    	
    	cal.setTime(result);
    	
    	assertEquals(2018,  cal.get(Calendar.YEAR));
    	assertEquals(0, cal.get(Calendar.MONTH));
    	assertEquals(8, cal.get(Calendar.DAY_OF_MONTH));
    	assertEquals(15, cal.get(Calendar.HOUR_OF_DAY));
    	assertEquals(19, cal.get(Calendar.MINUTE));
    }
    
    @Test(expected = QGException.class)
    public void testFromISO8601UTCInvalidFormat() {
    	String invalidDate = "2018/01/08";
    	qualityGateResponseParser.fromISO8601UTC(invalidDate);
    	
    }

}