package yitgogo.consumer.suning.model;

import org.json.JSONException;
import org.json.JSONObject;

import yitgogo.consumer.tools.Content;
import yitgogo.consumer.tools.Parameters;

/**
 * Created by Tiger on 2015-10-19.
 * <p/>
 * {
 * "code": "04",
 * "name": "武侯区"
 * }
 */
public class ModelSuningAreas {

    ModelSuningArea province = new ModelSuningArea();
    ModelSuningArea city = new ModelSuningArea();
    ModelSuningArea district = new ModelSuningArea();
    ModelSuningArea town = new ModelSuningArea();
    String consumerName = "", consumerPhone = "", consumerAddress = "";


    public ModelSuningAreas(JSONObject object) {
        if (object != null) {
            province = new ModelSuningArea(object.optJSONObject("province"), 1);
            city = new ModelSuningArea(object.optJSONObject("city"), 2);
            district = new ModelSuningArea(object.optJSONObject("district"), 3);
            town = new ModelSuningArea(object.optJSONObject("town"), 4);
            consumerName = object.optString("consumerName");
            consumerPhone = object.optString("consumerPhone");
            consumerAddress = object.optString("consumerAddress");
        }
    }

    public ModelSuningArea getProvince() {
        return province;
    }

    public ModelSuningArea getCity() {
        return city;
    }

    public ModelSuningArea getDistrict() {
        return district;
    }

    public ModelSuningArea getTown() {
        return town;
    }

    public void setProvince(ModelSuningArea province) {
        this.province = province;
    }

    public void setCity(ModelSuningArea city) {
        this.city = city;
    }

    public void setDistrict(ModelSuningArea district) {
        this.district = district;
    }

    public void setTown(ModelSuningArea town) {
        this.town = town;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public String getConsumerPhone() {
        return consumerPhone;
    }

    public void setConsumerPhone(String consumerPhone) {
        this.consumerPhone = consumerPhone;
    }

    public String getConsumerAddress() {
        return consumerAddress;
    }

    public void setConsumerAddress(String consumerAddress) {
        this.consumerAddress = consumerAddress;
    }

    public void save() {
        JSONObject object = new JSONObject();
        try {
            object.put("province", province.toJsonObject());
            object.put("city", city.toJsonObject());
            object.put("district", district.toJsonObject());
            object.put("town", town.toJsonObject());
            object.put("consumerName", consumerName);
            object.put("consumerPhone", consumerPhone);
            object.put("consumerAddress", consumerAddress);
            Content.saveStringContent(Parameters.CACHE_KEY_SUNING_AREAS, object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
