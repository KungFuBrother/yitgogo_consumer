package yitgogo.consumer.suning.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tiger on 2015-10-19.
 * <p/>
 * {
 * "code": "04",
 * "name": "武侯区"
 * }
 */
public class ModelSuningArea {

    int type = 0;
    String code = "", name = "";

    public ModelSuningArea() {
    }

    public ModelSuningArea(JSONObject object, int type) {
        if (object != null) {
            if (object.has("code")) {
                if (!object.optString("code").equalsIgnoreCase("null")) {
                    code = object.optString("code");
                }
            }
            if (object.has("name")) {
                if (!object.optString("name").equalsIgnoreCase("null")) {
                    name = object.optString("name");
                }
            }
            this.type = type;
        }
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public JSONObject toJsonObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("code", code);
            object.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }
}
