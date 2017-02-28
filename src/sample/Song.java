package sample;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.net.URI;

public class Song {
    private final ObjectProperty<URI> path;
    private final StringProperty title;

    public Song(URI path,String title) {
        //System.out.println(title);
        this.title = new SimpleStringProperty(title);
        this.path = new SimpleObjectProperty<>(path);
        //System.out.println(this.path);
    }

    public String getTitle(){return title.get();}
    public URI getPath(){return path.get();}
}
