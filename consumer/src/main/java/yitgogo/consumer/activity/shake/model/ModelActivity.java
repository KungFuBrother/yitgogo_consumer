package yitgogo.consumer.activity.shake.model;

import org.json.JSONException;
import org.json.JSONObject;

import yitgogo.consumer.store.model.ModelStoreSelected;

/**
 * 活动对象
 */
public class ModelActivity {

    String id = "", activityName = "", activityImg = "", titleImg = "",
            activityNum = "", activityState = "", activityStartTime = "", activityEndTime = "",
            progress = "", addUser = "", addTime = "", rule = "";
    double totalMoney = 0, surplusMoney = 0, lowestMoney = 0, highestMoney = 0, joinMoney = 0;
    ModelStoreSelected service = new ModelStoreSelected();
    int winExtent = 0, winNum = 0, joinNum = 0, numberOfJoin = 0, type = 0, forecastTotalCount = 0, joinNumForDay = 0, joinPersonNum = 0, joinCount = 0,joinState=0;
    boolean showDefault = false;
    JSONObject jsonObject = new JSONObject();

    public ModelActivity() {
    }

    public ModelActivity(JSONObject object) throws JSONException {
        if (object != null) {
            jsonObject = object;
            if (object.has("id")) {
                if (!object.optString("id").equalsIgnoreCase("null")) {
                    id = object.optString("id");
                }
            }
            if (object.has("activityName")) {
                if (!object.optString("activityName").equalsIgnoreCase("null")) {
                    activityName = object.optString("activityName");
                }
            }
            if (object.has("activityImg")) {
                if (!object.optString("activityImg").equalsIgnoreCase("null")) {
                    activityImg = object.optString("activityImg");
                }
            }
            if (object.has("titleImg")) {
                if (!object.optString("titleImg").equalsIgnoreCase("null")) {
                    titleImg = object.optString("titleImg");
                }
            }
            if (object.has("activityNum")) {
                if (!object.optString("activityNum").equalsIgnoreCase("null")) {
                    activityNum = object.optString("activityNum");
                }
            }
            if (object.has("activityState")) {
                if (!object.optString("activityState").equalsIgnoreCase("null")) {
                    activityState = object.optString("activityState");
                }
            }
            if (object.has("activityStartTime")) {
                if (!object.optString("activityStartTime").equalsIgnoreCase("null")) {
                    activityStartTime = object.optString("activityStartTime");
                }
            }
            if (object.has("activityEndTime")) {
                if (!object.optString("activityEndTime").equalsIgnoreCase("null")) {
                    activityEndTime = object.optString("activityEndTime");
                }
            }
            if (object.has("progress")) {
                if (!object.optString("progress").equalsIgnoreCase("null")) {
                    progress = object.optString("progress");
                }
            }
            if (object.has("addUser")) {
                if (!object.optString("addUser").equalsIgnoreCase("null")) {
                    addUser = object.optString("addUser");
                }
            }
            if (object.has("addTime")) {
                if (!object.optString("addTime").equalsIgnoreCase("null")) {
                    addTime = object.optString("addTime");
                }
            }
            if (object.has("rule")) {
                if (!object.optString("rule").equalsIgnoreCase("null")) {
                    rule = object.optString("rule");
                }
            }
            if (object.has("totalMoney")) {
                if (!object.optString("totalMoney").equalsIgnoreCase("null")) {
                    totalMoney = object.optDouble("totalMoney");
                }
            }
            if (object.has("surplusMoney")) {
                if (!object.optString("surplusMoney").equalsIgnoreCase("null")) {
                    surplusMoney = object.optDouble("surplusMoney");
                }
            }
            if (object.has("lowestMoney")) {
                if (!object.optString("lowestMoney").equalsIgnoreCase("null")) {
                    lowestMoney = object.optDouble("lowestMoney");
                }
            }
            if (object.has("highestMoney")) {
                if (!object.optString("highestMoney").equalsIgnoreCase("null")) {
                    highestMoney = object.optDouble("highestMoney");
                }
            }
            if (object.has("joinMoney")) {
                if (!object.optString("joinMoney").equalsIgnoreCase("null")) {
                    joinMoney = object.optDouble("joinMoney");
                }
            }
            service = new ModelStoreSelected(object.optJSONObject("service"));
            winExtent = object.optInt("winExtent");
            winNum = object.optInt("winNum");
            joinNum = object.optInt("joinNum");
            numberOfJoin = object.optInt("numberOfJoin");
            type = object.optInt("type");
            forecastTotalCount = object.optInt("forecastTotalCount");
            joinNumForDay = object.optInt("joinNumForDay");
            joinPersonNum = object.optInt("joinPersonNum");
            joinCount = object.optInt("joinCount");
            joinState = object.optInt("joinState");
            showDefault = object.optBoolean("showDefault");
        }
    }

    public String getId() {
        return id;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getActivityImg() {
        return activityImg;
    }

    public String getTitleImg() {
        return titleImg;
    }

    public String getActivityNum() {
        return activityNum;
    }

    public String getActivityState() {
        return activityState;
    }

    public String getActivityStartTime() {
        return activityStartTime;
    }

    public String getProgress() {
        return progress;
    }

    public String getAddUser() {
        return addUser;
    }

    public String getAddTime() {
        return addTime;
    }

    public double getTotalMoney() {
        return totalMoney;
    }

    public double getSurplusMoney() {
        return surplusMoney;
    }

    public double getLowestMoney() {
        return lowestMoney;
    }

    public double getHighestMoney() {
        return highestMoney;
    }

    public ModelStoreSelected getService() {
        return service;
    }

    public int getWinExtent() {
        return winExtent;
    }

    public int getWinNum() {
        return winNum;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public String getActivityEndTime() {
        return activityEndTime;
    }

    public String getRule() {
        return rule;
    }

    public double getJoinMoney() {
        return joinMoney;
    }

    public int getJoinNum() {
        return joinNum;
    }

    public int getNumberOfJoin() {
        return numberOfJoin;
    }

    public int getType() {
        return type;
    }

    public int getForecastTotalCount() {
        return forecastTotalCount;
    }

    public int getJoinNumForDay() {
        return joinNumForDay;
    }

    public int getJoinPersonNum() {
        return joinPersonNum;
    }

    public int getJoinCount() {
        return joinCount;
    }

    public int getJoinState() {
        return joinState;
    }

    public boolean isShowDefault() {
        return showDefault;
    }
}
