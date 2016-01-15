package yitgogo.consumer.store;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smartown.controller.mission.MissionController;
import com.smartown.controller.mission.MissionMessage;
import com.smartown.controller.mission.Request;
import com.smartown.controller.mission.RequestListener;
import com.smartown.controller.mission.RequestMessage;
import com.smartown.yitian.gogo.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.store.model.ModelArea;
import yitgogo.consumer.store.model.ModelStoreArea;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.ScreenUtil;

public class SelectAreaFragment extends BaseNotifyFragment {

    HorizontalScrollView areasScrollView;
    LinearLayout selectedAreaLayout;
    ListView listView;
    /**
     * 各级区域
     */
    HashMap<Integer, ModelStoreArea> selectedAreaHashMap = new HashMap<>();
    /**
     * 所有区域
     */
    List<ModelStoreArea> areas;
    /**
     * 当前选择的区域
     */
    ModelStoreArea currentArea = new ModelStoreArea();
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
        getArea();
    }

    private void init() {
        measureScreen();
        areas = new ArrayList<>();
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectArea(areas.get(i));
            }
        });
    }

    private void selectArea(ModelStoreArea area) {
        if (area != null) {
            currentArea = area;
            selectedAreaHashMap.put(currentArea.getType(), currentArea);
            refreshSelectedArea();
        }
        getArea();
    }

    private void refreshSelectedArea() {
        selectedAreaLayout.removeAllViews();
        List<Map.Entry<Integer, ModelStoreArea>> entries = new ArrayList<>(selectedAreaHashMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Integer, ModelStoreArea>>() {
            @Override
            public int compare(Map.Entry<Integer, ModelStoreArea> area1, Map.Entry<Integer, ModelStoreArea> area2) {
                return area1.getKey().compareTo(area2.getKey());
            }
        });
        for (int i = 0; i < entries.size(); i++) {
            final ModelStoreArea area = entries.get(i).getValue();
            selectedAreaLayout.addView(newTextView(entries.get(i).getValue().getName(), R.color.textColorPrimary, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeSelectedChildArea(area);
                }
            }));
        }
    }

    private void removeSelectedChildArea(ModelStoreArea area) {
        List<Map.Entry<Integer, ModelStoreArea>> entries = new ArrayList<>(selectedAreaHashMap.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getValue().getType() > area.getType()) {
                selectedAreaHashMap.remove(entries.get(i).getValue().getType());
            }
        }
        refreshSelectedArea();
        selectArea(area);
    }

    private String getSelectedAreaName() {
        StringBuilder builder = new StringBuilder();
        List<Map.Entry<Integer, ModelStoreArea>> entries = new ArrayList<>(selectedAreaHashMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Integer, ModelStoreArea>>() {
            @Override
            public int compare(Map.Entry<Integer, ModelStoreArea> area1, Map.Entry<Integer, ModelStoreArea> area2) {
                return area1.getKey().compareTo(area2.getKey());
            }
        });
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                builder.append(">");
            }
            builder.append(entries.get(i).getValue().getName());
        }
        return builder.toString();
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
            return areas.size();
        }

        @Override
        public Object getItem(int position) {
            return areas.get(position);
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
            holder.textView.setText(areas.get(position).getName());
            return convertView;
        }

        class ViewHolder {
            TextView textView;
        }
    }

    private void getArea() {
        Request request = new Request();
        request.setUrl(API.API_STORE_AREA);
        if (!TextUtils.isEmpty(currentArea.getId())) {
            request.addRequestParam("aid", currentArea.getId());
        }
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
                areas.clear();
                areaListAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray array = object.optJSONArray("dataList");
                            if (array != null) {
                                for (int i = 0; i < array.length(); i++) {
                                    ModelArea area = new ModelArea(array.getJSONObject(i));
                                    areas.add(new ModelStoreArea(area.getId(), area.getValuename(), area.getAreaType().getId()));
                                }
                                if (!areas.isEmpty()) {
                                    areaListAdapter.notifyDataSetChanged();
                                    if (areas.size() == 1) {
                                        if (areas.get(0).getId().equals("3253")) {
                                            selectArea(areas.get(0));
                                        }
                                    }
                                    return;
                                }
                            }
                            Intent intent = new Intent();
                            intent.putExtra("id", currentArea.getId());
                            intent.putExtra("name", getSelectedAreaName());
                            getActivity().setResult(23, intent);
                            getActivity().finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

//    class GetArea extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            if (!TextUtils.isEmpty(currentArea.getId())) {
//                nameValuePairs.add(new BasicNameValuePair("aid", currentArea.getId()));
//            }
//            return netUtil.postWithoutCookie(API.API_STORE_AREA, nameValuePairs, true, true);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            // {"message":"ok","state":"SUCCESS","cacheKey":null,"dataList":[{"id":3253,"valuename":"中国","valuetype":{"id":1,"typename":"国"},"onid":0,"onname":null,"brevitycode":null}],"totalCount":1,"dataMap":{},"object":null}
//
//
//        }
//    }

}
