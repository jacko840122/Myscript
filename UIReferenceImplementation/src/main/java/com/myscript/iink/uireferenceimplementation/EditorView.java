// Copyright MyScript. All rights reserved.

package com.myscript.iink.uireferenceimplementation;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.myscript.iink.Configuration;
import com.myscript.iink.ContentPackage;
import com.myscript.iink.ContentPart;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;
import com.myscript.iink.IEditorListener;
import com.myscript.iink.IRenderTarget;
import com.myscript.iink.MimeType;
import com.myscript.iink.ParameterSet;
import com.myscript.iink.PointerEvent;
import com.myscript.iink.PointerEventType;
import com.myscript.iink.PointerType;
import com.myscript.iink.Renderer;
import com.note.book.bean.PointEvent;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorView extends FrameLayout implements IRenderTarget, IEditorListener {
  private int viewWidth;
  private int viewHeight;

  @Nullable
  private Renderer renderer;
  @Nullable
  private Editor editor;

  @Nullable
  private InputController inputController;
  @Nullable
  private ImageLoader imageLoader;

  @Nullable
  private IRenderView renderView;
  @Nullable
  private IRenderView[] layerViews;

  private Map<String, Typeface> typefaceMap = new HashMap<>();

  @Nullable
  private SmartGuideView smartGuideView;

  public EditorView(Context context)
  {
    super(context);
  }

  public EditorView(Context context, @Nullable AttributeSet attrs)
  {
    super(context, attrs);
  }

  public EditorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate()
  {
    super.onFinishInflate();

    // find child render views and initialize them
    for (int i = 0, count = getChildCount(); i < count; ++i)
    {
      View view = getChildAt(i);
      if (view instanceof IRenderView)
      {
        IRenderView renderView = (IRenderView) view;
        if (renderView.isSingleLayerView())
        {
          if (layerViews == null)
            layerViews = new IRenderView[LayerType.values().length];
          layerViews[renderView.getType().ordinal()] = renderView;
        }
        else
        {
          this.renderView = renderView;
        }
        renderView.setRenderTarget(this);
        if (editor != null) // if null it will be transferred in setEngine() below
          renderView.setEditor(editor);
        if (imageLoader != null) // if null it will be transferred in setImageLoader() below
          renderView.setImageLoader(imageLoader);
        renderView.setCustomTypefaces(typefaceMap);
      }
    }

    smartGuideView = findViewById(R.id.smart_guide_view);
  }

  public void close()
  {
    // unplug input management
    setOnTouchListener(null);

    if (editor != null && !editor.isClosed())
    {
      editor.setPart(null);
      editor.setFontMetricsProvider(null);
      editor.close();
      editor = null;
    }

    if (renderer != null && !renderer.isClosed())
    {
      renderer.close();
      renderer = null;
    }
  }

  public void setEngine(@NonNull Engine engine)
  {
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

    renderer = engine.createRenderer(displayMetrics.xdpi, displayMetrics.ydpi, this);

    editor = engine.createEditor(renderer);
    editor.setFontMetricsProvider(new FontMetricsProvider(displayMetrics, typefaceMap));

    Configuration conf = editor.getConfiguration();
    float verticalMarginPX = getResources().getDimension(R.dimen.vertical_margin);
    float horizontalMarginPX = getResources().getDimension(R.dimen.horizontal_margin);
    float verticalMarginMM = 25.4f * verticalMarginPX / displayMetrics.ydpi;
    float horizontalMarginMM = 25.4f * horizontalMarginPX / displayMetrics.xdpi;
    conf.setNumber("text.margin.top", verticalMarginMM);
    conf.setNumber("text.margin.left", horizontalMarginMM);
    conf.setNumber("text.margin.right", horizontalMarginMM);
    conf.setNumber("math.margin.top", verticalMarginMM);
    conf.setNumber("math.margin.bottom", verticalMarginMM);
    conf.setNumber("math.margin.left", horizontalMarginMM);
    conf.setNumber("math.margin.right", horizontalMarginMM);

    smartGuideView = findViewById(R.id.smart_guide_view);
    smartGuideView.setEditor(editor);

    inputController = new InputController(getContext(), this, getEditor());
    setOnTouchListener(inputController);

    // transfer editor to render views
    if (renderView != null)
    {
      renderView.setEditor(editor);
    }
    else if (layerViews != null)
    {
      for (int i = 0; i < layerViews.length; ++i)
      {
        if (layerViews[i] != null)
          layerViews[i].setEditor(editor);
      }
    }

    editor.addListener(this);

    ArrayList<PointerEvent> events = new ArrayList<PointerEvent>();
//
//// Stroke 1
//    events.add(new PointerEvent().down(184.f, 124.f));
//    events.add(new PointerEvent().move(184.f, 125.f));
//    events.add(new PointerEvent().move(184.f, 128.f));
//    events.add(new PointerEvent().move(184.f, 133.f));
//    events.add(new PointerEvent().move(184.f, 152.f));
//    events.add(new PointerEvent().move(184.f, 158.f));
//    events.add(new PointerEvent().move(184.f, 163.f));
//    events.add(new PointerEvent().move(183.f, 167.f));
//    events.add(new PointerEvent().move(183.f, 174.f));
//    events.add(new PointerEvent().move(183.f, 183.f));
//    events.add(new PointerEvent().up(183.f, 184.f));
//
//// Stroke 2
//    events.add(new PointerEvent().down(150.f, 126.f));
//    events.add(new PointerEvent().move(151.f, 126.f));
//    events.add(new PointerEvent().move(152.f, 126.f));
//    events.add(new PointerEvent().move(158.f, 126.f));
//    events.add(new PointerEvent().move(166.f, 126.f));
//    events.add(new PointerEvent().move(184.f, 126.f));
//    events.add(new PointerEvent().move(190.f, 128.f));
//    events.add(new PointerEvent().move(196.f, 128.f));
//    events.add(new PointerEvent().move(200.f, 128.f));
//    events.add(new PointerEvent().move(207.f, 128.f));
//    events.add(new PointerEvent().move(208.f, 128.f));
//    events.add(new PointerEvent().up(209.f, 128.f));

// Feed the editor
//    editor.pointerEvents(events.toArray(new PointerEvent[0]), false);

    try{
      displayMetrics = getResources().getDisplayMetrics();
      renderer = engine.createRenderer(displayMetrics.xdpi, displayMetrics.ydpi, this);
      editor=engine.createEditor(renderer);
      AssetManager assetManager = getContext().getApplicationContext().getAssets();
      Map<String, Typeface> typefaceMap = FontUtils.loadFontsFromAssets(assetManager);
      editor.setFontMetricsProvider(new FontMetricsProvider(displayMetrics,typefaceMap));
      File file = new File(getContext().getFilesDir(), "shit");
      ContentPackage newPackage = editor.getEngine().createPackage(file);
      ContentPart newPart = newPackage.createPart("Text");
      editor.setPart(newPart);
      List<PointEvent> list= PointEvent.deserialize(new File("/mnt/sdcard/aa.bb"));
      List<PointerEvent> desList=new ArrayList<>();
      for(PointEvent pointEvent:list){

        PointerEventType eventType=PointerEventType.DOWN;
        switch (pointEvent.eventType){
          case 0:
            eventType=PointerEventType.DOWN;
            break;
          case 1:
            eventType=PointerEventType.MOVE;
            break;

          case 2:
            eventType=PointerEventType.UP;
            break;

          case 3:
            eventType=PointerEventType.CANCEL;
            break;
        }

        PointerType pointerType=PointerType.TOUCH;
        switch (pointEvent.pointType){
          case 0:
            pointerType=PointerType.PEN;
            break;
          case 1:
            pointerType=PointerType.TOUCH;
            break;

          case 2:
            pointerType=PointerType.ERASER;
            break;

        }
        PointerEvent pointerEvent=new PointerEvent(eventType,pointEvent.x,pointEvent.y, -1,0
                ,pointerType,-1);

        desList.add(pointerEvent);
      }

      editor.addListener(this);
      exportParams = editor.getEngine().createParameterSet();
      exportParams.setBoolean("export.jiix.strokes", false);
      exportParams.setBoolean("export.jiix.bounding-box", false);
      exportParams.setBoolean("export.jiix.glyphs", false);
      exportParams.setBoolean("export.jiix.primitives", false);
      exportParams.setBoolean("export.jiix.chars", false);
      editor.clear();
      editor.pointerEvents((PointerEvent[]) desList.toArray(new PointerEvent[0]),false);

      ParameterSet params = engine.createParameterSet();
// Set the appropriate configuration to exclude strokes from the export
      params.setBoolean("export.jiix.strokes", false);
      String json=editor.export_(null,MimeType.JIIX,params);
      editor.waitForIdle();
      Log.d("shit","editor.export_="+json);
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  private ParameterSet exportParams;

  @Nullable
  public Editor getEditor()
  {
    return editor;
  }

  @Nullable
  public Renderer getRenderer()
  {
    return renderer;
  }

  public void setInputControllerListener(IInputControllerListener listener)
  {
    if (inputController != null)
    {
      inputController.setListener(listener);
      smartGuideView.setSmartGuideMoreHandler(listener);
    }
  }

  public void setImageLoader(ImageLoader imageLoader)
  {
    this.imageLoader = imageLoader;

    // transfer image loader to render views
    if (renderView != null)
    {
      renderView.setImageLoader(imageLoader);
    }
    else if (layerViews != null)
    {
      for (int i = 0; i < layerViews.length; ++i)
      {
        if (layerViews[i] != null)
          layerViews[i].setImageLoader(imageLoader);
      }
    }
  }

  public void setTypefaces(@NonNull Map<String, Typeface> typefaceMap)
  {
    this.typefaceMap = typefaceMap;
    for (int i = 0, count = getChildCount(); i < count; ++i)
    {
      View view = getChildAt(i);
      if (view instanceof IRenderView)
      {
        IRenderView renderView = (IRenderView) view;
        renderView.setCustomTypefaces(typefaceMap);
      }
    }
  }

  public ImageLoader getImageLoader()
  {
    return imageLoader;
  }

  public void setInputMode(int inputMode)
  {
    if (inputController != null)
    {
      inputController.setInputMode(inputMode);
    }
  }

  public int getInputMode()
  {
    return inputController != null ? inputController.getInputMode() : InputController.INPUT_MODE_NONE;
  }

  @Override
  protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight)
  {
    viewWidth = newWidth;
    viewHeight = newHeight;

    if (editor != null)
    {
      editor.setViewSize(newWidth, newHeight);
      invalidate(renderer, EnumSet.allOf(IRenderTarget.LayerType.class));
    }

    super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
  }

  @Override
  public final void invalidate(Renderer renderer, EnumSet<LayerType> layers)
  {
    invalidate(renderer, 0, 0, viewWidth, viewHeight, layers);
  }

  @Override
  public final void invalidate(Renderer renderer, int x, int y, int width, int height, EnumSet<LayerType> layers)
  {
    if (width <= 0 || height <= 0)
      return;

    if (renderView != null)
    {
      renderView.update(renderer, x, y, width, height, layers);
    }
    else if (layerViews != null)
    {
      for (LayerType type : layers)
      {
        IRenderView layerView = layerViews[type.ordinal()];
        if (layerView != null)
          layerView.update(renderer, x, y, width, height, layers);
      }
    }
  }

  @Override
  public void invalidate()
  {
    super.invalidate();
    invalidate(renderer, EnumSet.allOf(LayerType.class));
  }

  @Override
  public void invalidate(int l, int t, int r, int b)
  {
    super.invalidate(l, t, r, b);
    invalidate(renderer, l, t, r - l, b - t, EnumSet.allOf(LayerType.class));
  }

  @Override
  public void invalidate(Rect dirty)
  {
    super.invalidate(dirty);
    int l = dirty.left;
    int t = dirty.top;
    int w = dirty.width();
    int h = dirty.height();
    invalidate(renderer, l, t, w, h, EnumSet.allOf(LayerType.class));
  }


  @Override
  public void partChanging(Editor editor, ContentPart contentPart, ContentPart contentPart1) {

  }

  @Override
  public void partChanged(Editor editor) {

  }

  @Override
  public void contentChanged(Editor editor, String[] strings) {
    String str= null;
    try {
      str = editor.export_(editor.getRootBlock(),MimeType.JIIX, exportParams);
    } catch (IOException e) {
      e.printStackTrace();
    }
    //editor.
    Log.d("ff",str);
  }

  @Override
  public void onError(Editor editor, String s, String s1) {

  }
}
