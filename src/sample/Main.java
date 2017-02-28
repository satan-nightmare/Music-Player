package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    public Stage primaryStage;
    private AnchorPane root;
    public boolean isPlaying;
    public MediaPlayer mp;
    private Media media;
    private Controller controller;
    public boolean isLoaded;
    public ObservableList<Song> playlist;
    private ObservableMap<String,Object> meta;

    @Override
    public void start(Stage primaryStage) throws Exception{
        isPlaying=false;
        playlist = FXCollections.observableArrayList();
        isLoaded=false;
        this.primaryStage=primaryStage;
        this.primaryStage.setTitle("Music Player");
        this.primaryStage.getIcons().add(new Image("file:resources/images/icon.png"));
        this.primaryStage.setResizable(false);
        initRoot();
    }

    public void initRoot() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("root.fxml"));
        root = (AnchorPane)loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        controller = loader.getController();
        controller.setMain(this);
        primaryStage.show();
    }

    public void playNew(File file){
        new Thread(()-> {
            if (isPlaying)
                mp.stop();
            isLoaded = true;
            media = new Media(file.toURI().toString());
            mp = new MediaPlayer(media);
            isPlaying = true;
            mp.setOnReady(() -> {
                meta = media.getMetadata();
                controller.setOverview((String) meta.get("title"), (String) meta.get("artist"), (Image) meta.get("image"), media.getDuration(), mp);
                mp.play();
                //new Thread(()->{mp.play();}).start();
            });
        }).start();
    }

    public void play(){
        mp.play();
        isPlaying=true;
    }

    public void pause(){
        mp.pause();
        isPlaying=false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
