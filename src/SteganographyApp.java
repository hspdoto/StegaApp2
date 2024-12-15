import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Основной класс приложения для стеганографии, который позволяет встраивать текстовую информацию в изображения
 * и извлекать её с использованием метода наименьших значащих битов (LSB).
 */
public class SteganographyApp extends Application {
    private static final Logger logger = LogManager.getLogger(SteganographyApp.class);
    private final ImageView originalImageView = new ImageView();
    private final ImageView modifiedImageView = new ImageView();
    private File originalImageFile;

    /**
     * Точка входа в приложение.
     *
     * @param args Аргументы командной строки.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Метод инициализации приложения.
     *
     * @param primaryStage Основное окно приложения.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting Steganography Application...");
            primaryStage.setTitle("Steganography - LSB Method");
            TextField textField = this.createTextField();
            HBox buttonsBox = this.createButtonsBox(primaryStage, textField);
            HBox imagesBox = new HBox(10.0, this.originalImageView, this.modifiedImageView);
            imagesBox.setPadding(new Insets(10.0));
            VBox mainLayout = new VBox(10.0, textField, buttonsBox, imagesBox);
            Scene scene = new Scene(mainLayout, 800.0, 600.0);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            handleException("Error during application start", e);
        }
    }

    /**
     * Создает текстовое поле для ввода текста, который будет встроен в изображение.
     *
     * @return Текстовое поле.
     */
    private TextField createTextField() {
        TextField textField = new TextField();
        textField.setPromptText("Enter text to embed");
        return textField;
    }

    /**
     * Создает панель с кнопками для загрузки изображения, встраивания текста и извлечения текста.
     *
     * @param primaryStage Основное окно приложения.
     * @param textField    Текстовое поле для ввода текста.
     * @return Панель с кнопками.
     */
    private HBox createButtonsBox(Stage primaryStage, TextField textField) {
        Button loadImageButton = new Button("Load Image");
        loadImageButton.setOnAction((e) -> loadImage(primaryStage));
        Button embedTextButton = new Button("Embed Text");
        embedTextButton.setOnAction((e) -> handleEmbedText(textField.getText()));
        Button extractTextButton = new Button("Extract Text");
        extractTextButton.setOnAction((e) -> handleExtractText());
        return new HBox(10.0, loadImageButton, embedTextButton, extractTextButton);
    }

    /**
     * Обрабатывает исключения и отображает сообщение об ошибке.
     *
     * @param message Сообщение об ошибке.
     * @param e       Исключение.
     */
    private void handleException(String message, Exception e) {
        logger.error(message + ": " + e.getMessage(), e);
        showAlert("Error", message + ": " + e.getMessage());
    }

    /**
     * Отображает уведомление пользователю.
     *
     * @param title   Заголовок уведомления.
     * @param message Сообщение уведомления.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Загружает изображение и отображает его в интерфейсе.
     *
     * @param stage Основное окно приложения.
     */
    private void loadImage(Stage stage) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("BMP Images", "*.bmp"));
            this.originalImageFile = fileChooser.showOpenDialog(stage);
            if (this.originalImageFile != null) {
                Image image = new Image(this.originalImageFile.toURI().toString());
                this.originalImageView.setImage(image);
                logger.info("Image loaded successfully: " + this.originalImageFile.getAbsolutePath());
            } else {
                logger.warn("Image loading cancelled.");
            }
        } catch (Exception e) {
            handleException("Failed to load image", e);
        }
    }

    /**
     * Обрабатывает встраивание текста в изображение.
     *
     * @param text Текст для встраивания.
     */
    private void handleEmbedText(String text) {
        if (this.originalImageFile != null && !text.isEmpty()) {
            try {
                BufferedImage image = loadBufferedImage(this.originalImageFile);
                BufferedImage modifiedImage = ImageProcessor.embedText(image, text);
                saveAndDisplayImage(modifiedImage);
            } catch (IOException e) {
                handleException("Failed to embed text", e);
            } catch (Exception e) {
                handleException("Unexpected error during text embedding", e);
            }
        } else {
            showAlert("Error", "Please load an image and enter text.");
            logger.error("Failed to embed text: image or text missing.");
        }
    }

    /**
     * Обрабатывает извлечение текста из изображения.
     */
    private void handleExtractText() {
        if (this.originalImageFile == null) {
            showAlert("Error", "Please load an image first.");
            logger.error("Failed to extract text: no image loaded.");
        } else {
            try {
                BufferedImage image = loadBufferedImage(this.originalImageFile);
                String extractedText = ImageProcessor.extractText(image);
                this.showAlert("Extracted Text", extractedText);
                logger.info("Text extracted successfully: " + extractedText);
            } catch (IOException e) {
                handleException("Failed to extract text", e);
            } catch (Exception e) {
                handleException("Unexpected error during text extraction", e);
            }
        }
    }

    /**
     * Загружает изображение в формате BufferedImage.
     *
     * @param file Файл изображения.
     * @return Загруженное изображение.
     * @throws IOException Если произошла ошибка при чтении файла.
     */
    private BufferedImage loadBufferedImage(File file) throws IOException {
        return ImageIO.read(file);
    }

    /**
     * Сохраняет измененное изображение и отображает его в интерфейсе.
     *
     * @param image Измененное изображение.
     * @throws IOException Если произошла ошибка при записи файла.
     */
    private void saveAndDisplayImage(BufferedImage image) throws IOException {
        File output = new File("embedded_image.bmp");
        ImageIO.write(image, "bmp", output);
        Image embeddedImage = new Image(output.toURI().toString());
        this.modifiedImageView.setImage(embeddedImage);
        logger.info("Image saved at " + output.getAbsolutePath());
    }
}