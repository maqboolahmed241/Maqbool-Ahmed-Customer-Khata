package com.maqboolahmed.customerkhata;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.provider.DocumentsContract;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
 private WebView web; private ValueCallback<Uri[]> upload; private static final int PICK=41, PICK_FOLDER=42;
 private Uri backupTree;
 @Override public void onCreate(Bundle state){super.onCreate(state);web=new WebView(this);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setAllowContentAccess(true);
 SharedPreferences prefs=getSharedPreferences("khata_backup",MODE_PRIVATE);String saved=prefs.getString("tree_uri",null);if(saved!=null)backupTree=Uri.parse(saved);
 web.addJavascriptInterface(new Object(){
 @JavascriptInterface public void shareText(String title,String text){runOnUiThread(()->{Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_SUBJECT,title);send.putExtra(Intent.EXTRA_TEXT,text);startActivity(Intent.createChooser(send,"بل کہاں بھیجنا ہے؟"));});}
 @JavascriptInterface public void selectBackupFolder(){runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,PICK_FOLDER);});}
 @JavascriptInterface public void saveBackup(String name,String json){runOnUiThread(()->saveBackupFile(name,json));}
 },"AndroidBridge");
 web.setWebChromeClient(new WebChromeClient(){@Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> cb,FileChooserParams p){if(upload!=null)upload.onReceiveValue(null);upload=cb;try{startActivityForResult(p.createIntent(),PICK);return true;}catch(Exception e){upload=null;return false;}}});setContentView(web);if(state==null)web.loadUrl("file:///android_asset/index.html");else web.restoreState(state);
 }
 private void saveBackupFile(String name,String json){
 try{if(backupTree==null){web.evaluateJavascript("onBackupSaved(false,'پہلے بیک اپ فولڈر منتخب کریں')",null);return;}
 Uri file=DocumentsContract.createDocument(getContentResolver(),backupTree,"application/json",name);
 if(file==null)throw new Exception("فائل نہیں بن سکی");
 try(OutputStream out=getContentResolver().openOutputStream(file)){if(out==null)throw new Exception("فائل نہیں کھل سکی");out.write(json.getBytes(StandardCharsets.UTF_8));}
 web.evaluateJavascript("onBackupSaved(true,'')",null);
 }catch(Exception e){String msg=e.getMessage()==null?"":e.getMessage().replace("\\","\\\\").replace("'","\\'");web.evaluateJavascript("onBackupSaved(false,'"+msg+"')",null);}
 }
 @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);
 if(req==PICK&&upload!=null){upload.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res,data));upload=null;}
 if(req==PICK_FOLDER&&res==RESULT_OK&&data!=null&&data.getData()!=null){Uri tree=data.getData();int flags=data.getFlags()&(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);try{getContentResolver().takePersistableUriPermission(tree,flags);}catch(Exception ignored){}backupTree=tree;getSharedPreferences("khata_backup",MODE_PRIVATE).edit().putString("tree_uri",tree.toString()).apply();if(web!=null)web.evaluateJavascript("onBackupFolderSelected(true)",null);}
 }
 @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);web.saveState(out);}
 @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}