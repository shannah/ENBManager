package io.github.palexdev.enbmanager;

import java.io.InputStream;
import java.net.URL;

/**
 * This class allows the access to the app's assets and resources.
 */
public class Res {

    //================================================================================
    // Constructors
    //================================================================================
    private Res() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static URL get(String name) {
        return Res.class.getResource(name);
    }

    public static String load(String name) {
        return get(name).toExternalForm();
    }

    public static InputStream loadAsset(String name) {
        return Res.class.getResourceAsStream("assets/" + name);
    }

    public static String loadViewCss(String name) {
        return load("css/views/" + name);
    }

    public static String loadComponentCss(String name) {
        return load("css/components/" + name);
    }
}
