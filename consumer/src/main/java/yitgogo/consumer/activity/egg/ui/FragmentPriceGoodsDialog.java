package yitgogo.consumer.activity.egg.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartown.yitian.gogo.R;

public class FragmentPriceGoodsDialog extends DialogFragment implements OnClickListener {


    private int screenWidth;
    private int screenHeight;
    private ImageView imageView;
    private TextView tvNoPlay;
    private TextView tvContinue;
    private TextView tvTips;
    private String name = "", image = "";

    private OnDialogDismissListner onDialogDismissListner;

    public static FragmentPriceGoodsDialog newInstance(String name, String image) {
        FragmentPriceGoodsDialog priceDialog = new FragmentPriceGoodsDialog();

        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("image", image);
        priceDialog.setArguments(bundle);
        return priceDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        name = getArguments().getString("name");
        image = getArguments().getString("image");

        measureScreen();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawableResource(R.color.dialog_bg);

        View view = inflater.inflate(R.layout.price_goods_fragment, null);

        initView(view);
        loadTips();
        return view;
    }

    private void initView(View view) {
        tvNoPlay = (TextView) view.findViewById(R.id.no_play);
        tvContinue = (TextView) view.findViewById(R.id.continue_play);
        tvTips = (TextView) view.findViewById(R.id.no_price_tips_tv);
        imageView = (ImageView) view.findViewById(R.id.no_price_tips_iv);

        tvNoPlay.setOnClickListener(this);
        tvContinue.setOnClickListener(this);
    }

    private void loadTips() {

        if (!TextUtils.isEmpty(name)) {
            tvTips.setText(name);
        }
        ImageLoader.getInstance().displayImage(image, imageView);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getDialog() == null) {
            return;
        }

        getDialog().getWindow().setLayout(screenWidth * 5 / 7, screenHeight / 2);
        getDialog().getWindow().setGravity(Gravity.CENTER);
    }

    private void measureScreen() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    public void setOnDialogDismissListner(OnDialogDismissListner onDialogDismissListner) {
        this.onDialogDismissListner = onDialogDismissListner;
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (v.getId() == tvNoPlay.getId()) {
            if (onDialogDismissListner != null) {
                onDialogDismissListner.onDialogDismiss(false);
            }
        } else {
            //支付界面
            if (onDialogDismissListner != null) {
                onDialogDismissListner.onDialogDismiss(true);
            }
        }
    }


}
