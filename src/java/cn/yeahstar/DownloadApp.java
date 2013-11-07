/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar;

import cn.yeahstar.net.FxFileDownload;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class DownloadApp extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    public void start(final Stage stage) throws Exception
    {
        HBox topBox = new HBox(4);
        final TextField textField = new TextField("http://media.beyondzeroemissions.org/ZCA2020_Stationary_Energy_Report_v1.pdf");
        HBox.setHgrow(textField, Priority.ALWAYS);
        topBox.getChildren().add(textField);
        Button downloadButton = new Button("Download");

        final ListView<FxFileDownload> downloadList = new ListView<FxFileDownload>();
        downloadList.setCellFactory(new Callback<ListView<FxFileDownload>, ListCell<FxFileDownload>>()
        {
            public ListCell<FxFileDownload> call(ListView<FxFileDownload> fileDownloadTaskListView)
            {
                final DownloadRow downloadRow = new DownloadRow();
                return new ListCell<FxFileDownload>()
                {
                    protected void updateItem(FxFileDownload fileDownloadTask, boolean empty)
                    {
                        super.updateItem(fileDownloadTask, empty);
                        if (!empty)
                        {
                            downloadRow.setFileDownloadTask(fileDownloadTask);
                            setGraphic(downloadRow);
                        }
                    }
                };
            }
        });

        downloadButton.setOnAction(new EventHandler<ActionEvent>()
        {
            public void handle(ActionEvent event)
            {
                FileChooser chooser = new FileChooser();
                File file = chooser.showSaveDialog(stage);
                if (file != null)
                {
                    FxFileDownload fileDownloadTask = new FxFileDownload(textField.getText(), file);
                    downloadList.getItems().add(fileDownloadTask);
                    new Thread(fileDownloadTask).start();
                }
            }
        });
        topBox.getChildren().add(downloadButton);

        VBox rootPane = new VBox(10);
        rootPane.getChildren().add(topBox);
        rootPane.getChildren().add(downloadList);

        Scene scene = new Scene(rootPane, 600, 200);
        scene.getStylesheets().add("styles.css");
        stage.setScene(scene);
        stage.setTitle("File Downloader");
        stage.show();
    }

    //-------------------------------------------------------------------------

    private class DownloadRow extends HBox
    {
        private FxFileDownload fileDownloadTask;
        private Hyperlink link;
        private ProgressBar progressBar;

        private DownloadRow()
        {
            setSpacing(10);

            link = new Hyperlink();
            link.setOnAction(new EventHandler<ActionEvent>()
            {
                public void handle(ActionEvent event)
                {
                    try
                    {
                        Desktop.getDesktop().open(fileDownloadTask.getLocalFile());
                    }
                    catch (IOException e)
                    {
                        // todo handle this by showing an error message
                        e.printStackTrace();
                    }
                }
            });
            getChildren().add(link);

            progressBar = new ProgressBar();
            getChildren().add(progressBar);
        }

        public void setFileDownloadTask(FxFileDownload fileDownloadTask)
        {
            this.fileDownloadTask = fileDownloadTask;
            link.setText(fileDownloadTask.getLocalFile().getName());
            link.disableProperty().bind(fileDownloadTask.stateProperty().isNotEqualTo(Worker.State.SUCCEEDED));
            progressBar.progressProperty().bind(fileDownloadTask.progressProperty());
            progressBar.visibleProperty().bind(fileDownloadTask.runningProperty());
        }
    }
}