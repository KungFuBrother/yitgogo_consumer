package yitgogo.consumer.activity.egg.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartown.yitian.gogo.R;

import yitgogo.consumer.tools.ScreenUtil;

public class GoldenPriceGoodsAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context context;

    public GoldenPriceGoodsAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public String getItem(int position) {
        return "哈哈哈哈哈哈哈哈哈啊哈哈哈";
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.price_good_item, null);
            holder = new ViewHolder();

            holder.img = (ImageView) convertView.findViewById(R.id.img);
            holder.name = (TextView) convertView.findViewById(R.id.name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(getItem(position));
        holder.img.setBackgroundResource(R.drawable.loading_default);

        LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) holder.img.getLayoutParams();
        params.width = ScreenUtil.getScreenWidth() / 5;
        params.height = ScreenUtil.getScreenWidth() / 5;
        holder.img.setLayoutParams(params);

        return convertView;
    }

    class ViewHolder {
        ImageView img;
        TextView name;
    }

}
