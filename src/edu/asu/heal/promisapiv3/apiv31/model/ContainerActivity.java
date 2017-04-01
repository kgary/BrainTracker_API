package edu.asu.heal.promisapiv3.apiv31.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * A ContainerActivity will represent a container of other nodes in an activity tree.
 * It may be a root node itself or be referenced by another ContainerActivity. It
 * must have 2 or more sub activities, which themselves may be ContainerActivity
 * objects or LeadActivity objects.
 * @author kevinagary
 *
 */
public class ContainerActivity extends Activity {
    static boolean flag1 = false;
    public enum Sequencing {
        ORDER, RANDOM, INTERLEAVE_ORDER, INTERLEAVE_RANDOM
    }

    private List<Activity> __subActivities;
    private ContainerActivity.Sequencing __sequencing;

    // YYY need our own Exception type here
    private void __init(List<Activity> activities, Sequencing s) throws ModelException {
        if (activities == null || activities.size() < 2) throw new ModelException("Container activities must have at least 2 children");
        // we intentionally do not deep clone
        __subActivities = activities;
        __sequencing = s;
        // We assume this is being populated from a persistent store via a DAO somehow
    }

    public ContainerActivity(String activityId,Activity child1, Activity child2, ContainerActivity.Sequencing seq) throws ModelException {
        super(activityId);
        List<Activity> tempList = new ArrayList<Activity>();
        tempList.add(child1);
        tempList.add(child2);
        __init(tempList, seq);
    }

    public ContainerActivity(String activityId,List<Activity> children, ContainerActivity.Sequencing seq) throws ModelException {
        super(activityId);
        __init(children, seq);
    }

    // factory method, we'll get back to this
    public ActivityInstance generateActivityInstance() {
        return null;
    }

    // generate the serialzied JSON
    public String generateJSON() throws JsonProcessingException {
        // here you have to generate your JSON by adding your metadata and then putting your children in nested objects
        //StringBuilder json = new StringBuilder("");
        String json="";
        JSONParser parser = new JSONParser();
        for(Activity activity : __subActivities)
        {

            JSONObject activity_content = new JSONObject();
            JSONObject jobj = new JSONObject();
            JSONArray jarray =new JSONArray();
            JSONArray jarraycombo =new JSONArray();
            if(activity.getClass().getName().endsWith("ContainerActivity"))
            {
                try {
                    jarray=(JSONArray) parser.parse(activity.generateJSON());
                    if(!json.isEmpty())
                        jarraycombo =(JSONArray) parser.parse(json);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                jarraycombo.addAll(jarray);
                json = jarraycombo.toJSONString();
            }
            else
            {
                try {
                    jobj=(JSONObject) parser.parse(activity.generateJSON());
                    if(!json.isEmpty())
                        jarray =(JSONArray) parser.parse(json);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                jarray.add(jobj);
                json = jarray.toJSONString();
            }

        }

        return json;
    }
    final private class ContainerActivityJSON extends JsonSerializer<ContainerActivity>
    {
        @Override
        public void serialize(ContainerActivity ContainerActivity, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            JSONParser parser = new JSONParser();
            JSONObject answerOpt = new JSONObject();
            jgen.writeArrayFieldStart("activitySequence");

            jgen.writeEndArray();
            jgen.writeEndObject();
        }
    }

    public List<Activity> getSubActivities()
    {
    	return __subActivities;
    }
    public Sequencing getSequencing()
    {
    	return __sequencing;
    }
}
