
package cs1302.gallery;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.geometry.Pos;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.control.*;
import java.net.*;
import java.io.*;
import com.google.gson.*;
import java.util.Random;
import javafx.event.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalTime;
import javafx.scene.control.Alert;

/**
 * GalleryApp class contains all methods necessary to 
 * display a GUI that interfaces the iTunes search
 * library to display album art of the specified 
 * search term 
 *
 * @author Ty Burns   tab26715@uga.edu
 */

public class GalleryApp extends Application {
    

    ProgressBar progBar = new ProgressBar();
    String[] imageURL = new String[50];
    GridPane centerPane = new GridPane();
    boolean play = true;
    int tog = 0;

    @Override
    public void start(Stage stage) {
	BorderPane outerPane = new BorderPane();
	VBox topPane = new VBox();
 

        Scene scene = new Scene(outerPane);
	outerPane.setPrefSize(625,595);
        stage.setTitle("Gallery");
        stage.setScene(scene);
	stage.sizeToScene();
	outerPane.setStyle("-fx-background-color: grey");
	topPane.setStyle("-fx-background-color: white");
	
	Menu fileMenu = new Menu("_File");
	MenuItem exitItem = new MenuItem("E_xit");
	exitItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));
	exitItem.setOnAction(event -> {
		Platform.exit();
		System.exit(0);
	    });

	fileMenu.getItems().add(exitItem);
	
	HBox progressBarBox = new HBox(20);
	progressBarBox.setAlignment(Pos.CENTER_LEFT);
	progressBarBox.setStyle("-fx-background-color: white");
	Text x = new Text("Images provided courtesy of iTunes");
	progBar.setProgress(1.00F);
	progressBarBox.getChildren().add(progBar);
	progressBarBox.getChildren().add(x);
	outerPane.setBottom(progressBarBox);

	MenuBar menuBar = new MenuBar();
	menuBar.getMenus().addAll(fileMenu);
	topPane.getChildren().add(menuBar);
	outerPane.setTop(topPane);

	search("metal"); //displays default images
	displayImages();

	Runnable r1 = () -> { //creates a runnable object that executes random()
	    random();
	};
   	EventHandler<ActionEvent> handler1 = event ->  { //creates a thread that executes runLater(r1)
		Thread t1 = new Thread(() -> {
			Platform.runLater(r1);
		    });
		t1.setDaemon(true);
		t1.start();
       	};
	KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler1);
	Timeline timeline = new Timeline();
	timeline.setCycleCount(Timeline.INDEFINITE);
	timeline.getKeyFrames().add(keyFrame);
	timeline.play();

	HBox searchBar = new HBox(20);
	searchBar.setPadding(new Insets(10));
	searchBar.setAlignment(Pos.CENTER_LEFT);
	
	Button pauseButton = new Button("Pause"); //creates the pause button and dictates it's behavior
       	pauseButton.setOnAction(event -> {
		if(tog==0){
		    timeline.pause();
		    pauseButton.setText("Play");
		    tog=1;}
		else if(tog==1){
		    timeline.play();
		    pauseButton.setText("Pause");
		    tog=0;}		
	    });

	searchBar.getChildren().add(pauseButton);

	Text tq = new Text("Search Query:");
	searchBar.getChildren().add(tq);

	TextField searchBox = new TextField();
	Button update = new Button("Update Images");
	Runnable r = () -> { //creates a runnable object
	    search(searchBox.getText());
	    displayImages();
	};
	EventHandler<ActionEvent> handler = event -> { //creates a new thread used to execute runnable r
		Thread t = new Thread(() -> {
			Platform.runLater(r);
		    });
		t.setDaemon(true);
		t.start();
       	};
    update.setOnAction(handler);
	
	searchBar.getChildren().add(searchBox);
	searchBar.getChildren().add(update);
	topPane.getChildren().add(searchBar);
	outerPane.setCenter(centerPane);	

	stage.show();
    } // start
