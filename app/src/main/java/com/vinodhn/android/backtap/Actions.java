package com.vinodhn.android.backtap;

import android.text.format.DateFormat;

import java.util.Date;

public class Actions {

    private void takeScreenshot(){
        Date mCurrentTime = new Date();
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", mCurrentTime);
    }

}
