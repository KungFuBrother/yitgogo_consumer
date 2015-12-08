package yitgogo.consumer.activity.shake.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tiger on 2015-12-08.
 */
public class ModelAwardHistory {

    String id = "", winDate = "", orderNumber = "", prizeId = "", prize = "";
    double winMoney = 0;
    int isGrant = 0;
    JSONObject jsonObject = new JSONObject();

    public ModelAwardHistory() {
    }

    public ModelAwardHistory(JSONObject object) throws JSONException {
        if (object != null) {
            jsonObject = object;
            if (object.has("id")) {
                if (!object.optString("id").equalsIgnoreCase("null")) {
                    id = object.optString("id");
                }
            }
            if (object.has("winDate")) {
                if (!object.optString("winDate").equalsIgnoreCase("null")) {
                    winDate = object.optString("winDate");
                }
            }
            if (object.has("orderNumber")) {
                if (!object.optString("orderNumber").equalsIgnoreCase("null")) {
                    orderNumber = object.optString("orderNumber");
                }
            }
            if (object.has("prizeId")) {
                if (!object.optString("prizeId").equalsIgnoreCase("null")) {
                    prizeId = object.optString("prizeId");
                }
            }
            if (object.has("prize")) {
                if (!object.optString("prize").equalsIgnoreCase("null")) {
                    prize = object.optString("prize");
                }
            }
            if (object.has("winMoney")) {
                if (!object.optString("winMoney").equalsIgnoreCase("null")) {
                    winMoney = object.optDouble("winMoney");
                }
            }
            isGrant = object.optInt("isGrant");
        }
    }

    public String getId() {
        return id;
    }

    public String getWinDate() {
        return winDate;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getPrizeId() {
        return prizeId;
    }

    public String getPrize() {
        return prize;
    }

    public double getWinMoney() {
        return winMoney;
    }

    public int getIsGrant() {
        return isGrant;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

}
