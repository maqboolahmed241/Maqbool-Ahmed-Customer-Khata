package com.maqboolahmed.customerkhata;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends Activity {
 private WebView web; private ValueCallback<Uri[]> upload; private static final int PICK=41;
 @Override public void onCreate(Bundle state){super.onCreate(state);web=new WebView(this);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setAllowContentAccess(true);web.setWebChromeClient(new WebChromeClient(){@Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> cb,FileChooserParams p){if(upload!=null)upload.onReceiveValue(null);upload=cb;try{startActivityForResult(p.createIntent(),PICK);return true;}catch(Exception e){upload=null;return false;}}});setContentView(web);if(state==null)web.loadUrl("file:///android_asset/index.html");else web.restoreState(state);}
 @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req==PICK&&upload!=null){upload.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res,data));upload=null;}}
 @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);web.saveState(out);}
 @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
