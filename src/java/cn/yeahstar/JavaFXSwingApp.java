/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar;

import cn.yeahstar.dao.UlianUsersDao;
import cn.yeahstar.dao.impl.DaoFactory;
import cn.yeahstar.dao.impl.UlianUsersDaoImpl;
import cn.yeahstar.jdbc.DBManager;
import cn.yeahstar.net.FxFileDownload;
import cn.yeahstar.jdbc.DBUtilsTransactionHelper;
import cn.yeahstar.util.FrameKeyStopper;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;



/**
 *
 * @author ganqing
 */
public class JavaFXSwingApp extends JApplet {
    private static final Log LOG = LogFactory.getLog(JavaFXSwingApp.class);
    private static JFXPanel fxContainer;
    private static JFrame frame;
    private static Popup popup;
    
    private static FrameKeyStopper stopper;
    
    private static boolean working = true;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception e) {
                }

                frame = new JFrame("JavaFX 2 in Swing");
                //frame.setUndecorated(true);
                //frame.setExtendedState(JFrame.MAXIMIZED_BOTH); //最大化
                //frame.setAlwaysOnTop(true);    //总在最前面
                //frame.setResizable(false);    //不能改变大小
                //frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                //stopper = FrameKeyStopper.create(frame);

                JApplet applet = new JavaFXSwingApp();
                applet.init();

                frame.setContentPane(applet.getContentPane());

                
                frame.pack();
                frame.setLocationRelativeTo(null);
                //frame.setLocationByPlatform(true);
                frame.setVisible(true);
                
               /* new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(working) {
                            frame.toFront();
                        }
                    }
                }).start();*/
                
                applet.start();
            }
        });
    }

    @Override
    public void init() {
        fxContainer = new JFXPanel();
        //fxContainer.setPreferredSize(new Dimension(JFXPANEL_WIDTH_INT, JFXPANEL_HEIGHT_INT));
        add(fxContainer, BorderLayout.CENTER);
        // create JavaFX scene
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                createScene();
            }
        });
    }

    private String getPageContent(String name) {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("page/" + name);
        String page = "";
        try {
            page = IOUtils.toString(in, "utf-8");
        } catch (IOException ex) {
        }

        return page;
    }

    public void createScene() {
        popup = new Popup();
        popup.setX(300); popup.setY(200);
        WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();
        
        final WebView smallBrowser = new WebView();
        final WebEngine smallWebEngine = smallBrowser.getEngine();

        Button closeBtn = new Button();
        closeBtn.setText("关闭");
        closeBtn.setPrefSize(100, 20);
        //Image imageDecline = new Image(getClass().getResourceAsStream("/page/img/error.png"));
        //closeBtn.setGraphic(new ImageView(imageDecline));
        //closeBtn.setStyle("-fx-base:#fff");
        closeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //stopper.stop();
                Platform.exit();
                System.exit(0);
            }
        });
        /*Button helpBtn = new Button();
         helpBtn.setText("");
         helpBtn.setPrefSize(100, 20);
         helpBtn.setOnAction(new EventHandler<ActionEvent>() {
         @Override
         public void handle(ActionEvent event) {
         webEngine.executeScript("change()");
         }
         });
         HBox hbox = new HBox();
         //hbox.setPadding(new javafx.geometry.Insets(15, 12, 15, 12));
         hbox.setSpacing(10);
         hbox.setStyle("-fx-background-color: #336699;-fx-padding: 15 12 15 12;");
         hbox.getChildren().addAll(closeBtn, helpBtn);*/

        //webEngine.load("http://docs.oracle.com/javase/index.html");


        /*StackPane stack = new StackPane();
         Rectangle helpIcon = new Rectangle(35.0, 25.0);
         helpIcon.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop[]{new Stop(0, Color.web("#4977A3")), new Stop(0.5, Color.web("#B0C6DA")), new Stop(1, Color.web("#9CB6CF")),}));
         helpIcon.setStroke(Color.web("#D0E6FA"));
         helpIcon.setArcHeight(3.5);
         helpIcon.setArcWidth(3.5);
         Text helpText = new Text("X");
         helpText.setFont(Font.font("Amble Cn", FontWeight.BOLD, 18));
         helpText.setFill(Color.WHITE);
         helpText.setStroke(Color.web("#7080A0"));
         stack.getChildren().addAll(helpIcon, helpText);
         stack.setAlignment(Pos.TOP_RIGHT); */

        //String page = getPageContent("test.html");

        // JavaScript interface object
        class JavaApp {

            public void open(String url) {
                System.err.println("here open:" + url);
                webEngine.loadContent(getPageContent(url));
            }
            public void alter(String title, String msg) {
                Stage dialog = new Stage();
                dialog.initStyle(StageStyle.UTILITY);
                Scene scene = new Scene(new Group(new Text(25, 25, msg)));
                dialog.setScene(scene);
                dialog.show();
            }
            
            public void popwin(String url) {
                
                smallWebEngine.loadContent(getPageContent(url));
                //JDialog dialog = new JDialog(frame, "观看视频");
                JDialog dialog = new JDialog(); 
                JFXPanel smallPanel = new JFXPanel();
                BorderPane root = new BorderPane();
                root.setCenter(smallBrowser);
                Scene scene = new Scene(root);
                smallPanel.setScene(scene);
                dialog.add(smallPanel);
                
                Toolkit kit = Toolkit.getDefaultToolkit();
                Dimension screenSize = kit.getScreenSize();
                int width = (int) screenSize.getWidth();
                int height = (int) screenSize.getHeight();
                dialog.setSize(800, 600);
                int w = dialog.getWidth();
                int h = dialog.getHeight();
                dialog.setLocation( (width - w) / 2, (height - h) / 2);
                dialog.setAlwaysOnTop(true);
                
                working = false;
                dialog.setVisible(true); 
                dialog.addWindowListener(new WindowAdapter() 
                {
                  public void windowClosing(WindowEvent e)
                  {
                       System.err.println("windowClosing");
                        working=true;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while(working) {
                                    frame.toFront();
                                }
                            }
                        }).start();
                  }
                });
                //popup.show(fxContainer);
            }

            public void write(String content) {
            }

            public void download(String remoteUrl) throws IOException {
                /*FileChooser chooser = new FileChooser();
                File file = chooser.showSaveDialog(stage);
                if (file != null)
                {
                    FileDownloadTask fileDownloadTask = new FileDownloadTask(remoteUrl, file);
                    new Thread(fileDownloadTask).start();
                }*/
                File localFile = new File("d:\\temp\\temp.mp4");
                FxFileDownload fileDownloadTask = new FxFileDownload(remoteUrl, localFile);
                new Thread(fileDownloadTask).start();
            }
            
            public String saveDB(String sql) throws ClassNotFoundException {
                String result = "";
                DBManager.initDB();
                //UlianUsersDao personDao = DaoFactory.getInstance().getPersonDao();
                //Long id = personDao.insert("insert into Person (userName, passwd, updateAt) values(?, ?, ?)", "user1", "passwd1", ""+System.currentTimeMillis());
                //LOG.debug("saveDB id:" + id);
                //Person person = personDao.get(id);

                //result = JSON.toJSONString(person);
                
                return result;
            }
            
            public String saveDB2(String sql) throws ClassNotFoundException {
                String result = "";
                //DBManager.initDB();
//                UlianUsersDao personDao = DaoFactory.getInstance().getPersonDao();
//                Long id = personDao.insert("insert into Person (userName, passwd) values(?, ?)", "呵呵", "呵呵呵");
//                LOG.debug("saveDB2 id:" + id);
//                Person person = personDao.get(id);
//
//                result = JSON.toJSONString(person);
                
                return result;
            }
            
            public String search(String sql) throws ClassNotFoundException {
                String result = "";
                //UlianUsersDao personDao = DaoFactory.getInstance().getPersonDao();
                /*DBUtilsTransactionHelper.startTransacion();
                
                try {
                    Connection conn = DBUtilsTransactionHelper.getConnetion();
                    LOG.debug(personDao.get(32L).getUserName());
                    Long id = personDao.insert(conn, "insert into Person (userName, passwd, realName) values(?, ?, ?)", "33", "123456", "开口道");
                    LOG.debug(id);
                    Person person = personDao.get(conn, id);
                    LOG.debug(person.getRealName());
                    long count = personDao.count(conn, "select count(*) from Person");
                    LOG.debug(count);
                    
                    DBUtilsTransactionHelper.commit();
                } catch(Exception e) {
                    LOG.error("exception roll back");
                    DBUtilsTransactionHelper.rollback();
                } finally {
                    DBUtilsTransactionHelper.close();
                }*/
//                Person person = personDao.get(1l);
//                result = JSON.toJSONString(person);
                LOG.debug(result);
                return result;
            }
        }

        // process page loading
        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> ov,
                    Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject win =
                            (JSObject) webEngine.executeScript("window");
                    win.setMember("app", new JavaApp());
                }
            }
        });

        //webEngine.loadContent(page);
        webEngine.load("http://google.com.hk");

        
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
