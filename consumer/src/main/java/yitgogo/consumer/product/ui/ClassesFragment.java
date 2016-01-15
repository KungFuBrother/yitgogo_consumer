package yitgogo.consumer.product.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
import java.util.List;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.home.model.ModelClass;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.view.InnerGridView;

public class ClassesFragment extends BaseNotifyFragment {

    ListView primaryClassList, secondClassList;
    List<ModelClass> primaryClasses, secondClasses;
    MainClassAdapter primaryClassAdapter;
    MidClassAdapter secondClassAdapter;
    int selection = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_product_classes);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(ClassesFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(ClassesFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getPrimaryClasses();
    }

    private void init() {
        measureScreen();
        primaryClasses = new ArrayList<ModelClass>();
        secondClasses = new ArrayList<ModelClass>();
        primaryClassAdapter = new MainClassAdapter();
        secondClassAdapter = new MidClassAdapter();
    }

    protected void findViews() {
        primaryClassList = (ListView) contentView
                .findViewById(R.id.classes_primary);
        secondClassList = (ListView) contentView
                .findViewById(R.id.classes_second);
        initViews();
        registerViews();
    }

    protected void initViews() {
        primaryClassList.setAdapter(primaryClassAdapter);
        secondClassList.setAdapter(secondClassAdapter);
    }

    protected void registerViews() {
        primaryClassList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                selectMainClass(arg2);
            }
        });
    }

    /**
     * 选择一级分类时做的操作
     *
     * @param position
     */
    private void selectMainClass(int position) {
        selection = position;
        primaryClassAdapter.notifyDataSetChanged();
        getSubClasses();
    }

    /**
     * 选择二级分类时做的操作
     * <p/>
     * //	 * @param position
     */
    private void showProductList(String classId, String className) {
        // TODO Auto-generated method stub
        // Intent intent = new Intent(getActivity(), ProductByClass.class);
        // Bundle bundle = new Bundle();
        // bundle.putString("class_id", classId);
        // bundle.putString("class_name", className);
        // intent.putExtras(bundle);
        // startActivity(intent);
    }

    class MainClassAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return primaryClasses.size();
        }

        @Override
        public Object getItem(int position) {
            return primaryClasses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.list_class_main,
                        null);
                holder.nameText = (TextView) convertView
                        .findViewById(R.id.class_main_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (selection == position) {
                convertView.setBackgroundResource(android.R.color.transparent);
                holder.nameText.setTextColor(getResources().getColor(
                        R.color.textColorCompany));
            } else {
                convertView.setBackgroundResource(R.color.white);
                holder.nameText.setTextColor(getResources().getColor(
                        R.color.textColorPrimary));
            }
            holder.nameText.setText(primaryClasses.get(position).getName());
            return convertView;
        }

        class ViewHolder {
            TextView nameText;
        }
    }

    class MidClassAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return secondClasses.size();
        }

        @Override
        public Object getItem(int position) {
            return secondClasses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.list_class_mid,
                        null);
                holder.nameText = (TextView) convertView
                        .findViewById(R.id.class_mid_name);
                holder.minClassesList = (InnerGridView) convertView
                        .findViewById(R.id.class_mid_subclasses);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final List<ModelClass> minClasses = secondClasses.get(position)
                    .getSubClasses();
            holder.nameText.setText(secondClasses.get(position).getName());
            MinClassAdapter adapter = new MinClassAdapter(minClasses);
            holder.minClassesList.setAdapter(adapter);
            holder.minClassesList
                    .setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View view,
                                                int arg2, long arg3) {
                            jumpProductList(minClasses.get(arg2).getName(),
                                    minClasses.get(arg2).getId(),
                                    ProductListFragment.TYPE_CLASS);
                        }
                    });
            return convertView;
        }

        class ViewHolder {
            TextView nameText;
            InnerGridView minClassesList;
        }
    }

    class MinClassAdapter extends BaseAdapter {
        List<ModelClass> minClasses;

        public MinClassAdapter(List<ModelClass> minClasses) {
            this.minClasses = minClasses;
        }

        @Override
        public int getCount() {
            return minClasses.size();
        }

        @Override
        public Object getItem(int position) {
            return minClasses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.list_class_min,
                        null);
                holder.nameText = (TextView) convertView
                        .findViewById(R.id.class_min_name);
                // holder.imageView = (ImageView) convertView
                // .findViewById(R.id.class_min_image);
                // LayoutParams params = new LayoutParams(
                // LayoutParams.MATCH_PARENT, screenWidth / 4);
                // convertView.setLayoutParams(params);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.nameText.setText(minClasses.get(position).getName());
            // imageLoader.displayImage(minClasses.get(position).getImg(),
            // holder.imageView, options, displayListener);
            return convertView;
        }

        class ViewHolder {
            ImageView imageView;
            TextView nameText;
        }

    }

    private void getPrimaryClasses() {
        Request request = new Request();
        request.setUrl(API.API_PRODUCT_CLASS_MAIN);
        request.addRequestParam("jmdId", Store.getStore()
                .getStoreId());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
                primaryClasses.clear();
                primaryClassAdapter.notifyDataSetChanged();
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
                        if (object.getString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray mainClassArray = object
                                    .optJSONArray("dataList");
                            if (mainClassArray != null) {
                                if (mainClassArray.length() > 0) {
                                    for (int i = 0; i < mainClassArray.length(); i++) {
                                        primaryClasses.add(new ModelClass(
                                                mainClassArray.getJSONObject(i)));
                                    }
                                    if (primaryClasses.size() > 0) {
                                        primaryClassAdapter.notifyDataSetChanged();
                                        selectMainClass(0);
                                    }
                                }
                            }
                        } else {
                            loadingEmpty();
                        }
                    } catch (JSONException e) {
                        loadingEmpty();
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

    private void getSubClasses() {
        Request request = new Request();
        request.setUrl(API.API_PRODUCT_CLASS_MID);
        request.addRequestParam("superiorClassId", primaryClasses.get(selection).getId());
        request.addRequestParam("jmdId", Store.getStore()
                .getStoreId());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
                secondClasses.clear();
                secondClassAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    {
                        JSONObject object;
                        try {
                            object = new JSONObject(requestMessage.getResult());
                            if (object.getString("state").equalsIgnoreCase(
                                    "SUCCESS")) {
                                JSONArray midClassArray = object
                                        .optJSONArray("dataList");
                                if (midClassArray != null) {
                                    if (midClassArray.length() > 0) {
                                        for (int i = 0; i < midClassArray.length(); i++) {
                                            secondClasses
                                                    .add(new ModelClass(
                                                            midClassArray
                                                                    .getJSONObject(i)));
                                        }
                                        if (secondClasses.size() > 0) {
                                            secondClassAdapter
                                                    .notifyDataSetChanged();
                                        }
                                    }
                                }
                            } else {
                                loadingEmpty();
                            }
                        } catch (JSONException e) {
                            loadingEmpty();
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }
}
