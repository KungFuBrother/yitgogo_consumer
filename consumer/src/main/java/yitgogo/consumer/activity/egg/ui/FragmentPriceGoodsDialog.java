package yitgogo.consumer.activity.egg.ui;

import android.content.DialogInterface;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartown.yitian.gogo.R;

public class FragmentPriceGoodsDialog extends DialogFragment implements OnClickListener {


    private int screenWidth;
    private int screenHeight;
    FrameLayout imageLayout;
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDialogDismissListner != null) {
            onDialogDismissListner.dismiss(false);
        }
    }

    private void initView(View view) {
        tvNoPlay = (TextView) view.findViewById(R.id.no_play);
        tvContinue = (TextView) view.findViewById(R.id.continue_play);
        tvTips = (TextView) view.findViewById(R.id.no_price_tips_tv);

        imageLayout = (FrameLayout) view.findViewById(R.id.no_price_tips_iv_layout);
        imageView = (ImageView) view.findViewById(R.id.no_price_tips_iv);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) ((float) screenWidth / 2.0f), (int) ((float) screenWidth / 2.0f));
        layoutParams.gravity = Gravity.CENTER;
        imageLayout.setLayoutParams(layoutParams);

        double imageWidth = Math.sqrt(((float) screenWidth / 2.0f) * ((float) screenWidth / 2.0f) / 2.0f);

        FrameLayout.LayoutParams imageLayoutParams = new FrameLayout.LayoutParams((int) imageWidth, (int) imageWidth);
        layoutParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(imageLayoutParams);

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
        if (v.getId() == tvNoPlay.getId()) {
            if (onDialogDismissListner != null) {
                onDialogDismissListner.dismiss(false);
            }
        } else {
            if (onDialogDismissListner != null) {
                onDialogDismissListner.dismiss(true);
            }
        }
        dismiss();
    }

}
