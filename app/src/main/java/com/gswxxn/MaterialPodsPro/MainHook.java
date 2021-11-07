package com.gswxxn.MaterialPodsPro;

import android.os.Build;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class MainHook implements IXposedHookLoadPackage {
    public static final String hookPackageName = "com.pryshedko.materialpods";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam){
        String[][] targetClass = null;
        if (lpparam.packageName.equals(hookPackageName)) {
            // Get app version
            String versionCode = getPackageVersion(lpparam);
            XposedBridge.log("[MPP] APP Current version code: " + versionCode);

            // Read class name from JSON file
            try {
                targetClass = findTargetClass(versionCode);
            }catch (JSONException e) {
                try {
                    XposedBridge.log("[MPP] Not adapted for current version (" + versionCode + "), use default config");
                    targetClass = findTargetClass("default");
                } catch (JSONException jsonException) {
                    XposedBridge.log(jsonException);
                }
            }

            // Start Hook
            if (targetClass != null) {
                try {
                    modifyMethod(targetClass, lpparam.classLoader);
                }catch (XposedHelpers.ClassNotFoundError | NoSuchMethodError e) {
                    XposedBridge.log("[MPP] This version(" + versionCode + ") has not been adapted");
                }
            }
        }
    }

    // Main Hook
    public void modifyMethod(String[][] classAndMethod, ClassLoader cl)
            throws XposedHelpers.ClassNotFoundError, NoSuchMethodError {
        Class<?> clazz1 = XposedHelpers.findClass(classAndMethod[0][0], cl);
        Class<?> clazz2 = XposedHelpers.findClass(classAndMethod[1][0], cl);

        // Modify Class 1
        XposedHelpers.findAndHookMethod(clazz1, classAndMethod[0][1], String.class, boolean.class,
                new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if ("PRO_MODE_ENABLE".equals(param.args[0])) {
                    param.args[1] = true;
                }
            }
        });

        // Modify Class 2
        XposedHelpers.findAndHookMethod(clazz2, classAndMethod[1][1], boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                param.args[0] = true;
            }
        });
    }

    // read class name form json file
    public String[][] findTargetClass(String versionCode) throws JSONException{
        try {
            InputStreamReader isr = new InputStreamReader(
                    Objects.requireNonNull(
                            this.getClass().getClassLoader()).getResourceAsStream("assets/" + "config.json"));
            BufferedReader bfr = new BufferedReader(isr);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bfr.readLine()) != null) {
                stringBuilder.append(line);
            }
            JSONObject root = new JSONObject(stringBuilder.toString());
            JSONObject data = root.getJSONObject("Data").getJSONObject(versionCode);

            String[] class1 = {
                    data.getJSONObject("Class1").getString("ClassName"),
                    data.getJSONObject("Class1").getString("MethodName")};
            String[] class2 = {
                    data.getJSONObject("Class2").getString("ClassName"),
                    data.getJSONObject("Class2").getString("MethodName")};
            return new String[][]{class1, class2};
        } catch (IOException e) {
            XposedBridge.log("[MPP] Here is an exception");
            XposedBridge.log(e);
        }
        return null;
    }

    // getPackageVersion
    private String getPackageVersion(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            File apkPath = new File(lpparam.appInfo.sourceDir);
            int versionCode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Class<?> pkgParserClass = XposedHelpers.findClass(
                        "android.content.pm.PackageParser", lpparam.classLoader);
                Object packageLite = XposedHelpers.callStaticMethod(
                        pkgParserClass, "parsePackageLite", apkPath, 0);
                versionCode = XposedHelpers.getIntField(packageLite, "versionCode");
            } else {
                Class<?> parserCls = XposedHelpers.findClass(
                        "android.content.pm.PackageParser", lpparam.classLoader);
                Object pkg = XposedHelpers.callMethod(
                        parserCls.newInstance(), "parsePackage", apkPath, 0);
                versionCode = XposedHelpers.getIntField(pkg, "mVersionCode");
            }
            return String.valueOf(versionCode);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        return null;
    }
}
