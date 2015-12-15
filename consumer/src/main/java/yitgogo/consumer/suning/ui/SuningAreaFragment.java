package yitgogo.consumer.suning.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smartown.yitian.gogo.R;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yitgogo.consumer.BaseNotifyFragment;
import yitgogo.consumer.store.SelectStoreByAreaFragment;
import yitgogo.consumer.suning.model.GetNewSignature;
import yitgogo.consumer.suning.model.ModelSuningArea;
import yitgogo.consumer.suning.model.ModelSuningAreas;
import yitgogo.consumer.suning.model.SuningManager;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.ScreenUtil;
import yitgogo.consumer.view.Notify;

public class SuningAreaFragment extends BaseNotifyFragment {

    HorizontalScrollView areasScrollView;
    LinearLayout selectedAreaLayout;
    ListView listView;
    /**
     * 各级区域
     */
    HashMap<Integer, ModelSuningArea> selectedAreaHashMap = new HashMap<>();
    /**
     * 当前选择的区域
     */
    ModelSuningArea currentArea = new ModelSuningArea();

    List<ModelSuningArea> suningAreas;
    AreaListAdapter areaListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_select_area);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(SelectStoreByAreaFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(SelectStoreByAreaFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshSelectedArea();
    }

    private void init() {
        measureScreen();
        suningAreas = new ArrayList<>();
        areaListAdapter = new AreaListAdapter();
    }

    @Override
    protected void findViews() {
        areasScrollView = (HorizontalScrollView) contentView.findViewById(R.id.select_area_scroll);
        selectedAreaLayout = (LinearLayout) contentView.findViewById(R.id.select_area_selected_areas);
        listView = (ListView) contentView.findViewById(R.id.select_area_areas);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        listView.setAdapter(areaListAdapter);
    }

    @Override
    protected void registerViews() {
        addTextButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedAreaHashMap.containsKey(1)) {
                    if (selectedAreaHashMap.containsKey(2)) {
                        if (selectedAreaHashMap.containsKey(3)) {
                            ModelSuningAreas suningAreas = SuningManager.getSuningAreas();
                            suningAreas.setProvince(selectedAreaHashMap.get(1));
                            suningAreas.setCity(selectedAreaHashMap.get(2));
                            suningAreas.setDistrict(selectedAreaHashMap.get(3));
                            if (selectedAreaHashMap.containsKey(4)) {
                                suningAreas.setTown(selectedAreaHashMap.get(4));
                            }
                            suningAreas.save();
                            getActivity().finish();
                        } else {
                            Notify.show("请选择所在区县");
                        }
                    } else {
                        Notify.show("请选择所在市");
                    }
                } else {
                    Notify.show("请选择所在省");
                }
            }
        });
    }

    private void selectSelectedArea(ModelSuningArea area) {
        if (area != null) {
            currentArea = area;
            selectedAreaHashMap.put(currentArea.getType(), currentArea);
            refreshSelectedArea();
            switch (area.getType()) {
                case 1:
                    new GetSuningProvince().execute();
                    break;
                case 2:
                    new GetSuningCity().execute();
                    break;
                case 3:
                    new GetSuningDistrict().execute();
                    break;
            }
        }
    }

    private void selectListArea(ModelSuningArea area) {
        if (area != null) {
            currentArea = area;
            selectedAreaHashMap.put(currentArea.getType(), currentArea);
            refreshSelectedArea();
            switch (area.getType()) {
                case 1:
                    new GetSuningCity().execute();
                    break;
                case 2:
                    new GetSuningDistrict().execute();
                    break;
                case 3:
                    new GetSuningTown().execute();
                    break;
            }
        }
    }

    private void refreshSelectedArea() {
        selectedAreaLayout.removeAllViews();
        if (selectedAreaHashMap.isEmpty()) {
            selectedAreaLayout.addView(newTextView("请选择收货区域", R.color.textColorThird, null));
            new GetSuningProvince().execute();
            return;
        }
        List<Map.Entry<Integer, ModelSuningArea>> entries = new ArrayList<>(selectedAreaHashMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Integer, ModelSuningArea>>() {
            @Override
            public int compare(Map.Entry<Integer, ModelSuningArea> area1, Map.Entry<Integer, ModelSuningArea> area2) {
                return area1.getKey().compareTo(area2.getKey());
            }
        });
        for (int i = 0; i < entries.size(); i++) {
            final ModelSuningArea area = entries.get(i).getValue();
            selectedAreaLayout.addView(newTextView(entries.get(i).getValue().getName(), R.color.textColorPrimary, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeSelectedChildArea(area);
                }
            }));
        }
    }

    private void removeSelectedChildArea(ModelSuningArea area) {
        List<Map.Entry<Integer, ModelSuningArea>> entries = new ArrayList<>(selectedAreaHashMap.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getValue().getType() > area.getType()) {
                selectedAreaHashMap.remove(entries.get(i).getValue().getType());
            }
        }
        refreshSelectedArea();
        selectSelectedArea(area);
    }

    private TextView newTextView(String lable, int textColorResId, View.OnClickListener onClickListener) {
        TextView textView = new TextView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ScreenUtil.dip2px(36));
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(layoutParams);
        textView.setBackgroundResource(R.drawable.selector_trans_divider);
        textView.setPadding(ScreenUtil.dip2px(8), 0, ScreenUtil.dip2px(8), 0);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setText(lable + ">");
        textView.setTextColor(getResources().getColor(textColorResId));
        if (onClickListener != null) {
            textView.setOnClickListener(onClickListener);
        }
        return textView;
    }

    class AreaListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return suningAreas.size();
        }

        @Override
        public Object getItem(int position) {
            return suningAreas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int index = position;
            ViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(
                        R.layout.list_area_selected, null);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView
                        .findViewById(R.id.list_area_selected);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final ModelSuningArea area = suningAreas.get(position);
            holder.textView.setText(area.getName());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectListArea(area);
                }
            });
            return convertView;
        }

        class ViewHolder {
            TextView textView;
        }
    }

    class GetSuningProvince extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoading();
            suningAreas.clear();
            areaListAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(Void... voids) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            JSONObject data = new JSONObject();
            try {
                data.put("accessToken", SuningManager.getSignature().getToken());
                data.put("appKey", SuningManager.appKey);
                data.put("v", SuningManager.version);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            nameValuePairs.add(new BasicNameValuePair("data", data.toString()));
            return netUtil.postWithoutCookie(API.API_SUNING_AREA_PROVINCE, nameValuePairs, true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            hideLoading();
            if (SuningManager.isSignatureOutOfDate(s)) {
                GetNewSignature getNewSignature = new GetNewSignature() {
                    @Override
                    protected void onPreExecute() {
                        showLoading();
                    }

                    @Override
                    protected void onPostExecute(Boolean isSuccess) {
                        hideLoading();
                        if (isSuccess) {
                            new GetSuningProvince().execute();
                        }
                    }
                };
                getNewSignature.execute();
                return;
            }
            if (!TextUtils.isEmpty(s)) {
                try {
                    JSONObject object = new JSONObject(s);
                    if (object.optBoolean("isSuccess")) {
                        JSONArray array = object.optJSONArray("province");
                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                suningAreas.add(new ModelSuningArea(array.optJSONObject(i), 1));
                            }
                            areaListAdapter.notifyDataSetChanged();
                        }
                        return;
                    }
                    Notify.show(object.optString("returnMsg"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class GetSuningCity extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoading();
            suningAreas.clear();
            areaListAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            JSONObject data = new JSONObject();
            try {
                data.put("accessToken", SuningManager.getSignature().getToken());
                data.put("appKey", SuningManager.appKey);
                data.put("v", SuningManager.version);
                data.put("provinceId", selectedAreaHashMap.get(1).getCode());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            nameValuePairs.add(new BasicNameValuePair("data", data.toString()));
            return netUtil.postWithoutCookie(API.API_SUNING_AREA_CITY, nameValuePairs, true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            hideLoading();
            if (SuningManager.isSignatureOutOfDate(s)) {
                GetNewSignature getNewSignature = new GetNewSignature() {
                    @Override
                    protected void onPreExecute() {
                        showLoading();
                    }

                    @Override
                    protected void onPostExecute(Boolean isSuccess) {
                        hideLoading();
                        if (isSuccess) {
                            new GetSuningCity().execute();
                        }
                    }
                };
                getNewSignature.execute();
                return;
            }
            if (!TextUtils.isEmpty(s)) {
                try {
                    JSONObject object = new JSONObject(s);
                    if (object.optBoolean("isSuccess")) {
                        JSONArray array = object.optJSONArray("city");
                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                suningAreas.add(new ModelSuningArea(array.optJSONObject(i), 2));
                            }
                            areaListAdapter.notifyDataSetChanged();
                            return;
                        }
                        Notify.show(object.optString("returnMsg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class GetSuningDistrict extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoading();
            suningAreas.clear();
            areaListAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            JSONObject data = new JSONObject();
            try {
                data.put("accessToken", SuningManager.getSignature().getToken());
                data.put("appKey", SuningManager.appKey);
                data.put("v", SuningManager.version);
                data.put("cityId", selectedAreaHashMap.get(2).getCode());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            nameValuePairs.add(new BasicNameValuePair("data", data.toString()));
            return netUtil.postWithoutCookie(API.API_SUNING_AREA_DISTRICT, nameValuePairs, true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            hideLoading();
            if (SuningManager.isSignatureOutOfDate(s)) {
                GetNewSignature getNewSignature = new GetNewSignature() {
                    @Override
                    protected void onPreExecute() {
                        showLoading();
                    }

                    @Override
                    protected void onPostExecute(Boolean isSuccess) {
                        hideLoading();
                        if (isSuccess) {
                            new GetSuningDistrict().execute();
                        }
                    }
                };
                getNewSignature.execute();
                return;
            }
            if (!TextUtils.isEmpty(s)) {
                try {
                    JSONObject object = new JSONObject(s);
                    if (object.optBoolean("isSuccess")) {
                        JSONArray array = object.optJSONArray("district");
                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                suningAreas.add(new ModelSuningArea(array.optJSONObject(i), 3));
                            }
                            areaListAdapter.notifyDataSetChanged();
                            return;
                        }
                        Notify.show(object.optString("returnMsg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    class GetSuningTown extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoading();
            suningAreas.clear();
            areaListAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            JSONObject data = new JSONObject();
            try {
                data.put("accessToken", SuningManager.getSignature().getToken());
                data.put("appKey", SuningManager.appKey);
                data.put("v", SuningManager.version);
                data.put("cityId", selectedAreaHashMap.get(2).getCode());
                data.put("countyId", selectedAreaHashMap.get(3).getCode());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            nameValuePairs.add(new BasicNameValuePair("data", data.toString()));
            return netUtil.postWithoutCookie(API.API_SUNING_AREA_TOWN, nameValuePairs, true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            hideLoading();
            if (SuningManager.isSignatureOutOfDate(s)) {
                GetNewSignature getNewSignature = new GetNewSignature() {
                    @Override
                    protected void onPreExecute() {
                        showLoading();
                    }

                    @Override
                    protected void onPostExecute(Boolean isSuccess) {
                        hideLoading();
                        if (isSuccess) {
                            new GetSuningTown().execute();
                        }
                    }
                };
                getNewSignature.execute();
                return;
            }
            if (!TextUtils.isEmpty(s)) {
                try {
                    JSONObject object = new JSONObject(s);
                    if (object.optBoolean("isSuccess")) {
                        JSONArray array = object.optJSONArray("town");
                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                suningAreas.add(new ModelSuningArea(array.optJSONObject(i), 4));
                            }
                            areaListAdapter.notifyDataSetChanged();
                            return;
                        }
                        Notify.show(object.optString("returnMsg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
