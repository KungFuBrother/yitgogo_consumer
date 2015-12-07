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
import android.widget.TextView;

import com.smartown.yitian.gogo.R;

public class FragmentPriceMoneyDialog extends DialogFragment implements OnClickListener{


	private int screenWidth;
	private int screenHeight;
	private TextView tvNoPlay;
	private TextView tvContinue;
	private TextView tvTips;
	private String result;

	public static FragmentPriceMoneyDialog newInstance(String result){
		FragmentPriceMoneyDialog priceDialog = new FragmentPriceMoneyDialog();

		Bundle bundle = new Bundle();
		bundle.putString("result", result);
		priceDialog.setArguments(bundle);
		return priceDialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		result = getArguments().getString("result");

		measureScreen();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawableResource(R.color.dialog_bg);

		View view = inflater.inflate(R.layout.price_money_fragment, null);

		initView(view);
		loadTips();
		return view;
	}

	private void initView(View view) {
		tvNoPlay = (TextView) view.findViewById(R.id.no_play);
		tvContinue = (TextView) view.findViewById(R.id.continue_play);
		tvTips = (TextView) view.findViewById(R.id.no_price_tips_tv);

		tvNoPlay.setOnClickListener(this);
		tvContinue.setOnClickListener(this);
	}

	private void loadTips() {

		if(!TextUtils.isEmpty(result)){
			tvTips.setText(result);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if(getDialog() == null){
			return;
		}

		getDialog().getWindow().setLayout(screenWidth*5/7, screenHeight/2);
		getDialog().getWindow().setGravity(Gravity.CENTER);
	}

	private void measureScreen() {
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
	}

	@Override
	public void onClick(View v) {
		if(v == tvNoPlay){
			dismiss();
		}else {
			//支付界面

		}
	}

}
