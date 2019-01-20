package com.myscript.iink.demo.mydemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.by.api.hw.ByHwProxy;
import com.by.hw.util.CommonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.myscript.iink.demo.R;
import com.myscript.iink.uireferenceimplementation.JiixDefinitions;

public class MyDemoActivity extends AppCompatActivity implements MyByNote.IRecognitionListener {

    private TextView mTvCan;
    private MyByNote mMyByNote;
    private Handler mHandle= new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_demo);

        mTvCan=findViewById(R.id.tv_can);
        mMyByNote=findViewById(R.id.by_note);
        mMyByNote.initRecogn(this);
        mMyByNote.setIRecognitionListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        CommonUtil.drawDisable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CommonUtil.drawEnable();
        ByHwProxy.drawUnlock();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMyByNote.uninitRecogn();
    }

    @Override
    public void recongnition(String jiixString) {

        Gson gson = new Gson();
        JsonObject result = gson.fromJson(jiixString, JsonObject.class);
        if (result != null)
        {
            JsonArray words = result.getAsJsonArray(JiixDefinitions.Result.WORDS_FIELDNAME);
            mTvCan.setText(words.toString());
        }
        mHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMyByNote.clearAll();
            }
        },1500);

    }
}
