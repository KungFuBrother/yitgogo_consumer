package com.smartown.controller.mission;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import yitgogo.consumer.money.model.MoneyAccount;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Content;
import yitgogo.consumer.tools.LogUtil;
import yitgogo.consumer.tools.PackageTool;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;

public class RequestMission extends Mission {

    HttpURLConnection httpURLConnection;

    Request request;
    RequestListener requestListener;

    public RequestMission(Request request, RequestListener requestListener) {
        this.request = request;
        this.requestListener = requestListener;
    }

    private void post() {
        if (isCanceled()) {
            return;
        }
        try {
            Log.i("Request", "url:" + request.getUrl());
            URL url = new URL(request.getUrl());
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);// 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在http正文内，因此需要设为true, 默认情况下是false;
            httpURLConnection.setDoInput(true);// 设置是否从httpUrlConnection读入，默认情况下是true;
            httpURLConnection.setUseCaches(false); // Post 请求不能使用缓存
            httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");//表单参数类型
            httpURLConnection.setRequestMethod("POST");// 设定请求的方法为"POST"，默认是GET
            httpURLConnection.setConnectTimeout(5000);//连接超时 单位毫秒
            httpURLConnection.setReadTimeout(5000);//读取超时 单位毫秒
            httpURLConnection.setRequestProperty("version", PackageTool.getVersionName());
            if (request.isUseCookie()) {
                if (request.getUrl().startsWith(API.IP_PUBLIC)) {
                    httpURLConnection.setRequestProperty("Cookie", CookieController.getCookie(API.IP_PUBLIC));
                } else if (request.getUrl().startsWith(API.IP_MONEY)) {
                    httpURLConnection.setRequestProperty("Cookie", CookieController.getCookie(API.IP_MONEY));
                }
            }
            if (!request.getRequestParams().isEmpty()) {
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < request.getRequestParams().size(); i++) {
                    if (i > 0) {
                        stringBuffer.append("&");
                    }
                    stringBuffer.append(request.getRequestParams().get(i).getKey());
                    stringBuffer.append("=");
                    stringBuffer.append(request.getRequestParams().get(i).getValue());
                }
                if (isCanceled()) {
                    return;
                }
                Log.i("Request", "parameters:" + stringBuffer);
                httpURLConnection.setFixedLengthStreamingMode(stringBuffer.toString().getBytes().length);//请求长度
                OutputStream outputStream = httpURLConnection.getOutputStream();// 此处getOutputStream会隐含的进行connect(即：如同调用上面的connect()方法，所以在开发中不调用上述的connect()也可以)。
                outputStream.write(stringBuffer.toString().getBytes());
                outputStream.flush();
                outputStream.close();
            }
            if (isCanceled()) {
                return;
            }
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (request.isSaveCookie()) {
                    List<String> cookies = httpURLConnection.getHeaderFields().get("Set-Cookie");
                    if (cookies != null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < cookies.size(); i++) {
                            if (i > 0) {
                                stringBuilder.append(";");
                            }
                            stringBuilder.append(cookies.get(i));
                        }
                        if (request.getUrl().startsWith(API.IP_PUBLIC)) {
                            CookieController.saveCookie(API.IP_PUBLIC, stringBuilder.toString());
                        } else if (request.getUrl().startsWith(API.IP_MONEY)) {
                            CookieController.saveCookie(API.IP_MONEY, stringBuilder.toString());
                        }
                    }
                }
                StringBuilder stringBuilder = new StringBuilder();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                bufferedReader.close();
                inputStream.close();
                if (isCanceled()) {
                    return;
                }
                if (!TextUtils.isEmpty(stringBuilder.toString())) {
                    // 判断返回的的字符串是否包含以下这些会话过期的标志
                    if (stringBuilder.toString().contains("NAUTH")) {
                        LogUtil.logError("Request Status", "会话过期");
                        Request loginRequest = new Request();
                        loginRequest.setUrl(API.API_USER_LOGIN);
                        loginRequest.addRequestParam("phone", User.getUser().getPhone());
                        loginRequest.addRequestParam("password", Content.getStringContent(Parameters.CACHE_KEY_USER_PASSWORD, ""));
                        loginRequest.setSaveCookie(true);
                        String result = MissionController.request(loginRequest);
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                JSONObject object = new JSONObject(result);
                                if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                                    Content.saveStringContent(Parameters.CACHE_KEY_MONEY_SN, object.optString("cacheKey"));
                                    JSONObject userObject = object.optJSONObject("object");
                                    if (userObject != null) {
                                        Content.saveStringContent(Parameters.CACHE_KEY_USER_JSON, userObject.toString());
                                        User.init();
                                        post();
                                        return;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Content.removeContent(Parameters.CACHE_KEY_USER_JSON);
                        Content.removeContent(Parameters.CACHE_KEY_USER_PASSWORD);
                        Content.removeContent(Parameters.CACHE_KEY_COOKIE);
                        Content.removeContent(Parameters.CACHE_KEY_MONEY_SN);
                        User.init();
                        MoneyAccount.init(null);
                        requestListener.sendMessage(new MissionMessage(MissionListener.PROGRESS_FAILED, "会话过期"));
                        return;
                    }
                    requestListener.sendMessage(new RequestMessage(MissionListener.PROGRESS_SUCCESS, "PROGRESS_SUCCESS", stringBuilder.toString()));
                }
            } else {
                if (isCanceled()) {
                    return;
                }
                requestListener.sendMessage(new MissionMessage(MissionListener.PROGRESS_FAILED, "PROGRESS_FAILED" + " " + String.valueOf(responseCode)));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (isCanceled()) {
                return;
            }
            requestListener.sendMessage(new MissionMessage(MissionListener.PROGRESS_FAILED, e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            if (isCanceled()) {
                return;
            }
            requestListener.sendMessage(new MissionMessage(MissionListener.PROGRESS_FAILED, e.getMessage()));
        } finally {
            httpURLConnection.disconnect();
        }
    }

    private void get() {
        if (isCanceled()) {
            return;
        }
        try {
            Log.i("Request", "url:" + request.getUrl());
            StringBuilder paramStringBuilder = new StringBuilder();
            if (!request.getRequestParams().isEmpty()) {
                paramStringBuilder.append("?");
                for (int i = 0; i < request.getRequestParams().size(); i++) {
                    if (i > 0) {
                        paramStringBuilder.append("&");
                    }
                    paramStringBuilder.append(request.getRequestParams().get(i).getKey());
                    paramStringBuilder.append("=");
                    paramStringBuilder.append(request.getRequestParams().get(i).getValue());
                }
                Log.i("Request", "parameters:" + paramStringBuilder.toString());
            }
            if (isCanceled()) {
                return;
            }
            URL url = new URL(request.getUrl() + paramStringBuilder.toString());
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);// 设置是否从httpUrlConnection读入，默认情况下是true;
            httpURLConnection.setUseCaches(false); // Post 请求不能使用缓存
            httpURLConnection.setConnectTimeout(5000);//连接超时 单位毫秒
            httpURLConnection.setReadTimeout(5000);//读取超时 单位毫秒
            if (isCanceled()) {
                return;
            }
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder stringBuilder = new StringBuilder();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                bufferedReader.close();
                inputStream.close();
                if (isCanceled()) {
                    return;
                }
                if (!TextUtils.isEmpty(stringBuilder.toString())) {
                    requestListener.sendMessage(new RequestMessage(MissionListener.PROGRESS_SUCCESS, "PROGRESS_SUCCESS", stringBuilder.toString()));
                }
            } else {
                if (isCanceled()) {
                    return;
                }
                requestListener.sendMessage(new MissionMessage(MissionListener.PROGRESS_FAILED, "PROGRESS_FAILED" + " " + String.valueOf(responseCode)));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (isCanceled()) {
                return;
            }
            requestListener.sendMessage(new MissionMessage(MissionListener.PROGRESS_FAILED, e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            if (isCanceled()) {
                return;
            }
            requestListener.sendMessage(new MissionMessage(MissionListener.PROGRESS_FAILED, e.getMessage()));
        } finally {
            httpURLConnection.disconnect();
        }
    }

    @Override
    public void start() {
        if (isCanceled()) {
            return;
        }
        requestListener.sendMessage(new MissionMessage(MissionListener.PROGRESS_START, "PROGRESS_START"));
        if (request.getRequestType().equals(Request.REQUEST_TYPE_POST)) {
            post();
        } else {
            get();
        }
        if (isCanceled()) {
            return;
        }
        requestListener.sendMessage(new MissionMessage(MissionListener.PROGRESS_FINISH, "PROGRESS_FINISH"));
    }

}
