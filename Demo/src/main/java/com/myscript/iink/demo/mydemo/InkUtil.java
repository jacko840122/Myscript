package com.myscript.iink.demo.mydemo;

import com.myscript.certificate.MyCertificate;
import com.myscript.iink.Engine;

public class InkUtil {

    private static Engine engine;

    public static synchronized Engine getEngine()
    {
        if (engine == null)
        {
            engine = Engine.create(MyCertificate.getBytes());
        }

        return engine;
    }

}
