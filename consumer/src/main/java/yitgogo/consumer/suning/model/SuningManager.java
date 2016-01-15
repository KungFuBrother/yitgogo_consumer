package yitgogo.consumer.suning.model;

import android.content.Context;
import android.text.TextUtils;

import com.smartown.controller.mission.MissionController;
import com.smartown.controller.mission.Request;
import com.smartown.controller.mission.RequestListener;
import com.smartown.controller.mission.RequestMessage;

import org.json.JSONException;
import org.json.JSONObject;

import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Content;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.view.Notify;


/**
 * Created by Tiger on 2015-10-21.
 */
public class SuningManager {

    //    public static final String appKey = "YTKJ";
    public static final String appKey = "SCYT";
    public static final String version = "2.0";

    public static ModelSignature getSignature() {
        JSONObject object = new JSONObject();
        try {
            object = new JSONObject(Content.getStringContent(Parameters.CACHE_KEY_SUNING_SIGNATURE, "{}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ModelSignature(object);
    }

    public static ModelSuningAreas getSuningAreas() {
        JSONObject object = new JSONObject();
        try {
            object = new JSONObject(Content.getStringContent(Parameters.CACHE_KEY_SUNING_AREAS, "{}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ModelSuningAreas(object);
    }

    public static boolean isSignatureOutOfDate(String result) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject object = new JSONObject(result);
                if (!object.optBoolean("isSuccess")) {
                    if (object.optString("returnMsg").equals("令牌校验失败")) {
                        Content.removeContent(Parameters.CACHE_KEY_SUNING_SIGNATURE);
                        return true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void getNewSignature(Context context, RequestListener requestListener) {
        Request request = new Request();
        request.setUrl(API.API_SUNING_SIGNATURE);
        MissionController.startRequestMission(context, request, requestListener);
    }

    public static boolean initSignature(RequestMessage requestMessage) {
        if (!TextUtils.isEmpty(requestMessage.getResult())) {
            try {
                JSONObject object = new JSONObject(requestMessage.getResult());
                if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                    JSONObject dataMap = object.optJSONObject("dataMap");
                    Content.saveStringContent(Parameters.CACHE_KEY_SUNING_SIGNATURE, dataMap.toString());
                    return true;
                }
                Notify.show(object.optString("message"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
