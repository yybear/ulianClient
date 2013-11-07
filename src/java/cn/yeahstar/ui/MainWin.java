/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.ui;

import cn.yeahstar.dao.impl.CommonDao;
import cn.yeahstar.dao.impl.DaoFactory;
import cn.yeahstar.ex.ErrorCode;
import cn.yeahstar.task.UpdateVersionTask;
import cn.yeahstar.task.UploadDataTask;
import cn.yeahstar.util.ClassLoaderUtils;
import cn.yeahstar.util.ClassUtils;
import cn.yeahstar.util.ConfigUtils;
import cn.yeahstar.util.Contants;
import cn.yeahstar.util.FrameKeyStopper;
import cn.yeahstar.util.JavaAppUtils;
import cn.yeahstar.util.MD5Utils;
import cn.yeahstar.util.MessageUtils;
import cn.yeahstar.util.SimpleLoader;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import netscape.javascript.JSObject;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author ganqing
 */
public class MainWin extends JApplet {
    private static final Log log = LogFactory.getLog(MainWin.class);
    private static final int JFXPANEL_WIDTH_INT = 800;
    private static final int JFXPANEL_HEIGHT_INT = 600;
    private static JFXPanel fxContainer;
    
    private static Timer timer;
    private static FrameKeyStopper stopper;
    
    private static Map<String, String> sysErrors;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ConfigUtils.init();
        log.debug("sys conf is " + ConfigUtils.getUlianConfig("pVersion"));
        // 检测系统
        sysErrors = checkSys();
        if(MapUtils.isEmpty(sysErrors)) {
            // 定时器
            timer = new Timer(true);
            timer.schedule(new UploadDataTask(), ConfigUtils.getLong("upload.task.delay"));
            timer.schedule(new UpdateVersionTask(), ConfigUtils.getLong("update.task.delay"));
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                }
                
                JFrame frame = new JFrame("ulian");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
                JApplet applet = new MainWin();
                applet.init();
                
                frame.setUndecorated(true);
                frame.setContentPane(applet.getContentPane());
                
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                
                applet.start();
            }
        });
    }
    
    @Override
    public void init() {
        fxContainer = new JFXPanel();
        fxContainer.setPreferredSize(new Dimension(JFXPANEL_WIDTH_INT, JFXPANEL_HEIGHT_INT));
        add(fxContainer, BorderLayout.CENTER);
        // create JavaFX scene
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                createScene();
            }
        });
    }
    
    private static Map<String, String> checkSys() {
        Map<String, String> res = new HashMap<String, String>();
        // 数据库文件
        String dbFile = ConfigUtils.getDataDir() + File.separator + Contants.DB_NAME;
        File db = new File(dbFile);
        boolean dbOK = db.exists() && db.isFile();
        if(dbOK == false)
            res.put(""+ErrorCode.SYS_DB_NOT_EXIST, MessageUtils.getErrorMessage(ErrorCode.SYS_DB_NOT_EXIST));
        
        // 数据库版本
        CommonDao commonDao = DaoFactory.getInstance().getCommonDao();
        String schema = commonDao.getEnvSetting("schema.version");
        if(ConfigUtils.get("schema.version").equals(schema)) { //版本不对应
            res.put(""+ErrorCode.SYS_DB_VERSION_ERR, MessageUtils.getErrorMessage(ErrorCode.SYS_DB_VERSION_ERR));
        }
            
        return res;
    }
    
    private void createScene() {
        System.err.println("thread id is " + Thread.currentThread().getId());
        WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();
        
        final WebView smallBrowser = new WebView();
        final WebEngine smallWebEngine = smallBrowser.getEngine();

        Button closeBtn = new Button();
        closeBtn.setText("关闭");
        closeBtn.setPrefSize(100, 20);
        closeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(stopper != null)
                    stopper.stop();
                if(timer != null)
                    timer.cancel();
                Platform.exit();
                System.exit(0);
            }
        });
        
        final JavaApp app = new JavaApp(webEngine);
        // process page loading
        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> ov,
                    Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject win = (JSObject) webEngine.executeScript("window");
                    win.setMember("app", app);
                }
            }
        });
        
        String page = "";
        /*boolean needInit = app.needUpdateData() || app.needUpdateProgram();
        if(needInit) {// 需要初始化则跳到初始化页面
            page = JavaAppUtils.getPageContent(ConfigUtils.get("init.page"));
        } else */
            page = JavaAppUtils.getPageContent(ConfigUtils.get("home.page"));
        webEngine.loadContent(page);
        
        HBox hbox = new HBox(); 
        hbox.setPadding(new Insets(15, 12, 15, 12)); 
        hbox.setSpacing(10); hbox.setStyle("-fx-background-color: #336699"); 
        Button buttonCurrent = new Button("Current"); 
        buttonCurrent.setPrefSize(100, 20); 
        hbox.getChildren().addAll(buttonCurrent, closeBtn);
        
        BorderPane root = new BorderPane();
        root.setTop(hbox);
        root.setCenter(browser);

        Scene scene = new Scene(root);
        fxContainer.setScene(scene);
    }
}
