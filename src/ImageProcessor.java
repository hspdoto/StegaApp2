import java.awt.image.BufferedImage;

public class ImageProcessor {

    // Embed text into an image using LSB method
    public static BufferedImage embedText(BufferedImage image, String text) {
        // Преобразуем текст в бинарную строку и добавим маркер конца
        String binaryText = toBinary(text) + "00000000"; // Добавляем 8 битов нулей для конца текста
        return processImageWithText(image, binaryText);
    }

    // Extract text from an image using LSB method
    public static String extractText(BufferedImage image) {
        StringBuilder binaryText = new StringBuilder();
        processImageForText(image, binaryText);
        return fromBinary(binaryText.toString());
    }

    // Convert text to binary
    private static String toBinary(String text) {
        StringBuilder binary = new StringBuilder();
        for (char c : text.toCharArray()) {
            binary.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binary.toString();
    }

    // Convert binary to text
    private static String fromBinary(String binary) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 8) {
            String byteStr = binary.substring(i, Math.min(i + 8, binary.length()));
            if (byteStr.equals("00000000")) {
                break; // Конец текста
            }
            char c = (char) Integer.parseInt(byteStr, 2);
            text.append(c);
        }
        return text.toString();
    }

    // Process image for embedding or extracting text based on binaryText
    private static BufferedImage processImageWithText(BufferedImage image, String binaryText) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        int binaryIndex = 0;

        outerLoop:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);

                // Если есть еще биты для вставки
                if (binaryIndex < binaryText.length()) {
                    int lsb = Character.getNumericValue(binaryText.charAt(binaryIndex)); // Получаем текущий бит

                    int r = (rgb >> 16) & 0xFF;  // Извлекаем красный канал
                    int g = (rgb >> 8) & 0xFF;   // Извлекаем зеленый канал
                    int b = rgb & 0xFF;          // Извлекаем синий канал

                    // Изменяем только младший бит красного канала
                    r = (r & 0xFE) | lsb;  // Устанавливаем младший бит красного канала

                    // Восстанавливаем RGB
                    int modifiedRgb = (r << 16) | (g << 8) | b;
                    result.setRGB(x, y, modifiedRgb);

                    binaryIndex++;
                } else {
                    result.setRGB(x, y, rgb);  // Если больше нет бит, копируем пиксель как есть
                }

                if (binaryIndex >= binaryText.length()) {
                    break outerLoop;  // Прерываем цикл, если все биты вставлены
                }
            }
        }

        return result;
    }

    // Process image for extracting text by appending least significant bits to binaryText
    private static void processImageForText(BufferedImage image, StringBuilder binaryText) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int lsb = (rgb >> 16) & 1; // Извлекаем младший бит красного канала
                binaryText.append(lsb);
            }
        }
    }
}
