/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar;

import cn.yeahstar.net.FxFileDownload;
import cn.yeahstar.util.StageKeyStopper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import netscape.javascript.JSObject;
import org.apache.commons.io.IOUtils;
 
public class WebViewSample extends Application {
 
    private Scene scene;
    private StageKeyStopper stopper;
    @Override
    public void start(Stage stage) {
        // create scene
        stage.setTitle("Web View");
        scene = new Scene(new Browser(stage), 750, 500, Color.web("#666970"));
        stage.setScene(scene);
        // apply CSS style
        scene.getStylesheets().add("webviewsample/BrowserToolbar.css");
        stopper = StageKeyStopper.create(stage);
        stage.setFullScreen(true);
        stage.setResizable(false);
        stage.centerOnScreen();
        // show stage
        stage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}
 
class Browser extends Region {
    private Stage stage;
    private HBox toolBar;
    private static String[] imageFiles = new String[]{
        "product.png",
        "blog.png",
        "documentation.png",
        "partners.png",
        "help.png"
    };
    private static String[] captions = new String[]{
        "Products",
        "Blogs",
        "Documentation",
        "Partners",
        "Help"
    };
    private static String[] urls = new String[]{
        "http://www.oracle.com/products/index.html",
        "http://blogs.oracle.com/",
        "http://docs.oracle.com/javase/index.html",
        "http://www.oracle.com/partners/index.html",
        WebViewSample.class.getResource("/page/help.html").toExternalForm()
    };
    final ImageView selectedImage = new ImageView();
    final Hyperlink[] hpls = new Hyperlink[captions.length];
    final Image[] images = new Image[imageFiles.length];
    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();
    final Button showPrevDoc = new Button("Toggle Previous Docs");
    final WebView smallView = new WebView();
    final Popup popup = new Popup(); 

    private boolean needDocumentationButton = false;
    
    final StackPane stack = new StackPane();

    public Browser() {
        //apply the styles
        getStyleClass().add("browser");
        
        popup.setX(300); popup.setY(200);
        Button closeBtn = new Button();
        //closeBtn.setText("关闭");
        //closeBtn.setPrefSize(20, 20);
        Image imageDecline = new Image(getClass().getResourceAsStream("/page/img/error.png"));
        closeBtn.setGraphic(new ImageView(imageDecline));
        closeBtn.setStyle("-fx-base:#fff");
        closeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popup.hide();
            }
        });
        BorderPane root = new BorderPane();
        //root.setTop(hbox);
        root.setRight(closeBtn);
        root.setCenter(smallView);
        popup.getContent().add(root);
 
        for (int i = 0; i < captions.length; i++) {
            // create hyperlinks
            Hyperlink hpl = hpls[i] = new Hyperlink(captions[i]);
            Image image = images[i] =
                    new Image(getClass().getResourceAsStream(imageFiles[i]));
            hpl.setGraphic(new ImageView(image));
            final String url = urls[i];
            final boolean addButton = (hpl.getText().equals("Documentation"));
 
            // process event 
            hpl.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    needDocumentationButton = addButton;
                    webEngine.load(url);
                }
            });
        }
 
       
        // create the toolbar
        toolBar = new HBox();
        toolBar.setAlignment(Pos.CENTER);
        toolBar.getStyleClass().add("browser-toolbar");
        toolBar.getChildren().addAll(hpls);
        toolBar.getChildren().add(createSpacer());
 
        //set action for the button
        showPrevDoc.setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) {
                webEngine.executeScript("toggleDisplay('PrevRel')");
            }           
        });
 
        smallView.setPrefSize(600, 450);
 
        //handle popup windows
        /*webEngine.setCreatePopupHandler(
            new Callback<PopupFeatures, WebEngine>() {
                @Override public WebEngine call(PopupFeatures config) {
                    smallView.setFontScale(0.8);
                    //if (!toolBar.getChildren().contains(smallView)) {
                    //    toolBar.getChildren().add(smallView);
                    //}
                    //stack.getChildren().add(smallView);
                    popup.show(stage);
                    return smallView.getEngine();
                }
             }
        );*/
 
        // process page loading
        webEngine.getLoadWorker().stateProperty().addListener(
            new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue<? extends State> ov,
                    State oldState, State newState) {
                    toolBar.getChildren().remove(showPrevDoc);    
                    if (newState == State.SUCCEEDED) {
                            JSObject win = 
                                (JSObject) webEngine.executeScript("window");
                            win.setMember("app", new JavaApp());
                            if (needDocumentationButton) {
                                toolBar.getChildren().add(showPrevDoc);
                            }
                        }
                    }
                }
        );
 
        // load the home page    
        String page = getPageContent("test.html");
        //webEngine.load("http://www.oracle.com/products/index.html");
        webEngine.loadContent(page);
 
        //add components
        getChildren().add(toolBar);
        //stack.getChildren().add(browser);
        getChildren().add(browser);
    }
    
    public Browser(Stage stage) {
        this();
        this.stage = stage;
    }
    
    private String getPageContent(String name) {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("page/" + name);
        String page = "";
        try {
            page = IOUtils.toString(in, "utf-8");
        } catch (IOException ex) {
            Logger.getLogger(JavaFXSwingApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        return page;
    }
 
    // JavaScript interface object
    public class JavaApp {
        public void alter(String title, String msg) {
                Stage dialog = new Stage();
                dialog.initStyle(StageStyle.UTILITY);
                Scene scene = new Scene(new Group(new Text(25, 25, msg)));
                dialog.setScene(scene);
                dialog.show();
            }
        public void exit() {
            Platform.exit();
        }
        public void download(String remoteUrl) throws IOException {
            FileChooser chooser = new FileChooser();
            chooser.setInitialDirectory(new File("D:\\temp"));
            chooser.setTitle("dd.mp4");
            File file = chooser.showSaveDialog(stage);
            if (file != null)
            {
                FxFileDownload fileDownloadTask = new FxFileDownload(remoteUrl, file);
                new Thread(fileDownloadTask).start();
                
                ProgressBar progressBar = new ProgressBar();
                progressBar.progressProperty().bind(fileDownloadTask.progressProperty());
                progressBar.visibleProperty().bind(fileDownloadTask.runningProperty()); // optionally hide the progress bar when not loading
                toolBar.getChildren().add(progressBar);
                
                fileDownloadTask.stateProperty().addListener(
                    new ChangeListener<State>() {
                        @Override
                        public void changed(ObservableValue<? extends State> ov,
                            State oldState, State newState) {
                                if (newState == State.SUCCEEDED) {
                                    webEngine.executeScript("downloadSuccess()");
                                }
                            }
                        }
                );
            }
            
            /*File localFile = new File("d:\\temp\\temp.mp4");
            FileDownloadTask fileDownloadTask = new FileDownloadTask(remoteUrl, localFile);
            new Thread(fileDownloadTask).start();*/
        }
    }
 
    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
 
    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double tbHeight = toolBar.prefHeight(w);
        layoutInArea(browser,0,0,w,h-tbHeight,0,HPos.CENTER, VPos.CENTER);
        layoutInArea(toolBar,0,h-tbHeight,w,tbHeight,0,HPos.CENTER,VPos.CENTER);
    }
 
    @Override
    protected double computePrefWidth(double height) {
        return 750;
    }
 
    @Override
    protected double computePrefHeight(double width) {
        return 600;
    }
}
