package yitgogo.consumer.activity.shake.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 活动对象
 */
public class ModelAward {

    String id = "", activityId = "", name = "", image = "";
    double typeValue = 0;
    int type = 0, presetQuantity = 0, surplusQuantity = 0;
    JSONObject jsonObject = new JSONObject();

    public ModelAward() {
    }

    public ModelAward(JSONObject object) throws JSONException {
        if (object != null) {
            jsonObject = object;
            if (object.has("id")) {
                if (!object.optString("id").equalsIgnoreCase("null")) {
                    id = object.optString("id");
                }
            }
            if (object.has("activityId")) {
                if (!object.optString("activityId").equalsIgnoreCase("null")) {
                    activityId = object.optString("activityId");
                }
            }
            if (object.has("name")) {
                if (!object.optString("name").equalsIgnoreCase("null")) {
                    name = object.optString("name");
                }
            }
            if (object.has("image")) {
                if (!object.optString("image").equalsIgnoreCase("null")) {
                    image = object.optString("image");
                }
            }
            if (object.has("typeValue")) {
                if (!object.optString("typeValue").equalsIgnoreCase("null")) {
                    typeValue = object.optDouble("typeValue");
                }
            }
            type = object.optInt("type");
            presetQuantity = object.optInt("presetQuantity");
            surplusQuantity = object.optInt("surplusQuantity");
        }
    }

    public String getId() {
        return id;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public double getTypeValue() {
        return typeValue;
    }

    public int getType() {
        return type;
    }

    public int getPresetQuantity() {
        return presetQuantity;
    }

    public int getSurplusQuantity() {
        return surplusQuantity;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
}
