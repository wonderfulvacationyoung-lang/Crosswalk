package com.example.gdtvlive;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class MainActivity extends Activity {

    private WebView webView;
    private int currentIndex = 0;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private boolean dialogShowing = false;

    private String[] channelNames = {
        "广东卫视",
        "珠江频道",
        "体育频道",
        "新闻频道",
        "公共频道",
        "嘉佳卡通",
        "南方卫视",
        "影视频道",
        "少儿频道",
        "房产频道"
    };

    private String[] channelIds = {
        "43", "44", "45", "46", "47", "48", "49", "50", "51", "52"
    };

    private static final String BASE_URL = "https://www.gdtv.cn/tvChannelDetail/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        webView = new WebView(this);
        setContentView(webView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 4.4.2; SmartTV Build/KOT49H) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.132 Safari/537.36"
        );

        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                FrameLayout decor = (FrameLayout) getWindow().getDecorView();
                decor.addView(customView, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                webView.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                if (customView != null) {
                    customView.setVisibility(View.GONE);
                    FrameLayout decor = (FrameLayout) getWindow().getDecorView();
                    decor.removeView(customView);
                    customView = null;
                    if (customViewCallback != null) {
                        customViewCallback.onCustomViewHidden();
                    }
                    webView.setVisibility(View.VISIBLE);
                }
            }
        });

        loadChannel(0);
    }

    private void loadChannel(int index) {
        if (index < 0 || index >= channelIds.length) {
            index = 0;
        }
        currentIndex = index;
        String url = BASE_URL + channelIds[index];
        webView.loadUrl(url);
        Toast.makeText(this, channelNames[index], Toast.LENGTH_SHORT).show();
    }

    private void showChannelList() {
        dialogShowing = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择频道");
        builder.setItems(channelNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadChannel(which);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialogShowing = false;
            }
        });
        dialog.show();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (dialogShowing) {
            return super.dispatchKeyEvent(event);
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_CHANNEL_UP:
                    loadChannel(currentIndex - 1);
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_CHANNEL_DOWN:
                    loadChannel(currentIndex + 1);
                    return true;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_MENU:
                    showChannelList();
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    if (customView != null) {
                        webView.loadUrl("javascript:document.exitFullscreen();");
                        return true;
                    } else if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
                case KeyEvent.KEYCODE_0:
                case KeyEvent.KEYCODE_1:
                case KeyEvent.KEYCODE_2:
                case KeyEvent.KEYCODE_3:
                case KeyEvent.KEYCODE_4:
                case KeyEvent.KEYCODE_5:
                case KeyEvent.KEYCODE_6:
                case KeyEvent.KEYCODE_7:
                case KeyEvent.KEYCODE_8:
                case KeyEvent.KEYCODE_9:
                    int num = keyCode - KeyEvent.KEYCODE_0;
                    int target = (num == 0) ? 10 : num;
                    if (target >= 1 && target <= channelIds.length) {
                        loadChannel(target - 1);
                    }
                    return true;
                default:
                    return false;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
