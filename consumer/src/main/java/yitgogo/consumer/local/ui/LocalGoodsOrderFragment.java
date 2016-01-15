package yitgogo.consumer.local.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;
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
import yitgogo.consumer.local.model.ModelLocalGoodsOrder;
import yitgogo.consumer.local.model.ModelLocalGoodsOrderGoods;
import yitgogo.consumer.order.ui.OrderFragment;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.InnerGridView;

public class LocalGoodsOrderFragment extends BaseNotifyFragment {

    String TAG = OrderFragment.class.getName();
    PullToRefreshListView orderList;
    List<ModelLocalGoodsOrder> localGoodsOrders;
    OrderAdapter orderAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_order_platform);
        init();
        findViews();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLocalGoodsOrder();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(LocalGoodsOrderFragment.class.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(LocalGoodsOrderFragment.class.getName());
    }

    private void init() {
        measureScreen();
        localGoodsOrders = new ArrayList<ModelLocalGoodsOrder>();
        orderAdapter = new OrderAdapter();
    }

    @Override
    protected void findViews() {
        orderList = (PullToRefreshListView) contentView
                .findViewById(R.id.order_list);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        orderList.setMode(Mode.BOTH);
        orderList.setAdapter(orderAdapter);
    }

    @Override
    protected void registerViews() {
        orderList.setOnRefreshListener(new OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                localGoodsOrders.clear();
                orderAdapter.notifyDataSetChanged();
                pagenum = 0;
                orderList.setMode(Mode.BOTH);
                getLocalGoodsOrder();
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                getLocalGoodsOrder();
            }
        });
    }

    private void showOrderDetail(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("localGoodsOrderNumber", localGoodsOrders
                .get(position).getRetailOrderNumber());
        jump(LocalGoodsOrderDetailFragment.class.getName(), "订单详情", bundle);
    }

    private void getLocalGoodsOrder() {
        pagenum++;
        Request request = new Request();
        request.setUrl(API.API_LOCAL_BUSINESS_GOODS_ORDER_LIST);
        request.addRequestParam("memberAccount", User.getUser().getUseraccount());
        request.addRequestParam("pageNo", pagenum + "");
        request.addRequestParam("pageSize", pagesize + "");
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                orderList.onRefreshComplete();
                pagenum--;
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                orderList.onRefreshComplete();
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    JSONObject object;
                    try {
                        object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray array = object.optJSONArray("dataList");
                            if (array != null) {
                                if (array.length() > 0) {
                                    if (array.length() < pagesize) {
                                        orderList.setMode(Mode.PULL_FROM_START);
                                    }
                                    for (int i = 0; i < array.length(); i++) {
                                        localGoodsOrders
                                                .add(new ModelLocalGoodsOrder(array
                                                        .optJSONObject(i)));
                                    }
                                    orderAdapter.notifyDataSetChanged();
                                    return;
                                } else {
                                    orderList.setMode(Mode.PULL_FROM_START);
                                }

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (localGoodsOrders.size() == 0) {
                        loadingEmpty();
                    }
                } else {
                    loadingFailed();
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    class OrderAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return localGoodsOrders.size();
        }

        @Override
        public Object getItem(int position) {
            return localGoodsOrders.get(position);
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
                convertView = layoutInflater.inflate(R.layout.list_order, null);
                holder.orderProducts = (InnerGridView) convertView
                        .findViewById(R.id.list_order_product);
                holder.orderStateText = (TextView) convertView
                        .findViewById(R.id.list_order_state);
                holder.orderMoneyText = (TextView) convertView
                        .findViewById(R.id.list_order_money);
                holder.orderTimeText = (TextView) convertView
                        .findViewById(R.id.list_order_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ModelLocalGoodsOrder localGoodsOrder = localGoodsOrders
                    .get(position);
            holder.orderStateText.setText(localGoodsOrder
                    .getRetailOrderStatus());
            holder.orderTimeText.setText(localGoodsOrder.getOrderDate());
            holder.orderMoneyText.setText(Parameters.CONSTANT_RMB
                    + decimalFormat.format(localGoodsOrder
                    .getRetailOrderPrice()));
            if (localGoodsOrder.getOrderGoods().size() > 1) {
                holder.orderProducts.setNumColumns(4);
            } else {
                holder.orderProducts.setNumColumns(1);
            }
            holder.orderProducts.setAdapter(new OrderProductAdapter(
                    localGoodsOrder.getOrderGoods()));
            final int index = position;
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    showOrderDetail(index);
                }
            });
            holder.orderProducts
                    .setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                                int arg2, long arg3) {
                            showOrderDetail(index);
                        }
                    });
            return convertView;
        }

        class ViewHolder {
            InnerGridView orderProducts;
            TextView orderStateText, orderMoneyText, orderTimeText;
        }
    }

    class OrderProductAdapter extends BaseAdapter {

        List<ModelLocalGoodsOrderGoods> goods;

        public OrderProductAdapter(List<ModelLocalGoodsOrderGoods> goods) {
            this.goods = goods;
        }

        @Override
        public int getCount() {
            return goods.size();
        }

        @Override
        public Object getItem(int position) {
            return goods.get(position);
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
                convertView = layoutInflater.inflate(
                        R.layout.list_product_order, null);
                holder.image = (ImageView) convertView
                        .findViewById(R.id.list_product_order_image);
                holder.productNameText = (TextView) convertView
                        .findViewById(R.id.list_product_order_name);
                holder.productPriceText = (TextView) convertView
                        .findViewById(R.id.list_product_order_price);
                if (goods.size() > 1) {
                    holder.productNameText.setVisibility(View.GONE);
                    holder.productPriceText.setVisibility(View.GONE);
                    holder.image.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
                    convertView.setLayoutParams(new AbsListView.LayoutParams(
                            AbsListView.LayoutParams.MATCH_PARENT,
                            screenWidth / 4));
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ModelLocalGoodsOrderGoods orderGoods = goods.get(position);
            holder.productNameText.setText(orderGoods.getRetailProductName());
            holder.productPriceText.setText(Parameters.CONSTANT_RMB
                    + decimalFormat.format(orderGoods
                    .getRetailProductUnitPrice()));
            ImageLoader.getInstance().displayImage(orderGoods.getImg(),
                    holder.image);
            return convertView;
        }

        class ViewHolder {
            ImageView image;
            TextView productNameText, productPriceText;
        }

    }

}
