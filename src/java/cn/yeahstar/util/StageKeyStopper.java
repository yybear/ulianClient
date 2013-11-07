/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.util;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinUser;
import java.awt.event.KeyEvent;
import javafx.stage.Stage;

/**
 *
 * @author ganqing
 */
public class StageKeyStopper implements Runnable {

    private boolean working = true;
    private Stage stage;
    
    private static WinUser.HHOOK hhk;
    private static WinUser.LowLevelKeyboardProc keyboardHook;

    public StageKeyStopper(Stage stage) {
        this.stage = stage;
    }

    public void stop() {
        working = false;
    }

    public static StageKeyStopper create(Stage stage) {
        StageKeyStopper stopper = new StageKeyStopper(stage);
        
        
        new Thread(stopper, "Alt-Tab Stopper").start();
        return stopper;
    }

    public void run() {
        if(!working)
            return;
        
        final User32 lib = User32.INSTANCE;
        WinDef.HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
        
        keyboardHook = new WinUser.LowLevelKeyboardProc() {
           
            public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinUser.KBDLLHOOKSTRUCT info) {
                boolean flag = false;
                if (nCode >= 0) {
                    switch(wParam.intValue()) {
                    //case WinUser.WM_KEYUP:
                    case WinUser.WM_KEYDOWN:
                    //case WinUser.WM_SYSKEYUP:
                    case WinUser.WM_SYSKEYDOWN:
                        //System.err.println("in callback, key=" + info.vkCode);
                        System.err.print(info.vkCode);
                        if (info.vkCode == 91 || info.vkCode == 164 || info.vkCode == 165
                                || info.vkCode == KeyEvent.VK_TAB
                                || info.vkCode == KeyEvent.VK_ESCAPE) { //win键 alt esc
                            flag = true;
                        }
                    }
                }
                if(flag==false)
                    return lib.CallNextHookEx(hhk, nCode, wParam, info.getPointer());
                else {
                    stage.requestFocus();
                    return new LRESULT(1);
                }
            }
        };
        hhk = lib.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardHook, hMod, 0);
        new Thread() {
            public void run() {
                while (working) {
                    try { Thread.sleep(10); } catch(Exception e) { }
                }
                System.err.println("unhook and exit");
                lib.UnhookWindowsHookEx(hhk);
                System.exit(0);
            }
        }.start();

        // This bit never returns from GetMessage
        int result;
        WinUser.MSG msg = new WinUser.MSG();
        while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
            if (result == -1) {
                System.err.println("error in get message");
                break;
            }
            else {
                System.err.println("got message");
                lib.TranslateMessage(msg);
                lib.DispatchMessage(msg);
            }
        }
        lib.UnhookWindowsHookEx(hhk);
    }
}
