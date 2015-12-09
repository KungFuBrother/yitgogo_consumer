package yitgogo.consumer.activity.egg.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.smartown.yitian.gogo.R;

import java.util.Random;

public class FragmentNoPriceDialog extends DialogFragment implements OnClickListener {

    private int screenWidth;
    private int screenHeight;
    private TextView tvNoPlay;
    private TextView tvContinue;
    private TextView tvTips;
    private Random random;
    private int num;

    private OnDialogDismissListner onDialogDismissListner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        measureScreen();
        random = new Random();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawableResource(R.color.dialog_bg);

        View view = inflater.inflate(R.layout.no_price_fragment, null);

        initView(view);
        loadRandomText();
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

        tvNoPlay.setOnClickListener(this);
        tvContinue.setOnClickListener(this);
    }

    private void loadRandomText() {
        num = random.nextInt(4);
        switch (num) {
            case 0:

                tvTips.setText("换个姿势，再来一次");
                break;
            case 1:

                tvTips.setText("热下身，再来一次");
                break;
            case 2:

                tvTips.setText("笑一个，再试试");
                break;
            case 3:

                tvTips.setText("先求菩萨保佑，重新来一次");
                break;
            case 4:

                tvTips.setText("不信不中，再来一次");
                break;
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (getDialog() == null) {
            return;
        }

        getDialog().getWindow()
                .setLayout(screenWidth * 5 / 7, screenHeight / 2);
        getDialog().getWindow().setGravity(Gravity.CENTER);
    }

    private void measureScreen() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(metrics);
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
