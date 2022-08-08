 package com.internshala.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	private static final int COLUMNS = 7;
	private static final int ROWS = 6;
	private static final int CIRCLE_DIAMETER = 70;
	private static final String discColor1 = "#24303E";
	private static final String discColor2 = "#4CAA88";

	private static String PLAYER_ONE = "Player One";
	private static String PLAYER_TWO = "Player Two";

	private boolean isAllowedToInsert = true;
	private boolean isPlayerOneTurn = true;


	private Controller.Disc[][] insertedDiscsArray = new Controller.Disc[6][7]; //for structural changes

	@FXML
	public GridPane rootGridPane;

	@FXML
	public Pane insertedDiscPane;


	@FXML
	public Label playerNameLabel;

	public Controller(){

	}
	public void createPlayground(){

		Shape rectangleWithHoles = this.createGameStructuralGrid();
		this.rootGridPane.add(rectangleWithHoles,0,1);

		List<Rectangle> rectangleList = this.createClickableColumns();
		for (Rectangle rectangle: rectangleList) {
			this.rootGridPane.add(rectangle,0,1);
		}

	}

	private Shape createGameStructuralGrid(){

		Shape rectangleWithHoles = new Rectangle((COLUMNS+1)*CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);

		for (int row = 0; row < ROWS; row++) {

			for (int col = 0; col < COLUMNS; col++) {

				Circle circle = new Circle();
				circle.setRadius(CIRCLE_DIAMETER/2.0);
				circle.setCenterX(CIRCLE_DIAMETER/2.0);
				circle.setCenterY(CIRCLE_DIAMETER/2.0);
				circle.setSmooth(true);

				circle.setTranslateX(col*(CIRCLE_DIAMETER+5) + (CIRCLE_DIAMETER/4.0));
				circle.setTranslateY(row*(CIRCLE_DIAMETER+5) + (CIRCLE_DIAMETER/4.0));

				rectangleWithHoles = Shape.subtract( rectangleWithHoles , circle );
			}
		}

		rectangleWithHoles.setFill(Color.WHITE);
		return rectangleWithHoles;
	}

	private List<Rectangle> createClickableColumns(){
		List <Rectangle> rectangleList = new ArrayList<>();


		for (int col = 0; col < 7; ++col) {
			Rectangle rectangle = new Rectangle( CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col*(CIRCLE_DIAMETER+5) + CIRCLE_DIAMETER/4.0);

			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			final int column = col;
			rectangle.setOnMouseClicked(event -> {
				if (isAllowedToInsert) {
					isAllowedToInsert=false;
					insertDisc(new Disc(isPlayerOneTurn), column);
				}
			});

			rectangleList.add(rectangle);
		}



		return rectangleList;
	}

	private  void insertDisc(Disc disc,int column) {

		int row = ROWS -1 ;
		while(row >= 0){
			if(getDiscIfPresent(row,column)== null)
				break;

			row--;

		}

		if (row < 0) //if it is full we can not insert anymore disc
			return;


		insertedDiscsArray[row][column] = disc; //for structural changes
		insertedDiscPane.getChildren().add(disc);

		disc.setTranslateX(column*(CIRCLE_DIAMETER+5)  + CIRCLE_DIAMETER/4.0);

		int currentROw = row;

		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5),disc);
		translateTransition.setToY(row*(CIRCLE_DIAMETER+5) + (CIRCLE_DIAMETER/4.0));
		translateTransition.setOnFinished(event -> {


			isAllowedToInsert=true;
			if (gameEnded(currentROw,column)){
				gameOver();
				return;

			}


			isPlayerOneTurn = !isPlayerOneTurn;
			playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);

		});

	 	translateTransition.play();

	}

	private boolean gameEnded(int row ,int column){

		//Vertical points .
		List<Point2D> verticalPoints = IntStream.rangeClosed(row - 3,row + 3) //RANGE OF ROW VALUES
		                                      .mapToObj(r ->  new Point2D(r,column)) //0,3  1,3  2,3  3,3  4,3  5,3  6,3 --> POINT2D
		                                      .collect(Collectors.toList());

		//Horizontal points
		List<Point2D> horizontalPoints = IntStream.rangeClosed(column - 3,column + 3)
				.mapToObj(col ->  new Point2D(col,row))
				.collect(Collectors.toList());
		Point2D startPoint1 = new Point2D(-3,+3);
		List<Point2D> diagonalPoints = IntStream.rangeClosed(0,6)
				.mapToObj(i-> startPoint1.add(i,-i))
				.collect(Collectors.toList());

		Point2D startPoint2 = new Point2D(-3,-3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6)
				.mapToObj(i-> startPoint2.add(i,i))
				.collect(Collectors.toList());

		boolean isEnded = checkCombinations(verticalPoints)
				|| checkCombinations(horizontalPoints)
				||checkCombinations(diagonalPoints)
				||checkCombinations(diagonal2Points);


		return isEnded;

	}

	private boolean checkCombinations(List<Point2D> points) {

		int chain = 0;

		for (Point2D point: points) {

			int rowIndexForArray = (int) point.getX();
			int columnIndexForArray = (int) point.getY();

			Disc disc = getDiscIfPresent(rowIndexForArray,columnIndexForArray);
			if (disc != null && disc .isPlayerOneMove == isPlayerOneTurn){

				chain++;
				if (chain == 4){
					return true;
				}
				else{
					chain = 0;
				}

			}

		}
		return false;
	}

	private Disc getDiscIfPresent(int row, int column){  //To prevent array out of bound exception

		if (row >= ROWS || row < 0 || column >= COLUMNS || column < 0)
			return null;

		return insertedDiscsArray[row][column];

	}

	private void gameOver(){

		String winner = isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO;
		System.out.println("Winner is :" + winner);

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner Is :" + winner);
		alert.setContentText("Want To Play Again? ");

		ButtonType yesBtn = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No , Exit");
		alert.getButtonTypes().setAll(yesBtn,noBtn);

        Platform.runLater(()->{
	        Optional<ButtonType> btnclicked = alert.showAndWait();
	        if (btnclicked.isPresent()&& btnclicked.get() == yesBtn){
		        //start the game
		        resetGame();

	        }else{
		        Platform.exit();
		        System.exit(0);
	        }

        });

	}

	public void resetGame() {

		insertedDiscPane.getChildren().clear();
		for (int row = 0; row < insertedDiscsArray.length; row++) {

			for (int col = 0; col < insertedDiscsArray.length;col++){
				insertedDiscsArray[row][col] = null;
			}
		}
		isPlayerOneTurn= true;
		playerNameLabel.setText(PLAYER_ONE);

		createPlayground();
	}


	private static class Disc extends Circle{
		private final boolean isPlayerOneMove;
		public Disc(boolean isPlayerOneMove){

			this.isPlayerOneMove = isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER/2.0);
			setFill(isPlayerOneMove? Color.valueOf(discColor1) : Color.valueOf(discColor2));
			setCenterX(CIRCLE_DIAMETER/2.0);
			setCenterY(CIRCLE_DIAMETER/2.0);


		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}

