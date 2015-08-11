package com.androidexperiments.tunnelvision;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by kylephillips on 8/3/15.
 */
public class OpenSourceLicenseActivity extends FragmentActivity
{

    @InjectView(R.id.webView)
    protected WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

        ButterKnife.inject(this);

        InputStream is;
        String htmlData = "";
        try
        {
            is = this.getAssets().open("html/info.html");
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while( (line=r.readLine()) != null )
            {
               stringBuilder.append(line);
            }

            htmlData = stringBuilder.toString();
        } catch( IOException error )
        {

        }


        //mWebView.loadUrl("file:///android_asset/html/info.html");
        mWebView.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "utf-8", "about:blank");

    }

    @OnClick(R.id.closeButton)
    protected void onCloseClicked()
    {
        onBackPressed();
    }
}
