package yitgogo.consumer.store;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
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
import yitgogo.consumer.main.ui.MainActivity;
import yitgogo.consumer.store.model.ModelStoreArea;
import yitgogo.consumer.store.model.ModelStoreSelected;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Content;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.view.InnerListView;

public class SelectStoreByAreaFragment extends BaseNotifyFragment {

    TextView storeNameTextView, storeAddressTextView;

    LinearLayout areaLayout;
    TextView areaTextView;

    InnerListView storeListView;

    HashMap<Integer, ModelStoreArea> areaHashMap = new HashMap<>();
    List<ModelStoreSelected> storeSelecteds;
    SelectedStoreAdapter storeAdapter;

    boolean firstTime = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_select_store_by_area);
        init();
        findViews();
    }

    private void init() {
        storeSelecteds = new ArrayList<>();
        storeAdapter = new SelectedStoreAdapter();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 22) {
            if (resultCode == 23) {
                areaTextView.setText(data.getStringExtra("name"));
                String areaId = data.getStringExtra("id");
                getStore(areaId);
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Store.getStore() == null) {
            firstTime = true;
            jumpForResult(SelectAreaFragment.class.getName(), "选择区域", 22);
        } else {
            getStoreArea();
        }
    }

    @Override
    protected void findViews() {

        storeNameTextView = (TextView) contentView.findViewById(R.id.store_by_area_store_name);
        storeAddressTextView = (TextView) contentView.findViewById(R.id.store_by_area_store_address);
        areaLayout = (LinearLayout) contentView.findViewById(R.id.store_by_area_area_layout);
        areaTextView = (TextView) contentView.findViewById(R.id.store_by_area_area);
        storeListView = (InnerListView) contentView.findViewById(R.id.store_by_area_store_list);

        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        storeListView.setAdapter(storeAdapter);
        if (Store.getStore() != null) {
            storeNameTextView.setText(Store.getStore().getStoreName());
            storeAddressTextView.setText(Store.getStore().getStoreAddess());
        }
    }

    @Override
    protected void registerViews() {
        storeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Content.saveIntContent(Parameters.CACHE_KEY_STORE_TYPE, Parameters.CACHE_VALUE_STORE_TYPE_SELECTED);
                Content.saveStringContent(Parameters.CACHE_KEY_STORE_JSONSTRING, storeSelecteds.get(i).getJsonObject().toString());
                Store.init(getActivity());
                if (firstTime) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
                getActivity().finish();
            }
        });
        areaLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpForResult(SelectAreaFragment.class.getName(), "选择区域", 22);
            }
        });
    }

    class SelectedStoreAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return storeSelecteds.size();
        }

        @Override
        public Object getItem(int position) {
            return storeSelecteds.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_store_selected, null);
                holder = new ViewHolder();
                holder.nameTextView = (TextView) convertView.findViewById(R.id.list_store_name);
                holder.addressTextView = (TextView) convertView.findViewById(R.id.list_store_address);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ModelStoreSelected storeSelected = storeSelecteds.get(position);
            holder.nameTextView.setText(storeSelected.getServicename());
            holder.addressTextView.setText(storeSelected.getServiceaddress());
            return convertView;
        }

        class ViewHolder {
            TextView nameTextView, addressTextView;
        }
    }

    private String getAreaName() {
        if (areaHashMap.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        List<Map.Entry<Integer, ModelStoreArea>> entries = new ArrayList<>(areaHashMap.entrySet());
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

    private String getAreaId() {
        if (areaHashMap.isEmpty()) {
            return "";
        }
        List<Map.Entry<Integer, ModelStoreArea>> entries = new ArrayList<>(areaHashMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Integer, ModelStoreArea>>() {
            @Override
            public int compare(Map.Entry<Integer, ModelStoreArea> area1, Map.Entry<Integer, ModelStoreArea> area2) {
                return area1.getKey().compareTo(area2.getKey());
            }
        });
        return entries.get(entries.size() - 1).getValue().getId();
    }

    private void getStoreArea() {
        Request request = new Request();
        request.setUrl(API.API_STORE_SELECTED_AREA);
        request.addRequestParam("spid", Store.getStore().getStoreId());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
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
                                areaHashMap = new HashMap<>();
                                for (int i = 0; i < array.length(); i++) {
                                    ModelStoreArea storeArea = new ModelStoreArea(array.optJSONObject(i));
                                    areaHashMap.put(storeArea.getType(), storeArea);
                                }
                                areaTextView.setText(getAreaName());
                                getStore(getAreaId());
                            }
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

//    class GetStoreArea extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            showLoading();
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            nameValuePairs.add(new BasicNameValuePair("spid", Store.getStore().getStoreId()));
//            return netUtil.postWithoutCookie(API.API_STORE_SELECTED_AREA, nameValuePairs, false, false);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            hideLoading();
//
//        }
//    }

    private void getStore(String areaId) {
        Request request = new Request();
        request.setUrl(API.API_STORE_LIST);
        request.addRequestParam("areaId", areaId);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    JSONObject object;
                    try {
                        object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray array = object.optJSONArray("dataList");
                            if (array != null) {
                                storeSelecteds = new ArrayList<>();
                                for (int i = 0; i < array.length(); i++) {
                                    storeSelecteds.add(new ModelStoreSelected(array.getJSONObject(i)));
                                }
                                if (storeSelecteds.size() > 0) {
                                    storeAdapter.notifyDataSetChanged();
                                } else {
                                    loadingEmpty("该区域暂无服务中心");
                                }
                            }
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

//    class GetStore extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            showLoading();
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            nameValuePairs.add(new BasicNameValuePair("areaId", params[0]));
//            return netUtil.postWithoutCookie(API.API_STORE_LIST, nameValuePairs, false, false);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//
//
//        }
//    }

}
