package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Controller {

    private Main main;
    @FXML
    private Slider timeSlider;
    @FXML
    private Label totalTime;
    @FXML
    private Label currentTime;
    @FXML
    private ImageView playButton;
    @FXML
    private ImageView forward;
    @FXML
    private ImageView backward;
    @FXML
    private ListView<Song> list;
    @FXML
    private ImageView albumArt;
    @FXML
    private TextArea lyrics;
    @FXML
    private Label titleLabel;
    @FXML
    private ImageView shuffle;
    @FXML
    private ImageView repeat;

    private Image play;
    private Image pause;
    private Image forw;
    private Image back;
    private Image defaultImage;
    private MediaPlayer mp;
    private Duration duration;
    private Double oldValueDouble,newValueDouble;
    private int currentIndex;
    private boolean isShuffle;
    private boolean isRepeat;

    @FXML
    public void initialize(){
        play = new Image("file:resources/images/play.png");
        pause = new Image("file:resources/images/pause.png");
        forw = new Image("file:resources/images/forward.png");
        back = new Image("file:resources/images/backward.png");
        defaultImage = new Image("file:resources/images/album.jpg",310,310,false,false);
        playButton.setImage(play);
        forward.setImage(forw);
        backward.setImage(back);
        shuffle.setImage(new Image("file:resources/images/shuffle.png"));
        repeat.setImage(new Image("file:resources/images/repeat.png"));
        //shuffle.setLayoutX(335);
        //shuffle.setLayoutY(443);
        list.setCellFactory(param -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getTitle() == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });

        isShuffle=false;
        isRepeat=false;
        lyrics.getStyleClass().add("outline");
    }

    public void setMain(Main main){
        this.main = main;
        list.setItems(main.playlist);
    }

    @FXML
    private void handleShuffle(){
        if(isShuffle)
            shuffle.setOpacity(0.4);
        else
            shuffle.setOpacity(1.0);
        isShuffle=!isShuffle;
    }

    @FXML
    private void handleRepeat(){
        if(isRepeat)
            repeat.setOpacity(0.4);
        else
            repeat.setOpacity(1.0);
        isRepeat=!isRepeat;
    }

    @FXML
    private void handleNext(){
        if(isRepeat){
            //main.mp.
            main.mp.seek(new Duration(0));
        }else if(isShuffle){
            int randomNum = ThreadLocalRandom.current().nextInt(0, main.playlist.size());
            list.getSelectionModel().select(randomNum);
            handleNewPlay();
        }else {
            if (main.playlist.size() - 1 > currentIndex) {
                list.getSelectionModel().select(currentIndex + 1);
                handleNewPlay();
            }
        }
    }

    @FXML
    private void handlePrevious(){
        if(main.isPlaying && main.mp.currentTimeProperty().getValue().toMillis()>5000){
            main.mp.seek(new Duration(0));
        }else {
            if (currentIndex>0) {
                list.getSelectionModel().select(currentIndex - 1);
                handleNewPlay();
            }
        }
    }

    @FXML
    private void handleAdd(){
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter ext = new FileChooser.ExtensionFilter(
                "Music files (*.mp3)","*.mp3");
        fc.getExtensionFilters().add(ext);
        List<File> files = fc.showOpenMultipleDialog(main.primaryStage);
        if(files!=null) {
            for(File file:files) {
                Media media = new Media(file.toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setOnReady(() -> {
                    main.playlist.add(new Song(file.toURI(), (String) media.getMetadata().get("title")));
                });
            }
        }
    }

    @FXML
    private void handleRemove(){
        int selectedSong = list.getSelectionModel().getSelectedIndex();
        if(selectedSong != -1)
            list.getItems().remove(selectedSong);
    }

    @FXML
    private void handleNewPlay(){
        int selectedSong = list.getSelectionModel().getSelectedIndex();
        if(selectedSong != -1) {
            main.playNew(new File(list.getItems().get(selectedSong).getPath()));
            playButton.setImage(pause);
            currentIndex=selectedSong;
        }
    }

    @FXML
    private void handlePlay(){
        if(main.isLoaded){
            if(main.isPlaying){
                main.pause();
                playButton.setImage(play);
            }else{
                main.play();
                playButton.setImage(pause);
            }
        }else
            handleNewPlay();
    }

    public void setOverview(String title,String artist,Image albumArt,Duration duration,MediaPlayer mp){
        titleLabel.setText(title+" - "+artist);
        lyrics.setText(getLyrics(title+" "+artist));
        lyrics.setText("Searching lyrics...");
        Thread t = new Thread(() -> lyrics.setText(getLyrics(title+" "+artist)));
        t.start();
        if(albumArt==null)
            this.albumArt.setImage(defaultImage);
        else
            this.albumArt.setImage(albumArt);
        this.duration=duration;
        this.mp = mp;
        setTime(totalTime,duration);
        timeSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            oldValueDouble = oldValue.doubleValue();
            newValueDouble = newValue.doubleValue();
            if(Math.abs(oldValueDouble-newValueDouble) > 0.1)
                mp.seek(new Duration(newValueDouble*duration.toMillis()/100.0));
        }));
        mp.currentTimeProperty().addListener((observable, oldValue, newValue) ->  {
            if(newValue.subtract(oldValue).greaterThan(new Duration(50)))
                updateValues();
        });
        mp.setOnEndOfMedia(()->handleNext());
    }

    private void setTime(Label label,Duration duration){
        int time = (int)Math.round(duration.toSeconds());
        String min=Integer.toString(time/60);
        String sec=Integer.toString(time%60);
        if(sec.length()==1)
            sec="0"+sec;
        label.setText(min+":"+sec);
    }

    private String getLyrics(String name){
        String url = "http://search.azlyrics.com/search.php?q=" + name.replace(" ", "+");
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            return "Cannot connect to Internet";
        }
        Elements e = doc.select("td > a");
        if (e.isEmpty())
            return "Lyrics not found";
        else{
            try {
                doc = Jsoup.connect(e.first().attr("href")).get();
            } catch (IOException e1) {
                return "Cannot connect to Internet";
            }
            e = doc.select("div.col-lg-8 > div");
            Element p;
            int n = 0;
            String str="";
            for (Element ele : e) {
                if (ele.hasText()) {
                    if (ele.text().length() > n) {
                        str = ele.html().replaceAll("<br>","");
                        //str.replaceAll("<!--.*-->", "");
                        str = str.replaceAll("(?s)<!--.*?-->", "");
                        n = ele.text().length();
                    }
                }
            }
            return str;
        }
    }

    void updateValues(){
        Platform.runLater(() -> {
                timeSlider.setValue(mp.getCurrentTime().divide(duration).toMillis() * 100.0);
                setTime(currentTime,mp.getCurrentTime());
        });
    }

}
