package com.myscript.iink.demo.mydemo;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.by.hw.drawcomponent.ByNote;
import com.myscript.iink.Configuration;
import com.myscript.iink.ContentPackage;
import com.myscript.iink.ContentPart;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;
import com.myscript.iink.IRenderTarget;
import com.myscript.iink.MimeType;
import com.myscript.iink.ParameterSet;
import com.myscript.iink.PointerEvent;
import com.myscript.iink.PointerEventType;
import com.myscript.iink.PointerType;
import com.myscript.iink.Renderer;
import com.myscript.iink.demo.MainActivity;
import com.myscript.iink.uireferenceimplementation.FontMetricsProvider;
import com.myscript.iink.uireferenceimplementation.FontUtils;
import com.note.book.bean.PointEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class MyByNote extends ByNote implements IRenderTarget, View.OnTouchListener {
    private static final int MSG_RECOGNITION = 0x100;
    private static final String TAG ="MyByNote";
    private Engine mEngine;
    private Editor mEditor;
    private Renderer mRenderer;
    private int mViewWidth;
    private int mViewHeight;
    private ParameterSet mExportParams;

    static private final int RECOGNITION_DELAY=3000;
    private ContentPackage mNewPackage;
    private ContentPart mNewPart;
    private long eventTimeOffset;

    public MyByNote(Context context, AttributeSet attributeSet) throws IOException {
        super(context, attributeSet);
    }

    public MyByNote(Context context) throws IOException {
        super(context);
    }



    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (mEditor == null)
        {
            return false;
        }

        final int action = event.getAction();
        final int actionMask = action & MotionEvent.ACTION_MASK;

        if (actionMask == MotionEvent.ACTION_POINTER_DOWN || actionMask == MotionEvent.ACTION_POINTER_UP)
        {
            final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            return handleOnTouchForPointer(event, actionMask, pointerIndex);
        }
        else
        {
            boolean consumed = false;
            final int pointerCount = event.getPointerCount();
            for (int pointerIndex = 0; pointerIndex < pointerCount; pointerIndex++)
            {
                consumed = consumed || handleOnTouchForPointer(event, actionMask, pointerIndex);
            }
            return consumed;
        }
    }


    private boolean handleOnTouchForPointer(MotionEvent event, int actionMask, int pointerIndex)
    {
        final int pointerId = event.getPointerId(pointerIndex);
        final int pointerType = event.getToolType(pointerIndex);

        PointerType iinkPointerType;
        {
            switch (pointerType)
            {
                case MotionEvent.TOOL_TYPE_STYLUS:
                    iinkPointerType = PointerType.PEN;
                    break;

                case MotionEvent.TOOL_TYPE_ERASER:
                    iinkPointerType = PointerType.ERASER;
                    break;
                case MotionEvent.TOOL_TYPE_FINGER:
                case MotionEvent.TOOL_TYPE_MOUSE:
                    iinkPointerType = PointerType.TOUCH;
                    return false;
                default:
                    // unsupported event type
                    return false;
            }
        }


        switch (actionMask)
        {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                try{
                    mEditor.pointerDown(event.getX(pointerIndex), event.getY(pointerIndex), eventTimeOffset + event.getEventTime(), event.getPressure(), iinkPointerType, pointerId);
                    Log.d(TAG,"down--x="+event.getX(pointerIndex)+"-y="+event.getY(pointerIndex));
                    mHandler.removeMessages(MSG_RECOGNITION);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;

            case MotionEvent.ACTION_MOVE:
                mEditor.pointerMove(event.getX(pointerIndex), event.getY(pointerIndex), eventTimeOffset + event.getEventTime(), event.getPressure(), iinkPointerType, pointerId);
                Log.d(TAG,"move--x="+event.getX(pointerIndex)+"-y="+event.getY(pointerIndex));
                mHandler.removeMessages(MSG_RECOGNITION);
                return false;

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                mEditor.pointerUp(event.getX(pointerIndex), event.getY(pointerIndex), eventTimeOffset + event.getEventTime(), event.getPressure(), iinkPointerType, pointerId);
                Log.d(TAG,"up--x="+event.getX(pointerIndex)+"-y="+event.getY(pointerIndex));
                mHandler.sendEmptyMessageDelayed(MSG_RECOGNITION,RECOGNITION_DELAY);
                return false;

            case MotionEvent.ACTION_CANCEL:
                mEditor.pointerCancel(pointerId);
                mHandler.removeMessages(MSG_RECOGNITION);
                return false;

            default:
                return false;
        }
    }


    public interface IRecognitionListener{
        void recongnition(String json);
    }

    private IRecognitionListener mIRecognitionListener;

    public void setIRecognitionListener(IRecognitionListener iRecognitionListener){
        mIRecognitionListener=iRecognitionListener;
    }


    public void initRecogn(Context context) {
        mEngine = InkUtil.getEngine();

        // configure recognition
        Configuration conf = mEngine.getConfiguration();
        String confDir = "zip://" + context.getPackageCodePath() + "!/assets/conf";
        conf.setStringArray("configuration-manager.search-path", new String[]{confDir});
        String tempDir = context.getFilesDir().getPath() + File.separator + "tmp";
        conf.setString("content-package.temp-folder", tempDir);
        conf.setString("lang", "zh_CN");

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mRenderer = mEngine.createRenderer(displayMetrics.xdpi, displayMetrics.ydpi, this);
        mEditor =mEngine.createEditor(mRenderer);
        AssetManager assetManager = getContext().getApplicationContext().getAssets();
        Map<String, Typeface> typefaceMap = FontUtils.loadFontsFromAssets(assetManager);
        mEditor.setFontMetricsProvider(new FontMetricsProvider(displayMetrics,typefaceMap));

        File file = new File(getContext().getFilesDir(), "myBynote");
        try {
            mNewPackage = mEngine.createPackage(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mNewPart = mNewPackage.createPart("Text");
        mEditor.setPart(mNewPart);
        mExportParams = mEditor.getEngine().createParameterSet();
        mExportParams.setBoolean("export.jiix.strokes", false);
        mExportParams.setBoolean("export.jiix.bounding-box", false);
        mExportParams.setBoolean("export.jiix.glyphs", false);
        mExportParams.setBoolean("export.jiix.primitives", false);
        mExportParams.setBoolean("export.jiix.chars", false);

        setOnTouchListener(this);
        long rel_t = SystemClock.uptimeMillis();
        long abs_t = System.currentTimeMillis();
        eventTimeOffset = abs_t - rel_t;

    }


    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_RECOGNITION:
                    try {

//                        List<PointEvent> list= PointEvent.deserialize(new File("/mnt/sdcard/aa.bb"));
//                        List<PointerEvent> desList=new ArrayList<>();
//                        for(PointEvent pointEvent:list){
//
//                            PointerEventType eventType=PointerEventType.DOWN;
//                            switch (pointEvent.eventType){
//                                case 0:
//                                    eventType=PointerEventType.DOWN;
//                                    break;
//                                case 1:
//                                    eventType=PointerEventType.MOVE;
//                                    break;
//
//                                case 2:
//                                    eventType=PointerEventType.UP;
//                                    break;
//
//                                case 3:
//                                    eventType=PointerEventType.CANCEL;
//                                    break;
//                            }
//
//                            PointerType pointerType=PointerType.TOUCH;
//                            switch (pointEvent.pointType){
//                                case 0:
//                                    pointerType=PointerType.PEN;
//                                    break;
//                                case 1:
//                                    pointerType=PointerType.TOUCH;
//                                    break;
//
//                                case 2:
//                                    pointerType=PointerType.ERASER;
//                                    break;
//
//                            }
//                            PointerEvent pointerEvent=new PointerEvent(eventType,pointEvent.x,pointEvent.y, -1,0
//                                    ,pointerType,-1);
//
//                            desList.add(pointerEvent);
//
//                        }

                        //mEditor.clear();
                       // mEditor.pointerEvents((PointerEvent[]) desList.toArray(new PointerEvent[0]),false);
                        mEditor.waitForIdle();
                        String json=mEditor.export_(null,MimeType.JIIX,mExportParams);
                        Log.d(TAG,"editor.export_="+json);
                        if(mIRecognitionListener!=null){
                            mIRecognitionListener.recongnition(json);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeMessages(MSG_RECOGNITION);
    }

    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight)
    {
        mViewWidth = newWidth;
        mViewHeight = newHeight;

        if (mEditor != null)
        {
            mEditor.setViewSize(newWidth, newHeight);
            //invalidate(mRenderer, EnumSet.allOf(IRenderTarget.LayerType.class));
        }

        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        return super.onTouchEvent(motionEvent);
    }

    public void uninitRecogn() {

        if (mNewPart != null&&!mNewPart.isClosed())
        {
            mNewPart.close();
            mNewPart = null;
        }

        if(mNewPackage!=null&&!mNewPackage.isClosed()){
            mNewPackage.close();
            mNewPackage=null;
        }

        if (mEditor != null && !mEditor.isClosed())
        {
            mEditor.setPart(null);
            mEditor.setFontMetricsProvider(null);
            mEditor.close();
            mEditor = null;
        }

        if (mRenderer != null && !mRenderer.isClosed())
        {
            mRenderer.close();
            mRenderer = null;
        }

    }



    @Override
    public void invalidate(Renderer renderer, EnumSet<LayerType> enumSet) {

    }

    @Override
    public void invalidate(Renderer renderer, int i, int i1, int i2, int i3, EnumSet<LayerType> enumSet) {

    }

    //@Override
    public void clearAll() {
        super.clearAll();
        mEditor.clear();
    }
}
