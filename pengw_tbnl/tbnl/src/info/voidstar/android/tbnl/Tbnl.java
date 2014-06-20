/*
**
** Copyright 2014, Wei Peng <write.to.peng.wei@gmail.com>
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/


package info.voidstar.android.tbnl;

import com.android.internal.os.BaseCommand;
import com.android.internal.os.BinderInternal;

import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Binder;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.IUserManager;
import android.os.UserManager;
import android.os.HandlerThread;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.LoadedApk;
import android.app.IActivityController;
import android.app.IActivityManager;
import android.app.IInstrumentationWatcher;
import android.app.Instrumentation;
import android.app.UiAutomationConnection;

import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ContainerEncryptionParams;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageManager;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.pm.VerificationParams;
import android.content.res.AssetManager;
import android.content.res.Resources;

import android.hardware.input.InputManager;
import android.hardware.input.IInputManager;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.IWindowManager;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AndroidException;
import android.view.Display;
import android.view.IWindowManager;

import android.service.dreams.DreamService;

import android.accounts.IAccountManager;
import android.app.admin.IDevicePolicyManager;
import android.app.backup.IBackupManager;
import android.app.IActivityManager;
import android.app.IAlarmManager;
import android.app.INotificationManager;
import android.app.ISearchManager;
import android.app.IUiModeManager;
import android.app.IWallpaperManager;
import android.bluetooth.IBluetoothManager;
import android.content.pm.IPackageManager;
import android.hardware.display.IDisplayManager;
import android.hardware.input.IInputManager;
import android.hardware.ISerialManager;
import android.hardware.usb.IUsbManager;
import android.net.IConnectivityManager;
import android.net.INetworkPolicyManager;
import android.net.nsd.INsdManager;
import android.os.IPowerManager;
import android.os.IServiceManager;
import android.os.IUserManager;
import android.service.dreams.IDreamManager;
import android.view.accessibility.IAccessibilityManager;
import android.view.IWindowManager;
import com.android.internal.textservice.ITextServicesManager;
import com.android.internal.view.IInputMethodManager;

import dalvik.system.VMRuntime;
import dalvik.system.Zygote;

import android.util.AndroidException;
import android.util.Slog;
import android.text.format.Time;
import android.net.Uri;

import java.lang.Runnable;
import java.lang.Thread;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.android.internal.content.PackageHelper;

public class Tbnl extends BaseCommand {

    private IAccountManager mAccountManager;
    private boolean mAccountManagerEnabled = false;
    private IDevicePolicyManager mDevicePolicyManager;
    private boolean mDevicePolicyManagerEnabled = false;
    private IBackupManager mBackupManager;
    private boolean mBackupManagerEnabled = false;
    private IActivityManager mActivityManager;
    private boolean mActivityManagerEnabled = true;
    private IAlarmManager mAlarmManager;
    private boolean mAlarmManagerEnabled = false;
    private INotificationManager mNotificationManager;
    private boolean mNotificationManagerEnabled = false;
    private ISearchManager mSearchManager;
    private boolean mSearchManagerEnabled = false;
    private IUiModeManager mUiModeManager;
    private boolean mUiModeManagerEnabled = false;
    private IWallpaperManager mWallpaperManager;
    private boolean mWallpaperManagerEnabled = false;
    private IBluetoothManager mBluetoothManager;
    private boolean mBluetoothManagerEnabled = false;
    private IPackageManager mPackageManager;
    private boolean mPackageManagerEnabled = true;
    private IDisplayManager mDisplayManager;
    private boolean mDisplayManagerEnabled = false;
    private IInputManager mInputManager;
    private boolean mInputManagerEnabled = true;
    private ISerialManager mSerialManager;
    private boolean mSerialManagerEnabled = false;
    private IUsbManager mUsbManager;
    private boolean mUsbManagerEnabled = false;
    private IConnectivityManager mConnectivityManager;
    private boolean mConnectivityManagerEnabled = false;
    private INetworkPolicyManager mNetworkPolicyManager;
    private boolean mNetworkPolicyManagerEnabled = false;
    private INsdManager mNsdManager;
    private boolean mNsdManagerEnabled = false;
    private IPowerManager mPowerManager;
    private boolean mPowerManagerEnabled = false;
    private IServiceManager mServiceManager;
    private boolean mServiceManagerEnabled = false;
    private IUserManager mUserManager;
    private boolean mUserManagerEnabled = false;
    private IDreamManager mDreamManager;
    private boolean mDreamManagerEnabled = false;
    private IAccessibilityManager mAccessibilityManager;
    private boolean mAccessibilityManagerEnabled = false;
    private IWindowManager mWindowManager;
    private boolean mWindowManagerEnabled = true;
    private ITextServicesManager mTextServicesManager;
    private boolean mTextServicesManagerEnabled = false;
    private IInputMethodManager mInputMethodManager;
    private boolean mInputMethodManagerEnabled = false;

    // android.util.Log tag
    private static final String TAG = "PengwTbnl";

    private static String sessionId;

    private HandlerThread mMonitorActivityControllerThread;
    private Handler mMonitorActivityControllerHandler;
    private TbnlMonitorActivityController mMonitorActivityController;

    private final String PROP_MONITOR_TARGET_PACKAGE = "tbnl.monitor.targetpackage";

    // errors
    private static final String ERR_NOT_RUNNING = " not running";

    // input sources
    private static final Map<String, Integer> INPUT_SOURCES = new HashMap<String, Integer>() {{
        put("keyboard", InputDevice.SOURCE_KEYBOARD);
        put("dpad", InputDevice.SOURCE_DPAD);
        put("gamepad", InputDevice.SOURCE_GAMEPAD);
        put("touchscreen", InputDevice.SOURCE_TOUCHSCREEN);
        put("mouse", InputDevice.SOURCE_MOUSE);
        put("stylus", InputDevice.SOURCE_STYLUS);
        put("trackball", InputDevice.SOURCE_TRACKBALL);
        put("touchpad", InputDevice.SOURCE_TOUCHPAD);
        put("touchnavigation", InputDevice.SOURCE_TOUCH_NAVIGATION);
        put("joystick", InputDevice.SOURCE_JOYSTICK);
    }};

    private String[] mArgs;
    private int mNextArg;
    private String mCurArgData;

    private int mStartFlags = 0;
    private boolean mWaitOption = false;
    private boolean mStopOption = false;

    private int mRepeat = 0;
    private int mUserId;
    private String mReceiverPermission;

    private String mProfileFile;

    /**
     * Command-line entry point.
     *
     * @param args The command-line arguments
     */
    public static void main(String[] args) {
        (new Tbnl()).run(args);
    }

    @Override
    public void onShowUsage(PrintStream out) {
        out.println(
                "usage: tbnl [subcommand] [options]\n" +
                "usage:\n" +
                "       tbnl start <INTENT>\n" +
                "       tbnl startservice <INTENT>\n" +
                "       tbnl getinfo <PACKAGE_NAME>\n" +
                "       tbnl monitor [<PACKAGE_NAME>]\n" +
                "\n" +
                "tbnl start: start an activity. Options: none.\n" +
                "\n" +
                "tbnl startservice: start a service. Options: none.\n" +
                "\n" +
                "tbnl getinfo: Get static info about the given app. Options: none.\n" +
                "\n" +
                "tbnl monitor: Monitor the given app. Options: none.\n" +
                "\n" +
                "<INTENT> specifications include these flags and arguments:\n" +
                "    [-a <ACTION>] [-d <DATA_URI>] [-t <MIME_TYPE>]\n" +
                "    [-c <CATEGORY> [-c <CATEGORY>] ...]\n" +
                "    [-e|--es <EXTRA_KEY> <EXTRA_STRING_VALUE> ...]\n" +
                "    [--esn <EXTRA_KEY> ...]\n" +
                "    [--ez <EXTRA_KEY> <EXTRA_BOOLEAN_VALUE> ...]\n" +
                "    [--ei <EXTRA_KEY> <EXTRA_INT_VALUE> ...]\n" +
                "    [--el <EXTRA_KEY> <EXTRA_LONG_VALUE> ...]\n" +
                "    [--ef <EXTRA_KEY> <EXTRA_FLOAT_VALUE> ...]\n" +
                "    [--eu <EXTRA_KEY> <EXTRA_URI_VALUE> ...]\n" +
                "    [--ecn <EXTRA_KEY> <EXTRA_COMPONENT_NAME_VALUE>]\n" +
                "    [--eia <EXTRA_KEY> <EXTRA_INT_VALUE>[,<EXTRA_INT_VALUE...]]\n" +
                "    [--ela <EXTRA_KEY> <EXTRA_LONG_VALUE>[,<EXTRA_LONG_VALUE...]]\n" +
                "    [--efa <EXTRA_KEY> <EXTRA_FLOAT_VALUE>[,<EXTRA_FLOAT_VALUE...]]\n" +
                "    [-n <COMPONENT>] [-f <FLAGS>]\n" +
                "    [--grant-read-uri-permission] [--grant-write-uri-permission]\n" +
                "    [--debug-log-resolution] [--exclude-stopped-packages]\n" +
                "    [--include-stopped-packages]\n" +
                "    [--activity-brought-to-front] [--activity-clear-top]\n" +
                "    [--activity-clear-when-task-reset] [--activity-exclude-from-recents]\n" +
                "    [--activity-launched-from-history] [--activity-multiple-task]\n" +
                "    [--activity-no-animation] [--activity-no-history]\n" +
                "    [--activity-no-user-action] [--activity-previous-is-top]\n" +
                "    [--activity-reorder-to-front] [--activity-reset-task-if-needed]\n" +
                "    [--activity-single-top] [--activity-clear-task]\n" +
                "    [--activity-task-on-home]\n" +
                "    [--receiver-registered-only] [--receiver-replace-pending]\n" +
                "    [--selector]\n" +
                "    [<URI> | <PACKAGE> | <COMPONENT>]\n" +
                ""
                );
    }

    @Override
    public void onRun() throws Exception {

        getServiceHandles(); 

        String op = nextArgRequired();

        if (op.equals("tbnl")) {
            // do not advertise this
            runTbnl();
        }
        else if (op.equals("start")) {
            runStart();
        }
        else if (op.equals("getinfo")) {
            runGetInfo();
        }
        else if (op.equals("monitor")) {
            runMonitor();
        }
        else {
            showError("Error: unknown command '" + op + "'");
        }

    }

    private static void say(final String tag, final String what) {
        System.out.println("<" + tag + ">" + what);
    }

    private final static String tagActivityController = "ActivityController";
    private static void sayActivityController(final String what) {
        say(tagActivityController, "session=" + sessionId + "|" + what);
    }

    private final static String tagNormal = "*normal*";
    private static void sayNormal(final String what) {
        say(tagNormal, what);
    }

    private final static String tagError = "*error*";
    private static void sayError(final String what) {
        say(tagError, what);
    }

    private final static String tagOver = "*over*";
    private static void sayOver(final String what) {
        say(tagOver, what);
    }

    private void getServiceHandles() throws InterruptedException {

        // wait before retry to avoid flooding the system with requests
        long waitTime = 2000;


        if (mAccountManagerEnabled) {
            while ((mAccountManager = IAccountManager.Stub.asInterface(ServiceManager.getService(Context.ACCOUNT_SERVICE))) == null) {
                sayError("obtaining IAccountManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mDevicePolicyManagerEnabled) {
            while ((mDevicePolicyManager = IDevicePolicyManager.Stub.asInterface(ServiceManager.getService(Context.DEVICE_POLICY_SERVICE))) == null) {
                sayError("obtaining IDevicePolicyManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mBackupManagerEnabled) {
            while ((mBackupManager = IBackupManager.Stub.asInterface(ServiceManager.getService(Context.BACKUP_SERVICE))) == null) {
                sayError("obtaining IBackupManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mActivityManagerEnabled) {
            while ((mActivityManager = ActivityManagerNative.getDefault()) == null) {
                sayError("obtaining IActivityManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mAlarmManagerEnabled) {
            while ((mAlarmManager = IAlarmManager.Stub.asInterface(ServiceManager.getService(Context.ALARM_SERVICE))) == null) {
                sayError("obtaining IAlarmManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mNotificationManagerEnabled) {
            while ((mNotificationManager = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE))) == null) {
                sayError("obtaining INotificationManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mSearchManagerEnabled) {
            while ((mSearchManager = ISearchManager.Stub.asInterface(ServiceManager.getService(Context.SEARCH_SERVICE))) == null) {
                sayError("obtaining ISearchManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mUiModeManagerEnabled) {
            while ((mUiModeManager = IUiModeManager.Stub.asInterface(ServiceManager.getService(Context.UI_MODE_SERVICE))) == null) {
                sayError("obtaining IUiModeManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mWallpaperManagerEnabled) {
            while ((mWallpaperManager = IWallpaperManager.Stub.asInterface(ServiceManager.getService(Context.WALLPAPER_SERVICE))) == null) {
                sayError("obtaining IWallpaperManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mBluetoothManagerEnabled) {
            while ((mBluetoothManager = IBluetoothManager.Stub.asInterface(ServiceManager.getService(Context.BLUETOOTH_SERVICE))) == null) {
                sayError("obtaining IBluetoothManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mPackageManagerEnabled) {
            while ((mPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"))) == null) {
                sayError("obtaining IPackageManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mDisplayManagerEnabled) {
            while ((mDisplayManager = IDisplayManager.Stub.asInterface(ServiceManager.getService(Context.DISPLAY_SERVICE))) == null) {
                sayError("obtaining IDisplayManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mInputManagerEnabled) {
            while ((mInputManager = IInputManager.Stub.asInterface(ServiceManager.getService(Context.INPUT_SERVICE))) == null) {
                sayError("obtaining IInputManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mSerialManagerEnabled) {
            while ((mSerialManager = ISerialManager.Stub.asInterface(ServiceManager.getService(Context.SERIAL_SERVICE))) == null) {
                sayError("obtaining ISerialManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mUsbManagerEnabled) {
            while ((mUsbManager = IUsbManager.Stub.asInterface(ServiceManager.getService(Context.USB_SERVICE))) == null) {
                sayError("obtaining IUsbManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mConnectivityManagerEnabled) {
            while ((mConnectivityManager = IConnectivityManager.Stub.asInterface(ServiceManager.getService(Context.CONNECTIVITY_SERVICE))) == null) {
                sayError("obtaining IConnectivityManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mNetworkPolicyManagerEnabled) {
            while ((mNetworkPolicyManager = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService(Context.NETWORK_POLICY_SERVICE))) == null) {
                sayError("obtaining INetworkPolicyManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mNsdManagerEnabled) {
            while ((mNsdManager = INsdManager.Stub.asInterface(ServiceManager.getService(Context.NSD_SERVICE))) == null) {
                sayError("obtaining INsdManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mPowerManagerEnabled) {
            while ((mPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE))) == null) {
                sayError("obtaining IPowerManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mUserManagerEnabled) {
            while ((mUserManager = IUserManager.Stub.asInterface(ServiceManager.getService(Context.USER_SERVICE))) == null) {
                sayError("obtaining IUserManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mDreamManagerEnabled) {
            while ((mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.getService(DreamService.DREAM_SERVICE))) == null) {
                sayError("obtaining IDreamManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mAccessibilityManagerEnabled) {
            while ((mAccessibilityManager = IAccessibilityManager.Stub.asInterface(ServiceManager.getService(Context.ACCESSIBILITY_SERVICE))) == null) {
                sayError("obtaining IAccessibilityManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mWindowManagerEnabled) {
            while ((mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE))) == null) {
                sayError("obtaining IWindowManager handler...");
                Thread.sleep(waitTime);
            }
        }

        if (mInputMethodManagerEnabled) {
            while ((mInputMethodManager = IInputMethodManager.Stub.asInterface(ServiceManager.getService(Context.INPUT_METHOD_SERVICE))) == null) {
                sayError("obtaining IInputMethodManager handler...");
                Thread.sleep(waitTime);
            }
        }

    }

    int parseUserArg(String arg) {
        int userId;
        if ("all".equals(arg)) {
            userId = UserHandle.USER_ALL;
        } else if ("current".equals(arg) || "cur".equals(arg)) {
            userId = UserHandle.USER_CURRENT;
        } else {
            userId = Integer.parseInt(arg);
        }
        return userId;
    }

    private Intent makeIntent(int defUser) throws URISyntaxException {
        Intent intent = new Intent();
        Intent baseIntent = intent;
        boolean hasIntentInfo = false;

        mStartFlags = 0;
        mWaitOption = false;
        mStopOption = false;
        mRepeat = 0;
        mProfileFile = null;
        mUserId = defUser;
        Uri data = null;
        String type = null;

        String opt;
        while ((opt=nextOption()) != null) {
            if (opt.equals("-a")) {
                intent.setAction(nextArgRequired());
                if (intent == baseIntent) {
                    hasIntentInfo = true;
                }
            } else if (opt.equals("-d")) {
                data = Uri.parse(nextArgRequired());
                if (intent == baseIntent) {
                    hasIntentInfo = true;
                }
            } else if (opt.equals("-t")) {
                type = nextArgRequired();
                if (intent == baseIntent) {
                    hasIntentInfo = true;
                }
            } else if (opt.equals("-c")) {
                intent.addCategory(nextArgRequired());
                if (intent == baseIntent) {
                    hasIntentInfo = true;
                }
            } else if (opt.equals("-e") || opt.equals("--es")) {
                String key = nextArgRequired();
                String value = nextArgRequired();
                intent.putExtra(key, value);
            } else if (opt.equals("--esn")) {
                String key = nextArgRequired();
                intent.putExtra(key, (String) null);
            } else if (opt.equals("--ei")) {
                String key = nextArgRequired();
                String value = nextArgRequired();
                intent.putExtra(key, Integer.valueOf(value));
            } else if (opt.equals("--eu")) {
                String key = nextArgRequired();
                String value = nextArgRequired();
                intent.putExtra(key, Uri.parse(value));
            } else if (opt.equals("--ecn")) {
                String key = nextArgRequired();
                String value = nextArgRequired();
                ComponentName cn = ComponentName.unflattenFromString(value);
                if (cn == null) throw new IllegalArgumentException("Bad component name: " + value);
                intent.putExtra(key, cn);
            } else if (opt.equals("--eia")) {
                String key = nextArgRequired();
                String value = nextArgRequired();
                String[] strings = value.split(",");
                int[] list = new int[strings.length];
                for (int i = 0; i < strings.length; i++) {
                    list[i] = Integer.valueOf(strings[i]);
                }
                intent.putExtra(key, list);
            } else if (opt.equals("--el")) {
                String key = nextArgRequired();
                String value = nextArgRequired();
                intent.putExtra(key, Long.valueOf(value));
            } else if (opt.equals("--ela")) {
                String key = nextArgRequired();
                String value = nextArgRequired();
                String[] strings = value.split(",");
                long[] list = new long[strings.length];
                for (int i = 0; i < strings.length; i++) {
                    list[i] = Long.valueOf(strings[i]);
                }
                intent.putExtra(key, list);
                hasIntentInfo = true;
            } else if (opt.equals("--ef")) {
                String key = nextArgRequired();
                String value = nextArgRequired();
                intent.putExtra(key, Float.valueOf(value));
                hasIntentInfo = true;
            } else if (opt.equals("--efa")) {
                String key = nextArgRequired();
                String value = nextArgRequired();
                String[] strings = value.split(",");
                float[] list = new float[strings.length];
                for (int i = 0; i < strings.length; i++) {
                    list[i] = Float.valueOf(strings[i]);
                }
                intent.putExtra(key, list);
                hasIntentInfo = true;
            } else if (opt.equals("--ez")) {
                String key = nextArgRequired();
                String value = nextArgRequired();
                intent.putExtra(key, Boolean.valueOf(value));
            } else if (opt.equals("-n")) {
                String str = nextArgRequired();
                ComponentName cn = ComponentName.unflattenFromString(str);
                if (cn == null) throw new IllegalArgumentException("Bad component name: " + str);
                intent.setComponent(cn);
                if (intent == baseIntent) {
                    hasIntentInfo = true;
                }
            } else if (opt.equals("-f")) {
                String str = nextArgRequired();
                intent.setFlags(Integer.decode(str).intValue());
            } else if (opt.equals("--grant-read-uri-permission")) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else if (opt.equals("--grant-write-uri-permission")) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else if (opt.equals("--exclude-stopped-packages")) {
                intent.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
            } else if (opt.equals("--include-stopped-packages")) {
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            } else if (opt.equals("--debug-log-resolution")) {
                intent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
            } else if (opt.equals("--activity-brought-to-front")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            } else if (opt.equals("--activity-clear-top")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } else if (opt.equals("--activity-clear-when-task-reset")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            } else if (opt.equals("--activity-exclude-from-recents")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            } else if (opt.equals("--activity-launched-from-history")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            } else if (opt.equals("--activity-multiple-task")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            } else if (opt.equals("--activity-no-animation")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            } else if (opt.equals("--activity-no-history")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            } else if (opt.equals("--activity-no-user-action")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            } else if (opt.equals("--activity-previous-is-top")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            } else if (opt.equals("--activity-reorder-to-front")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            } else if (opt.equals("--activity-reset-task-if-needed")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            } else if (opt.equals("--activity-single-top")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if (opt.equals("--activity-clear-task")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            } else if (opt.equals("--activity-task-on-home")) {
                intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            } else if (opt.equals("--receiver-registered-only")) {
                intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
            } else if (opt.equals("--receiver-replace-pending")) {
                intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            } else if (opt.equals("--selector")) {
                intent.setDataAndType(data, type);
                intent = new Intent();
            } else if (opt.equals("-D")) {
                mStartFlags |= ActivityManager.START_FLAG_DEBUG;
            } else if (opt.equals("-W")) {
                mWaitOption = true;
            } else if (opt.equals("-P")) {
                mProfileFile = nextArgRequired();
                mStartFlags |= ActivityManager.START_FLAG_AUTO_STOP_PROFILER;
            } else if (opt.equals("--start-profiler")) {
                mProfileFile = nextArgRequired();
                mStartFlags &= ~ActivityManager.START_FLAG_AUTO_STOP_PROFILER;
            } else if (opt.equals("-R")) {
                mRepeat = Integer.parseInt(nextArgRequired());
            } else if (opt.equals("-S")) {
                mStopOption = true;
            } else if (opt.equals("--opengl-trace")) {
                mStartFlags |= ActivityManager.START_FLAG_OPENGL_TRACES;
            } else if (opt.equals("--user")) {
                mUserId = parseUserArg(nextArgRequired());
            } else if (opt.equals("--receiver-permission")) {
                mReceiverPermission = nextArgRequired();
            } else {
                sayError("Error: Unknown option: " + opt);
                return null;
            }
        }
        intent.setDataAndType(data, type);

        final boolean hasSelector = intent != baseIntent;
        if (hasSelector) {
            // A selector was specified; fix up.
            baseIntent.setSelector(intent);
            intent = baseIntent;
        }

        String arg = nextArg();
        baseIntent = null;
        if (arg == null) {
            if (hasSelector) {
                // If a selector has been specified, and no arguments
                // have been supplied for the main Intent, then we can
                // assume it is ACTION_MAIN CATEGORY_LAUNCHER; we don't
                // need to have a component name specified yet, the
                // selector will take care of that.
                baseIntent = new Intent(Intent.ACTION_MAIN);
                baseIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            }
        } else if (arg.indexOf(':') >= 0) {
            // The argument is a URI.  Fully parse it, and use that result
            // to fill in any data not specified so far.
            baseIntent = Intent.parseUri(arg, Intent.URI_INTENT_SCHEME);
        } else if (arg.indexOf('/') >= 0) {
            // The argument is a component name.  Build an Intent to launch
            // it.
            baseIntent = new Intent(Intent.ACTION_MAIN);
            baseIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            baseIntent.setComponent(ComponentName.unflattenFromString(arg));
        } else {
            // Assume the argument is a package name.
            baseIntent = new Intent(Intent.ACTION_MAIN);
            baseIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            baseIntent.setPackage(arg);
        }
        if (baseIntent != null) {
            Bundle extras = intent.getExtras();
            intent.replaceExtras((Bundle)null);
            Bundle uriExtras = baseIntent.getExtras();
            baseIntent.replaceExtras((Bundle)null);
            if (intent.getAction() != null && baseIntent.getCategories() != null) {
                HashSet<String> cats = new HashSet<String>(baseIntent.getCategories());
                for (String c : cats) {
                    baseIntent.removeCategory(c);
                }
            }
            intent.fillIn(baseIntent, Intent.FILL_IN_COMPONENT | Intent.FILL_IN_SELECTOR);
            if (extras == null) {
                extras = uriExtras;
            } else if (uriExtras != null) {
                uriExtras.putAll(extras);
                extras = uriExtras;
            }
            intent.replaceExtras(extras);
            hasIntentInfo = true;
        }

        if (!hasIntentInfo) throw new IllegalArgumentException("No intent supplied");
        return intent;
    }

    private void runStartService() throws Exception {
        Intent intent = makeIntent(UserHandle.USER_CURRENT);
        if (mUserId == UserHandle.USER_ALL) {
            sayError("Error: Can't start activity with user 'all'");
            return;
        }
        sayNormal("Starting service: " + intent);
        ComponentName cn = mActivityManager.startService(null, intent, intent.getType(), mUserId);
        if (cn == null) {
            sayError("Error: Not found; no service started.");
        } else if (cn.getPackageName().equals("!")) {
            sayError("Error: Requires permission " + cn.getClassName());
        } else if (cn.getPackageName().equals("!!")) {
            sayError("Error: " + cn.getClassName());
        }
    }    

    private void runStart() throws Exception {
        Intent intent = makeIntent(UserHandle.USER_CURRENT);

        if (mUserId == UserHandle.USER_ALL) {
            sayError("Error: Can't start service with user 'all'");
            return;
        }

        String mimeType = intent.getType();
        if (mimeType == null && intent.getData() != null
                && "content".equals(intent.getData().getScheme())) {
            mimeType = mActivityManager.getProviderMimeType(intent.getData(), mUserId);
        }

        do {
            if (mStopOption) {
                String packageName;
                if (intent.getComponent() != null) {
                    packageName = intent.getComponent().getPackageName();
                } else {
                    IPackageManager pm = IPackageManager.Stub.asInterface(
                            ServiceManager.getService("package"));
                    if (pm == null) {
                        sayError("Error: Package manager not running; aborting");
                        return;
                    }
                    List<ResolveInfo> activities = pm.queryIntentActivities(intent, mimeType, 0,
                            mUserId);
                    if (activities == null || activities.size() <= 0) {
                        sayError("Error: Intent does not match any activities: "
                                + intent);
                        return;
                    } else if (activities.size() > 1) {
                        sayError("Error: Intent matches multiple activities; can't stop: "
                                + intent);
                        return;
                    }
                    packageName = activities.get(0).activityInfo.packageName;
                }
                sayNormal("Stopping: " + packageName);
                mActivityManager.forceStopPackage(packageName, mUserId);
                Thread.sleep(250);
            }
    
            sayNormal("Starting: " + intent);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    
            ParcelFileDescriptor fd = null;
    
            if (mProfileFile != null) {
                try {
                    fd = ParcelFileDescriptor.open(
                            new File(mProfileFile),
                            ParcelFileDescriptor.MODE_CREATE |
                            ParcelFileDescriptor.MODE_TRUNCATE |
                            ParcelFileDescriptor.MODE_READ_WRITE);
                } catch (FileNotFoundException e) {
                    sayError("Error: Unable to open file: " + mProfileFile);
                    return;
                }
            }

            IActivityManager.WaitResult result = null;
            int res;
            if (mWaitOption) {
                result = mActivityManager.startActivityAndWait(null, null, intent, mimeType,
                            null, null, 0, mStartFlags, mProfileFile, fd, null, mUserId);
                res = result.result;
            } else {
                res = mActivityManager.startActivityAsUser(null, null, intent, mimeType,
                        null, null, 0, mStartFlags, mProfileFile, fd, null, mUserId);
            }
            PrintStream out = mWaitOption ? System.out : System.err;
            boolean launched = false;
            switch (res) {
                case ActivityManager.START_SUCCESS:
                    launched = true;
                    break;
                case ActivityManager.START_SWITCHES_CANCELED:
                    launched = true;
                    out.println(
                            "Warning: Activity not started because the "
                            + " current activity is being kept for the user.");
                    break;
                case ActivityManager.START_DELIVERED_TO_TOP:
                    launched = true;
                    out.println(
                            "Warning: Activity not started, intent has "
                            + "been delivered to currently running "
                            + "top-most instance.");
                    break;
                case ActivityManager.START_RETURN_INTENT_TO_CALLER:
                    launched = true;
                    out.println(
                            "Warning: Activity not started because intent "
                            + "should be handled by the caller");
                    break;
                case ActivityManager.START_TASK_TO_FRONT:
                    launched = true;
                    out.println(
                            "Warning: Activity not started, its current "
                            + "task has been brought to the front");
                    break;
                case ActivityManager.START_INTENT_NOT_RESOLVED:
                    out.println(
                            "Error: Activity not started, unable to "
                            + "resolve " + intent.toString());
                    break;
                case ActivityManager.START_CLASS_NOT_FOUND:
                    out.println(NO_CLASS_ERROR_CODE);
                    out.println("Error: Activity class " +
                            intent.getComponent().toShortString()
                            + " does not exist.");
                    break;
                case ActivityManager.START_FORWARD_AND_REQUEST_CONFLICT:
                    out.println(
                            "Error: Activity not started, you requested to "
                            + "both forward and receive its result");
                    break;
                case ActivityManager.START_PERMISSION_DENIED:
                    out.println(
                            "Error: Activity not started, you do not "
                            + "have permission to access it.");
                    break;
                default:
                    out.println(
                            "Error: Activity not started, unknown error code " + res);
                    break;
            }
            if (mWaitOption && launched) {
                if (result == null) {
                    result = new IActivityManager.WaitResult();
                    result.who = intent.getComponent();
                }
                sayNormal("Status: " + (result.timeout ? "timeout" : "ok"));
                if (result.who != null) {
                    sayNormal("Activity: " + result.who.flattenToShortString());
                }
                if (result.thisTime >= 0) {
                    sayNormal("ThisTime: " + result.thisTime);
                }
                if (result.totalTime >= 0) {
                    sayNormal("TotalTime: " + result.totalTime);
                }
                sayNormal("Complete");
            }
            mRepeat--;
            if (mRepeat > 1) {
                mActivityManager.unhandledBack();
            }
        } while (mRepeat > 1);
    }

    private void sendBroadcast() throws Exception {
        Intent intent = makeIntent(UserHandle.USER_ALL);
        IntentReceiver receiver = new IntentReceiver();
        sayNormal("Broadcasting: " + intent);
        mActivityManager.broadcastIntent(null, intent, null, receiver, 0, null, null, mReceiverPermission,
                android.app.AppOpsManager.OP_NONE, true, false, mUserId);
        receiver.waitForFinish();
    }

    static void removeWallOption() {
        String props = SystemProperties.get("dalvik.vm.extra-opts");
        if (props != null && props.contains("-Xprofile:wallclock")) {
            props = props.replace("-Xprofile:wallclock", "");
            props = props.trim();
            SystemProperties.set("dalvik.vm.extra-opts", props);
        }
    }

    private class IntentReceiver extends IIntentReceiver.Stub {
        private boolean mFinished = false;

        @Override
        public void performReceive(Intent intent, int resultCode, String data, Bundle extras,
                boolean ordered, boolean sticky, int sendingUser) {
            String line = "Broadcast completed: result=" + resultCode;
            if (data != null) line = line + ", data=\"" + data + "\"";
            if (extras != null) line = line + ", extras: " + extras;
            sayNormal(line);
            synchronized (this) {
              mFinished = true;
              notifyAll();
            }
        }

        public synchronized void waitForFinish() {
            try {
                while (!mFinished) wait();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private class InstrumentationWatcher extends IInstrumentationWatcher.Stub {
        private boolean mFinished = false;
        private boolean mRawMode = false;

        /**
         * Set or reset "raw mode".  In "raw mode", all bundles are dumped.  In "pretty mode",
         * if a bundle includes Instrumentation.REPORT_KEY_STREAMRESULT, just print that.
         * @param rawMode true for raw mode, false for pretty mode.
         */
        public void setRawOutput(boolean rawMode) {
            mRawMode = rawMode;
        }

        @Override
        public void instrumentationStatus(ComponentName name, int resultCode, Bundle results) {
            synchronized (this) {
                // pretty printer mode?
                String pretty = null;
                if (!mRawMode && results != null) {
                    pretty = results.getString(Instrumentation.REPORT_KEY_STREAMRESULT);
                }
                if (pretty != null) {
                    System.out.print(pretty);
                } else {
                    if (results != null) {
                        for (String key : results.keySet()) {
                            sayNormal(
                                    "INSTRUMENTATION_STATUS: " + key + "=" + results.get(key));
                        }
                    }
                    sayNormal("INSTRUMENTATION_STATUS_CODE: " + resultCode);
                }
                notifyAll();
            }
        }

        @Override
        public void instrumentationFinished(ComponentName name, int resultCode,
                Bundle results) {
            synchronized (this) {
                // pretty printer mode?
                String pretty = null;
                if (!mRawMode && results != null) {
                    pretty = results.getString(Instrumentation.REPORT_KEY_STREAMRESULT);
                }
                if (pretty != null) {
                    sayNormal(pretty);
                } else {
                    if (results != null) {
                        for (String key : results.keySet()) {
                            sayNormal(
                                    "INSTRUMENTATION_RESULT: " + key + "=" + results.get(key));
                        }
                    }
                    sayNormal("INSTRUMENTATION_CODE: " + resultCode);
                }
                mFinished = true;
                notifyAll();
            }
        }

        public boolean waitForFinish() {
            synchronized (this) {
                while (!mFinished) {
                    try {
                        if (!mActivityManager.asBinder().pingBinder()) {
                            return false;
                        }
                        wait(1000);
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
            return true;
        }
    }

    private String getPackageName() {
        String packageName = nextArgRequired();

        boolean pkgAvail = true;
        
        try {
            if(mPackageManager.getPackageInfo(packageName, 0, 0) == null) {
                pkgAvail = false;
            }
        }
        catch (RemoteException e) {
            pkgAvail = false;
        }

        return pkgAvail ? packageName : null;
    }

    /* the lab---before its function mature and trickle down to exposed interface */
    private void runTbnl() throws Exception {
        final String packageName = getPackageName();

        HandlerThread thd = new HandlerThread("for systemMain");
        thd.start();

        Handler myHld = new Handler(thd.getLooper()); 

        myHld.post(
                new Runnable() {
                    public void run() {
                        final Context sysCtx = ActivityThread.systemMain().getSystemContext();
                        final LoadedApk pkgApk = ActivityThread.systemMain().getPackageInfo(packageName, null, 0);
                        {
                            sayNormal(packageName);
                            sayNormal(ActivityThread.currentPackageName());
                            sayNormal(ActivityThread.currentProcessName());
                            //sayNormal(pkgApk.getAppDir());
                        }
                    }
                }
                );

    }

    private void runGetInfo() throws Exception {
        final String packageName = getPackageName();

        if (packageName == null) {
            return;
        }

        PackageInfo pkgInfo = mPackageManager.getPackageInfo(
                packageName, 
                // flags - Additional option flags. Use any combination of GET_ACTIVITIES, GET_GIDS, GET_CONFIGURATIONS, GET_INSTRUMENTATION, GET_PERMISSIONS, GET_PROVIDERS, GET_RECEIVERS, GET_SERVICES, GET_SIGNATURES, GET_UNINSTALLED_PACKAGES to modify the data returned. 
                PackageManager.GET_ACTIVITIES | 
                PackageManager.GET_CONFIGURATIONS | 
                PackageManager.GET_PERMISSIONS | 
                PackageManager.GET_PROVIDERS | 
                PackageManager.GET_RECEIVERS | 
                PackageManager.GET_SERVICES | 
                0, 
                UserHandle.getUserId(UserHandle.USER_OWNER));

        if (pkgInfo == null) {
            sayError(
                    "cannot get package information for package '" +
                    packageName + 
                    "'"
                    );
            return;
        }

        if (pkgInfo.activities != null) {
            for (ActivityInfo activity: pkgInfo.activities) {
                sayNormal(
                        "type=activity|package=" +
                        pkgInfo.packageName +
                        "|name=" +
                        activity.name
                        );
            }
        }

        if (pkgInfo.services != null) {
            for (ServiceInfo service: pkgInfo.services) {
                sayNormal(
                        "type=service|package=" +
                        pkgInfo.packageName +
                        "|name=" +
                        service.name
                        );
            }
        }

        if (pkgInfo.providers != null) {
            for (ProviderInfo provider: pkgInfo.providers) {
                sayNormal(
                        "type=provider|package=" +
                        pkgInfo.packageName +
                        "|name=" +
                        provider.name
                        );
            }
        }

        if (pkgInfo.receivers != null) {
            for (ActivityInfo receiver: pkgInfo.receivers) {
                sayNormal(
                        "type=receiver|package=" +
                        pkgInfo.packageName +
                        "|name=" +
                        receiver.name
                        );
            }
        }

    }

    class TbnlMonitorActivityController extends IActivityController.Stub {

        final private IPackageManager mPkgManager;
        private Time mNow;

        TbnlMonitorActivityController(IPackageManager pkgManager) {
            mPkgManager = pkgManager; 
            mNow = new Time();
        }

        public String getTargetPkg() {
            String targetPkg = SystemProperties.get(PROP_MONITOR_TARGET_PACKAGE);

            try {
                if(mPackageManager.getPackageInfo(targetPkg, 0, 0) == null) {
                    targetPkg = null;
                }
            }
            catch (RemoteException e) {
                targetPkg = null;
            } 

            return targetPkg;
        }

        @Override
        public boolean activityResuming(String pkg) {
            synchronized (this) {
                final String targetPkg = getTargetPkg();
                if (targetPkg == null || pkg.equals(targetPkg)) {
                    mNow.setToNow();
                    sayActivityController(
                            "timestamp=" + mNow.toMillis(true) + "|action=resume|package=" + pkg
                            );
                }
                return true;
            }
        }

        @Override
        public boolean activityStarting(Intent intent, String pkg) {
            synchronized (this) {
                final String targetPkg = getTargetPkg();

                StringBuilder tmp = new StringBuilder();
                synchronized (intent) {
                    Set<String> cat = intent.getCategories();
                    if (cat == null) {
                        tmp.append("null");
                    }
                    else {
                        for (String c: cat) {
                            tmp.append(c);
                            tmp.append(";");
                        }
                        // remove trailing excess ";"
                        tmp.deleteCharAt(tmp.length() - 1);
                    }
                }
                String categories = tmp.toString();

                if (targetPkg == null || pkg.equals(targetPkg)) {

                    mNow.setToNow();
                    sayActivityController(
                            "timestamp=" + mNow.toMillis(true) + "|action=start|package=" + pkg + 
                            "|intent-action=" + intent.getAction() +
                            "|intent-component=" + intent.getComponent().getPackageName() + "/" + intent.getComponent().getShortClassName() +
                            "|intent-category=" + categories +
                            "|intent-data=" + intent.getDataString() +
                            "|intent-extras=" + intent.getExtras() +
                            "|intent-flags=" + String.format("%08x", intent.getFlags()) +
                            ""
                            );
                }
                return true;
            }
        }

        @Override
        public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg,
                long timeMillis, String stackTrace) {
            synchronized (this) {
                List<ActivityManager.RunningAppProcessInfo> appprocs;

                try {
                    appprocs = mActivityManager.getRunningAppProcesses();
                }
                catch (RemoteException e) {
                    return true;
                }

                if (appprocs != null) {
                    for (ActivityManager.RunningAppProcessInfo appproc: appprocs) {
                        if (pid == appproc.pid && processName.equals(appproc.processName)) {
                            String[] pkglist = appproc.pkgList;

                            if (pkglist != null) {
                                StringBuilder tmp = new StringBuilder();

                                for (String pkg: pkglist) {
                                    tmp.append(pkg);
                                    tmp.append(";");
                                }
                                // remove trailing excess ";"
                                tmp.deleteCharAt(tmp.length() - 1);

                                {
                                    mNow.setToNow();
                                    sayActivityController(
                                            "timestamp=" + mNow.toMillis(true) +
                                            "|action=crashed|package=" +
                                            tmp.toString()
                                            );
                                }
                            }
                        }
                    }


                }
                            
                return true; 
            }
        }

        @Override
        public int appEarlyNotResponding(String processName, int pid, String annotation) {
            synchronized (this) {
                return 1;
            }
        }

        @Override
        public int appNotResponding(String processName, int pid, String processStats) {
            synchronized (this) {
                return 1;
            }
        }

        @Override
        public int systemNotResponding(String message) {
            synchronized (this) {
                return 1;
            }
        }

    }

    private void runMonitor() throws Exception {
        final String packageName = getPackageName();

        sessionId = UUID.randomUUID().toString();

        mMonitorActivityControllerThread = new HandlerThread("ActivityControllerLooper");
        mMonitorActivityControllerThread.start(); 

        mMonitorActivityControllerHandler = new Handler(
                mMonitorActivityControllerThread.getLooper(),
                new Handler.Callback() {
                    public boolean handleMessage(Message msg) {
                        return true;
                    }
                }
                );

        mMonitorActivityController = new TbnlMonitorActivityController(mPackageManager);

        mMonitorActivityControllerHandler.post(
                new Runnable() {
                    public void run() {
                        try {
                            mActivityManager.setActivityController(
                                mMonitorActivityController
                                );
                        }
                        catch (RemoteException e) {
                        }
                    }
                }
                );

        SystemProperties.set(PROP_MONITOR_TARGET_PACKAGE, packageName);

        try {
            while (true) {
                Thread.sleep(10000);
            }
        }
        catch (InterruptedException e) {
        }
        finally {
            mActivityManager.setActivityController(null);
        }

    }



}
