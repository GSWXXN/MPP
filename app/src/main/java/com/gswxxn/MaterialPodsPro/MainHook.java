package com.gswxxn.MaterialPodsPro;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    public static final String hookPackageName = "com.pryshedko.materialpods";
    public static Class<?> clazz;
    public static Class<?> clazz2;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if(lpparam.packageName.equals(hookPackageName)){
            // Find target Class
            if(findTargetClass(lpparam.classLoader)){
                // Modify Class 1
                XposedHelpers.findAndHookMethod(clazz, "a",String.class, boolean.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        if ("PRO_MODE_ENABLE".equals(param.args[0])){
                            param.args[1] = true;
                        }
                    }
                });

                // Modify Class 2
                XposedHelpers.findAndHookMethod(clazz2, "c", boolean.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        param.args[0] = true;
                    }
                });
            }
        }
    }

    public boolean findTargetClass(ClassLoader cl) {
        char[] range = new char[26];
        for(int i = 0; i < 26; i++){
            range[i] = (char) (i + 97);
        }

        for(char i : range){
            for(char j : range) {
                for (char k : range) {

                    try {
                        clazz = XposedHelpers.findClass(i + ".a.a." + j + "." + k + ".a.a", cl);
                        clazz2 = XposedHelpers.findClass(i + ".a.a." + j + "." + k + ".b.a", cl);

                        if(XposedHelpers.findMethodExactIfExists(clazz, "a", String.class, boolean.class)
                                == null){
                            continue;
                        }

                        XposedBridge.log("Found class: "
                                + "\"" + i + ".a.a." + j + "." + k + ".a.a" + "\""
                                + " & "
                                + "\"" + i + ".a.a." + j + "." + k + ".b.a" + "\"");
                        return true;

                    } catch (XposedHelpers.ClassNotFoundError e) { }
                }
            }
        }

        XposedBridge.log("Cannot find target classes");
        return false;
    }
}