/**
 * Entry point for the application. Attempts to lauch
 * the application and displays an error message if 
 * it encounters an exception.
 *
 */
    public static void main(String[] args) {
	try {
	    Application.launch(args);
	} catch (UnsupportedOperationException e) {
	    System.out.println(e);
	    System.err.println("If this is a DISPLAY problem, then your X server connection");
	    System.err.println("has likely timed out. This can generally be fixed by logging");
	    System.err.println("out and logging back in.");
	    System.exit(1);
	} // try
    } // main
/**
 * Takes the parameterized search term, URL encodes it, and 
 * makes an iTunes friendly search query that is passed to 
 * a inputStreamReader. Parses the results into an array of 
 * URLs.
 *
 * @param searchTerm the keyword to search for
 */
       public void search(String searchTerm){
	   progBar.setProgress(.00F);
	   /*   Task<Void> task = new Task<Void>(){ //progress bar task attempt
                @Override
                public Void call(){
		
		    for(int i=0;i<=20;i++){
			Thread.sleep(100);
			updateProgress(i,20);
		    }//for
                }
		};*/
	       /*
		 encodes the given search term into a iTunes
		 friendly search query URL
	       */
	   try{
	   String encodedString = URLEncoder.encode(searchTerm,"UTF-8");
	   URL iTunesURL = new URL("https://itunes.apple.com/search?term="+encodedString+"&country=US");
	   InputStreamReader reader = new InputStreamReader(iTunesURL.openStream());

	   /*
	     parses the JSON response from iTunes.
	     Code for this was pulled from project FAQ
	   */
	   JsonParser jp = new JsonParser();
	   JsonElement je = jp.parse(reader);
	   JsonObject root = je.getAsJsonObject();                      // root of response
	   JsonArray results = root.getAsJsonArray("results");          // "results" array
	   int numResults = results.size();                             // "results" array size
	   if(numResults < 20){
	       System.out.println("Not enough results returned");
	       // Alert alert = new Alert(AlertType.ERROR, "Not enough results returned");
	       //alert.show();
	   }//if
	   else{
	       for(int i=0;i<numResults;i++){
	       JsonObject result = results.get(i).getAsJsonObject();    // object i in array
	       JsonElement artworkUrl100 = result.get("artworkUrl100"); // artworkUrl100 member
	       if (artworkUrl100 != null) {                             // member might not exist
		   String artUrl = artworkUrl100.getAsString();        // get member as string
		   imageURL[i]= artUrl;
	       } // if
	       }//for
	   }//else
	   }//try
	   catch(IOException e){
	       System.out.println("Error: IOException");
	   }//catch
	   /* catch(MalformedURLException e){
	       System.out.println("Error: Malformed URL");
	   }//catch
	   catch(UnsupportedEncodingException e){
	       System.out.println("Error: Unsupported Encoding");
	       }//catch*/
	   
/*  Thread th = new Thread(task); //progressbar thread attempt
            th.setDaemon(true);
            th.start();*/
      }//search

/**
 * Displays the imageViews created by the first 20 URLs stored in 
 * imageURL[] in a 5*4 grid.
 */
    public void displayImages(){
	centerPane.getChildren().clear();
	int pic = 0;
	for(int i=0; i<=3; i++){
	    for(int n=0; n<=4; n++){
		if(imageURL[i].contains("movie") || imageURL[i].contains("video")){
		    pic++;}
		ImageView iv = new ImageView(imageURL[pic]);
		iv.setFitWidth(125);
		iv.setPreserveRatio(true);
		centerPane.add(iv,n,i);
			       pic++;
	    }//for
	}//for
    }//displayImages

/**
 * Randomly swaps one of the images being displayed with one that
 * was also returned by the iTunes search 
 */
    public void random(){
	Random rand = new Random();
	String[] copy = new String[50];
	String temp = new String();
	for(int i=0;i<=49;i++){
	    if(imageURL[i].contains("movie") || imageURL[i].contains("video")){
		    i++;}
		else
		    copy[i]=imageURL[i];
	}//for
	    int n = rand.nextInt(49);
	    int r = rand.nextInt(19);
	    if(copy[n]!=null){
		temp = imageURL[r];
		imageURL[r]=copy[n];
		copy[n] = temp;
		displayImages();
	    }//if
	//while
    }//random
} // GalleryApp
