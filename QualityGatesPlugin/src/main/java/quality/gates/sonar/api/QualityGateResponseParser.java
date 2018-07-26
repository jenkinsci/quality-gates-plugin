package quality.gates.sonar.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import quality.gates.jenkins.plugin.QGException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.print.attribute.standard.DateTimeAtCompleted;

public class QualityGateResponseParser {

    public QualityGatesStatus getQualityGateResultFromJSON(String jsonString, boolean isNewAPI) throws QGException {
        
    	JSONObject latestEventResult;
    	String fieldName;
    	if(isNewAPI) {
    		JSONObject resultObject = createJSONObjectFromString(jsonString);
    		latestEventResult = getLastestEventResultNewAPI(resultObject);
    		fieldName = "name";
    	} else {
    		JSONArray resultArray = createJSONArrayFromString(jsonString);
    		latestEventResult = getLatestEventResult(resultArray);
    		fieldName = "n";
    	}

        String gateStatus = getValueForJSONKey(latestEventResult, fieldName);
        if (gateStatus.startsWith("Green")) {
            return QualityGatesStatus.GREEN;
        }
        if (gateStatus.startsWith("Orange"))
        {
        	return QualityGatesStatus.ORANGE;
        }
        return QualityGatesStatus.RED;
    }

    protected JSONObject getLatestEventResult(JSONArray jsonArray) throws QGException {
        List<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject returnObject;
        int jsonArrayLength = jsonArray.length();

        if(jsonArrayLength == 0){
            jsonObjects.add(createObjectWithStatusGreen());
        }else {
            for (int i = 0; i < jsonArrayLength; i++) {
                jsonObjects.add(getJSONObjectFromArray(jsonArray, i));
            }
        }

        String mostRecentAlertID = getValueForJSONKey(jsonObjects, 0, "id");
        returnObject = jsonObjects.get(0);

        for (int i = 0; i < jsonObjects.size(); i++) {
            String alertId = getValueForJSONKey(jsonObjects, i, "id");
            if (Integer.parseInt(alertId) > Integer.parseInt(mostRecentAlertID)) {
                returnObject = jsonObjects.get(i);
            }
        }

        return returnObject;
    }
    
    protected JSONObject getLastestEventResultNewAPI(JSONObject jsonObject) throws QGException {
    	JSONObject returnObject = createObjectWithStatusGreenNewAPI();
    	    	
    	if(jsonObject.has("analyses")) {
    		JSONArray analyses = getJSONArrayFromObject(jsonObject,"analyses");
    		
    		Date lastDate = new Date(0);
    		
    		for( int i=0; i<analyses.length(); i++) {
    			JSONObject currentAnalysis = getJSONObjectFromArray(analyses, i);
    			if(currentAnalysis.has("events")) {
    				Date analysisDate = fromISO8601UTC(getValueForJSONKey(currentAnalysis, "date"));
    				if(lastDate.compareTo(analysisDate) < 0) {
    					JSONArray events = getJSONArrayFromObject(currentAnalysis,"events");
    					for (int j=0; j<events.length(); j++) {
        					JSONObject currentEvent = getJSONObjectFromArray(events,j);
        					if("QUALITY_GATE".equals(getValueForJSONKey(currentEvent, "category"))) {
        						lastDate = analysisDate;
        						returnObject = currentEvent;
        						break;
        					}
        				}
    				}
    			}
    		}
    	} else {
    		throw new QGException("The request returned an empty object");
    	}
    	
    	return returnObject;
    }

    protected JSONObject createObjectWithStatusGreen() {
        try {
            JSONObject returnObject = new JSONObject();
            returnObject.put("id", "1");
            returnObject.put("dt", "2000-01-01T12:00:00+0100");
            returnObject.put("n", "Green");
            return returnObject;
        } catch (JSONException e) {
            throw new QGException(e);
        }
    }
    
    protected JSONObject createObjectWithStatusGreenNewAPI() {
    	try {
    		JSONObject returnObject = new JSONObject();
    		returnObject.put("key", "1");
    		returnObject.put("category", "QUALITY_GATE");
    		returnObject.put("name", "Green");
    		return returnObject;
    	} catch (JSONException e) {
    		throw new QGException(e);
    	}
    }

    protected JSONObject getJSONObjectFromArray(JSONArray array, int index) throws QGException {
        try {
            return array.getJSONObject(index);
        } catch (JSONException e) {
            throw new QGException("The request returned an empty array", e);
        }
    }
    
    protected JSONArray getJSONArrayFromObject(JSONObject object, String key) throws QGException {
    	try {
    		return object.getJSONArray(key);
    	} catch (JSONException e) {
    		throw new QGException("The request returned an empty object", e);
    	}
    }

    protected String getValueForJSONKey(List<JSONObject> array, int index, String key) throws QGException {
        try {
            return array.get(index).getString(key);
        } catch (JSONException e) {
            throw new QGException("JSON Key was not found", e);
        }
    }

    protected String getValueForJSONKey(JSONObject jsonObject, String key) throws QGException {
        try {
            return jsonObject.getString(key);
        } catch (JSONException e) {
            throw new QGException("JSON Key was not found ", e);
        }
    }

    protected JSONArray createJSONArrayFromString(String jsonString) throws QGException {
        try {
            return new JSONArray(jsonString);
        } catch (JSONException e) {
            throw new QGException("There was a problem handling the JSON response " + jsonString, e);
        }
    }
    
    protected JSONObject createJSONObjectFromString(String jsonString) throws QGException {
    	try {
    		return new JSONObject(jsonString);
    	} catch (JSONException e) {
    		throw new QGException("There was a problem handling the JSON response "+jsonString, e);
    	}
    }
    
    protected Date fromISO8601UTC(String dateStr) throws QGException {
    	TimeZone tz = TimeZone.getTimeZone("UTC");
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    	    	
    	df.setTimeZone(tz);
    	try {
    	    return df.parse(dateStr);
    	  } catch (ParseException e) {
    		  throw new QGException("There was a problem parsing the JSON date "+dateStr, e);
    	  }

    }
}
