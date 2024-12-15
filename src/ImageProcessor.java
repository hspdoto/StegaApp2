import java.awt.image.BufferedImage;

/**
 * Класс для обработки изображений с использованием метода наименьших значащих битов (LSB).
 */
public class ImageProcessor {

    /**
     * Встраивает текстовую информацию в изображение с использованием метода LSB.
     *
     * @param image Исходное изображение.
     * @param text  Текст для встраивания.
     * @return Измененное изображение с встроенным текстом.
     */
    public static BufferedImage embedText(BufferedImage image, String text) {
        String binaryText = toBinary(text) + "00000000"; // Добавляем 8 битов нулей для конца текста
        return processImageWithText(image, binaryText);
    }

    /**
     * Извлекает текстовую информацию из изображения с использованием метода LSB.
     *
     * @param image Исходное изображение.
     * @return Извлеченный текст.
     */
    public static String extractText(BufferedImage image) {
        StringBuilder binaryText = new StringBuilder();
        processImageForText(image, binaryText);
        return fromBinary(binaryText.toString());
    }

    /**
     * Преобразует текст в бинарную строку.
     *
     * @param text Текст для преобразования.
     * @return Бинарная строка.
     */
    private static String toBinary(String text) {
        StringBuilder binary = new StringBuilder();
        for (char c : text.toCharArray()) {
            binary.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binary.toString();
    }

    /**
     * Преобразует бинарную строку в текст.
     *
     * @param binary Бинарная строка.
     * @return Текст.
     */
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

    /**
     * Обрабатывает изображение для встраивания текста.
     *
     * @param image      Исходное изображение.
     * @param binaryText Бинарная строка текста.
     * @return Измененное изображение.
     */
    private static BufferedImage processImageWithText(BufferedImage image, String binaryText) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        int binaryIndex = 0;

        outerLoop:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);

                if (binaryIndex < binaryText.length()) {
                    int lsb = Character.getNumericValue(binaryText.charAt(binaryIndex));

                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;

                    r = (r & 0xFE) | lsb;

                    int modifiedRgb = (r << 16) | (g << 8) | b;
                    result.setRGB(x, y, modifiedRgb);

                    binaryIndex++;
                } else {
                    result.setRGB(x, y, rgb);
                }

                if (binaryIndex >= binaryText.length()) {
                    break outerLoop;
                }
            }
        }

        return result;
    }

    /**
     * Обрабатывает изображение для извлечения текста.
     *
     * @param image      Исходное изображение.
     * @param binaryText Строка для хранения бинарного текста.
     */
    private static void processImageForText(BufferedImage image, StringBuilder binaryText) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int lsb = (rgb >> 16) & 1;
                binaryText.append(lsb);
            }
        }
    }
}