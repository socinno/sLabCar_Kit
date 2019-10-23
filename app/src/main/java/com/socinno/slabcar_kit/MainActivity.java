package com.socinno.slabcar_kit;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity:";
    private SharedPreferences pref;
    private AsyncNwAccess task1;
    private TextView dispStatusTxt;
    private boolean progressFlag = false;
    private SoundPool mSoundPool;
    private int mSoundId;
    private EditText editTxtIp;
    private String iotServerIP = "";
    private String preServerIP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 表示関連
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        iotServerIP = pref.getString("SERVER_IP", "未設定");
        preServerIP = iotServerIP;
        editTxtIp = findViewById(R.id.editTextIP);
        editTxtIp.setText(iotServerIP);
        // 表示関連
        dispStatusTxt = (TextView) findViewById(R.id.idTextStatus);
        // 操作音
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mSoundId = mSoundPool.load(getApplicationContext(), R.raw.button01a, 0);
    }

    public void carControl(View v){
        // 操作IPを取得
        iotServerIP = editTxtIp.getText().toString();
        if (iotServerIP.equals("未設定") || iotServerIP.equals("") ) {
            dispStatusTxt.setText("未設定");
            return;
        } else if (progressFlag) {
            dispStatusTxt.setText("処理中");
            return;
        }
        // 前回と異なっていれば保存
        if ( !preServerIP.equals(iotServerIP) ) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("SERVER_IP", iotServerIP);
            editor.commit();
        }
        preServerIP = iotServerIP;
        // sLabCarを操作
        ImageView clickView = (ImageView) v;
        int clickIdInt = clickView.getId();
        String clickIdName = getResources().getResourceEntryName(clickIdInt);
        String aUrl = "/cargo?a=STOP";
        if (clickIdName.contains("Go")) {
            aUrl = "/cargo?a=GO";
        } else if (clickIdName.contains("Left") ) {
            aUrl = "/carturn?a=LEFT" + "&n=" + pref.getString("LEFT_TURN", "250");
        } else if (clickIdName.contains("Right") ) {
            aUrl = "/carturn?a=RIGHT" + "&n=" + pref.getString("RIGHT_TURN", "250");
        } else if (clickIdName.contains("Back") ) {
            aUrl = "/cargo?a=BACK";
        } else if (clickIdName.contains("Stop") ) {
            aUrl = "/cargo?a=STOP";
        }
        iotAccess (aUrl);
    }


    protected void iotAccess ( String urlStr) {
        progressFlag = true;
        mSoundPool.play(mSoundId, 1.0F, 1.0F, 0, 0, 1.0F);
        // 非同期でURLにアクセス
        task1 = new AsyncNwAccess(getApplicationContext(), this);
        task1.setOnCallBack(new AsyncNwAccess.CallBackTask() {
            @Override
            public void CallBack(String result) {
                super.CallBack(result);
                String checkRes = result;
                checkRes = checkRes.trim();
                if ( checkRes.indexOf("errAsyncNwAccess") == -1 ){
                    // OK/NG形式
                    if (checkRes.equals("OK")) {
                        dispStatusTxt.setText("処理完了");
                    } else {
                        dispStatusTxt.setText("処理失敗");
                    }
                } else {
                    dispStatusTxt.setText("通信エラー");
                }
                progressFlag = false;
            }
        });
        // ログイン処理のため本体へ接続実行
        task1.execute(urlStr);
    }
}
