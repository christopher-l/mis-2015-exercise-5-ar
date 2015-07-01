package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargetsUni;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.qualcomm.vuforia.samples.SampleApplication.utils.Texture;

import java.util.Vector;

/**
 * Created by chris on 6/27/15.
 */

// copied all the good codez from https://stackoverflow.com/a/27878442

public class WebDownloader implements Runnable {

    public WebView mWebView;
    private Vector<Texture> mTextures;
    private final String URLS[] = {"",
            "https://www.uni-weimar.de/qisserver/rds?state=wplan&act=Raum&pool=Raum&P.subc=plan&raum.rgid=3218&idcol=raum.rgid&idval=3218&raum.dtxt=015&purge=n&getglobal=n&text=Bauhausstra%C3%9Fe+11+-+Seminarraum++015++%28Seminarraum%2F%C3%9Cbungsraum%29",
            "https://www.uni-weimar.de/qisserver/rds?state=wplan&act=Raum&pool=Raum&P.subc=plan&raum.rgid=2947&idcol=raum.rgid&idval=2947&raum.dtxt=it-ap&purge=n&getglobal=n&text=Karl-Hau%C3%9Fknecht-Stra%C3%9Fe+7+-+H%C3%B6rsaal+%28IT-AP%29++%28H%C3%B6rsaal%2C+eben%29",
            "https://www.uni-weimar.de/en/media/chairs/computer-graphics/",
            "https://www.uni-weimar.de/en/media/chairs/webis/home/",
            "https://www.uni-weimar.de/qisserver/rds?state=wplan&act=Raum&pool=Raum&raum.rgid=2881"
    };
    private int mCurrentId = -1;
    private Activity mActivity;
    private boolean mReady[];


    // Zooming in can improve font quality
    final double scale = 2.0;
    // Unfortunately, there is no method view.getContentWidth()
    final int contentWidth = 800;

    public WebDownloader(Activity activity, Vector<Texture> textures) {

        mActivity = activity;
        mTextures = textures;
        mReady = new boolean[URLS.length];

    }

    private void initialize() {

        mWebView = new WebView(mActivity.getApplicationContext());


        mWebView.setPictureListener(new WebView.PictureListener() {

            @Override
            public void onNewPicture(final WebView view, final Picture picture) {
                if (view.getProgress() == 100 && view.getContentHeight() > 0) {
                    view.setPictureListener(null);
                    // Content is now fully rendered
                    final int width = (int) Math.round(contentWidth * scale);
                    final int height = (int) Math.round(view.getContentHeight() * scale);
                    final Bitmap bitmap = getBitmap(view, width, height);
                    // Display or print bitmap...
                    mTextures.set(mCurrentId, bitmapToTexture(bitmap));
                    mReady[mCurrentId] = true;
                    mCurrentId = -1;
                }
            }
        });

    }

    public synchronized void run() {
//        mActivity.runOnUiThread(new Runnable() {
//            @Override
//            public synchronized void run() {
//                initialize();
//            }
//        });
        while (true) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            if (mCurrentId >= 0) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public synchronized void run() {
                        download(mCurrentId);
                    }
                });
//            }
        }

    }

    public void download(int id) {
        initialize();
        mWebView.loadUrl(URLS[id]);
        mWebView.setInitialScale((int) Math.round(scale * 100));
        // Width and height must be at least 1
        mWebView.layout(0, 0, 1, 1);
    }

    public boolean setId(int id) {
        if (mCurrentId != -1)
            return false;
        else {
            mCurrentId = id;
            return true;
        }
    }

    public boolean ready(int id) {
        return mReady[id];
    }

    private Bitmap getBitmap(
            final WebView view, final int width, final int height) {
        final Bitmap bitmap = Bitmap.createBitmap(
                width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private Texture bitmapToTexture(Bitmap bitmap) {
        int[] data = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(data, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());

        return Texture.loadTextureFromIntBuffer(data, bitmap.getWidth(),
                bitmap.getHeight());
    }

}
