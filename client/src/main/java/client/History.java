package client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// Создали отдельный класс, который работает с сохранением истории

public class History {

    // Класс, позволяющий выполнять запись в файл
    private static PrintWriter out;

    // Метод, генерирующий имя файла с историей
    private static String getHistoryFilenameByLogin(String login) {
        return "history/history_" + login + ".txt";
    }

    // Метод, работающий с классом PrintWriter на запись истории в файл. Открываем один раз
    public static void start(String login) {
        try {
            out = new PrintWriter(new FileOutputStream(getHistoryFilenameByLogin(login), true), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // При выходе закрываем (один раз открыли, один раз закрыли)
    public static void stop() {
        if (out != null) {
            out.close();
        }
    }

    // Непосредственная запись
    public static void writeLine(String msg) {
        out.println(msg);
    }

    // Показ последних 100 строк истории
    public static String getLast100LinesOfHistory(String login) {
        // Проверяем существование файла
        if (!Files.exists(Paths.get(getHistoryFilenameByLogin(login)))) {
            return "";
        }
        // Собираем историю
        StringBuilder sb = new StringBuilder();
        try {
            List<String> historyLines = Files.readAllLines(Paths.get(getHistoryFilenameByLogin(login)));
            int startPosition = 0;
            if (historyLines.size() > 100) {
                startPosition = historyLines.size() - 100;
            }
            for (int i = startPosition; i < historyLines.size(); i++) {
                sb.append(historyLines.get(i)).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();

    }

}
