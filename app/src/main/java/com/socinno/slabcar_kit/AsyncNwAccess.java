package com.socinno.slabcar_kit;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

// HttpURLConnectionでHTTPアクセス
public class AsyncNwAccess  extends AsyncTask<String, Integer, String> {

    private static final String TAG = "AsyncNwAccess:";
    private SharedPreferences pref;
    private Context curContext;
    private Activity curAccessActivity;

    public AsyncNwAccess(Context context, Activity activity) {
        super();
        /** 点滅させたいView */
        curContext = context;
        curAccessActivity = activity;
        pref = PreferenceManager.getDefaultSharedPreferences(curContext);
    }

    @Override
    protected String doInBackground(String... params) {
        StringBuilder result = new StringBuilder();
        // アクセス先URL
        String acsUrl = params[0];
        String wsServerIP = pref.getString("SERVER_IP", "-");
        if (wsServerIP.equals("-")) {
            return "errAsyncNwAccess0";
        }
        acsUrl = "http://" + wsServerIP + acsUrl;

        Log.d(TAG, "acsUrl=" + acsUrl);
        HttpURLConnection con = null;   // HttpsURLConnectionは、HttpURLConnectionのサブクラスなので、両方いける模様
        try {
            // コネクション取得
            URL url = new URL(acsUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(10000);   // コネクションが接続するまで
            con.setReadTimeout(10000);      // コネクションが確立してからデータ受信まで
            con.connect();
            // HTTPレスポンスコード
            final int status = con.getResponseCode();
            // 通信に成功した　もしくは　Status400の場合でもsCloudからエラーを返すので
            if (status == HttpURLConnection.HTTP_OK ) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                String line;
                boolean firstFlag = true;
                while ((line = reader.readLine()) != null) {
                    if (firstFlag) {
                        result.append(line);
                        firstFlag = false;
                    } else {
                        result.append("\n");
                        result.append(line);
                    }
                }
                // If responseCode is not HTTP_OK
            } else {
                result.append("errAsyncNwAccess1");
            }
        } catch (java.net.SocketTimeoutException e1) {
            e1.printStackTrace();
            // コネクションを切断
            //con.disconnect();
            result.append("errAsyncNwAccess2");
            //result.append("conTimeout");
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            result.append("errAsyncNwAccess3");
        } catch (ProtocolException e1) {
            e1.printStackTrace();
            result.append("errAsyncNwAccess4");
        } catch (IOException e1) {
            e1.printStackTrace();
            result.append("errAsyncNwAccess5");
        } finally { // 例外が発生してもしなくても実行する処理
            if (con != null) {
                // コネクションを切断
                con.disconnect();
            }
        }
        //Log.d(TAG, "response=" + result);
        return result.toString();
    }

    @Override
    // doInBackground()の実行後の処理
    protected void onPostExecute(String result) {      // 実行結果後の処理
        super.onPostExecute(result);
        callbacktask.CallBack(result);
    }

    @Override
    protected void onCancelled() {      // キャンセル時の処理 => 何もしない
    }

    @Override
    protected void onPreExecute() {      // コールバック用のstaticなclass
    }

    /**********　コールバック用　**********/
    private CallBackTask callbacktask;
    public void setOnCallBack(CallBackTask _cbj) {  // コールバック用
        callbacktask = _cbj;
    }

    public static class CallBackTask {      // コールバック用のstaticなclass
        public void CallBack(String result) {
        }
    }
}
