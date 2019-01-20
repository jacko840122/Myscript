package com.myscript.iink.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by fiona
 * Time 2019/1/10 22:18
 * Describe:点阵
 */
public class PointEvent implements Serializable, Cloneable {
  public int eventType; //DOWN=0,MOVE,UP,CANCEL;
  public float x;
  public float y;
  public int pointType; //PEN=0,TOUCH,ERASER;

  @Override
  protected Object clone() throws CloneNotSupportedException {
    // TODO Auto-generated method stub
    PointEvent pointEvent = null;
    try {
      pointEvent = (PointEvent) super.clone();
    } catch (CloneNotSupportedException e) {
    }
    return pointEvent;
  }

  /**
   * 序列化
   * @param file 文件
   * @param arrays 对象列表
   * @throws IOException
   */
  public static void serialize(File file, List<PointEvent> arrays) throws IOException {
    if (file == null || arrays == null) {
      return;
    }

    if (!file.exists()) {
      file.createNewFile();
    }

    OutputStream os = new FileOutputStream(file);
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(arrays);
    oos.close();
  }

  /**
   *
   * @param file
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static List<PointEvent> deserialize(File file) throws IOException,
          ClassNotFoundException {
    InputStream is = new FileInputStream(file);
    ObjectInputStream ois = new ObjectInputStream(is);
    List<PointEvent> obj = (ArrayList<PointEvent>)ois.readObject();
    ois.close();
    return obj;
  }
}