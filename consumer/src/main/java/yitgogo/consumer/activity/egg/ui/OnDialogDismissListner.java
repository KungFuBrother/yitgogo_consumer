package yitgogo.consumer.activity.egg.ui;

/**
 * Created by Tiger on 2015-12-08.
 */
public abstract class OnDialogDismissListner {

    int count = 0;

    public void dismiss(boolean retry) {
        if (count == 0) {
            count++;
            onDialogDismiss(retry);
        }
    }

    public abstract void onDialogDismiss(boolean retry);

}
