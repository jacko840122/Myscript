package com.myscript.iink.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.by.api.hw.ByHwProxy;
import com.by.hw.util.CommonUtil;

public class MyDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_demo);


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
}
