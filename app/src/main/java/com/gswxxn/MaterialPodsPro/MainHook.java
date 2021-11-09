package com.gswxxn.MaterialPodsPro;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    public static final String hookPackageName = "com.pryshedko.materialpods";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals(hookPackageName)) {

            // Remove popup ads
            try{
                Class<?> clazz = XposedHelpers.findClass(
                        "com.pryshedko.materialpods.model.settings.PopupSettings", lpparam.classLoader);
                XposedHelpers.findAndHookConstructor(clazz,
                        int.class, int.class, String.class, float.class, float.class, long.class, int.class, float.class,
                        float.class, float.class, String.class, int.class, String.class, float.class, boolean.class,
                        boolean.class, boolean.class, int.class, int.class, boolean.class, boolean.class,
                        new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        param.args[20] = false;
                    }
            });}catch (Exception ignored){}

            // Open Pro mode
            try{
                Class<?> onCreateClazz = XposedHelpers.findClass(
                        "com.pryshedko.materialpods.MainActivity", lpparam.classLoader);
                XposedHelpers.findAndHookMethod(onCreateClazz, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                        SharedPreferences sp = context.getSharedPreferences("com.pryshedko.materialpods", 0);
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putBoolean("PRO_MODE_ENABLED", true);
                        edit.apply();
                    }
                });}catch (Exception ignored) {}
        }
    }
}