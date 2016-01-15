package yitgogo.consumer.money.model;

import org.json.JSONObject;

/**
 * @author Tiger
 * @Json {"balance":"0","payaccount" :"15081617050001", "seckey":
 * "a61ede39d7ae871588b5e7aae4431762" }
 */
public class MoneyAccount {

    public static MoneyAccount moneyAccount = new MoneyAccount();
    double balance = -1;
    String payaccount = "", seckey = "";
    boolean isLogin = false;

    public MoneyAccount() {
    }

    public MoneyAccount(JSONObject object) {
        if (object != null) {
            isLogin = true;
            if (object.has("balance")) {
                if (!object.optString("balance").equalsIgnoreCase("null")) {
                    balance = object.optDouble("balance");
                }
            }
            if (object.has("payaccount")) {
                if (!object.optString("payaccount").equalsIgnoreCase("null")) {
                    payaccount = object.optString("payaccount");
                }
            }
            if (object.has("seckey")) {
                if (!object.optString("seckey").equalsIgnoreCase("null")) {
                    seckey = object.optString("seckey");
                }
            }
        }
    }

    public static void init(JSONObject object) {
        if (object != null) {
            moneyAccount = new MoneyAccount(object);
        } else {
            moneyAccount = new MoneyAccount();
        }
    }

    public static MoneyAccount getMoneyAccount() {
        return moneyAccount;
    }

    public double getBalance() {
        return balance;
    }

    public String getPayaccount() {
        return payaccount;
    }

    public String getSeckey() {
        return seckey;
    }

    public boolean isLogin() {
        return isLogin;
    }

}
