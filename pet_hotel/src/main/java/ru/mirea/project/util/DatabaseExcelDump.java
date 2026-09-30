package ru.mirea.project.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class DatabaseExcelDump {
    private static final List<String> TABLES = List.of("owners", "pets", "enclosures", "bookings");
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter SQL_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DatabaseExcelDump() {
    }

    public static void main(String[] args) throws Exception {
        // Параметры подключения: сначала аргументы запуска, затем переменные среды, затем значения по умолчанию.
        String url = argumentOrEnvironment(args, 0, "DB_URL", "jdbc:postgresql://localhost:5437/pet_hotel");
        String user = argumentOrEnvironment(args, 1, "DB_USER", "postgres");
        String password = argumentOrEnvironment(args, 2, "DB_PASSWORD", "postgres");

        // Папку можно переопределить для подключаемого модуля; по умолчанию это db_snapshots.
        Path outputDirectory = Path.of(System.getProperty("db.snapshot.directory", "db_snapshots"))
                .toAbsolutePath().normalize();

        Files.createDirectories(outputDirectory);
        Path outputFile = outputDirectory.resolve("pet_hotel_snapshot_"
                + LocalDateTime.now().format(FILE_TIMESTAMP) + ".xlsx");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // Repeatable read гарантирует, что все листы отражают одно состояние базы.
            connection.setReadOnly(true);
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setAutoCommit(false);

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                for (String table : TABLES) {
                    exportTable(connection, workbook, table);
                }

                try (OutputStream output = Files.newOutputStream(outputFile)) {
                    workbook.write(output);
                }
            }
            connection.commit();
        } catch (Exception exception) {
            Files.deleteIfExists(outputFile);
            throw exception;
        }

        System.out.println("Excel snapshot created: " + outputFile.toAbsolutePath());
    }

    private static void exportTable(Connection connection, XSSFWorkbook workbook, String table) throws Exception {
        // Каждая таблица выгружается на отдельный лист; текстовые значения сохраняются в Unicode.
        Sheet sheet = workbook.createSheet(table);
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT * FROM " + table + " ORDER BY id")) {
            ResultSetMetaData metadata = result.getMetaData();
            int columnCount = metadata.getColumnCount();

            Row header = sheet.createRow(0);
            for (int column = 1; column <= columnCount; column++) {
                header.createCell(column - 1).setCellValue(metadata.getColumnLabel(column));
            }

            int rowNumber = 1;
            while (result.next()) {
                Row row = sheet.createRow(rowNumber++);
                for (int column = 1; column <= columnCount; column++) {
                    Cell cell = row.createCell(column - 1);
                    Object value = result.getObject(column);
                    if (value == null) {
                        continue;
                    }
                    if (value instanceof Number number) {
                        cell.setCellValue(number.doubleValue());
                    } else if (value instanceof java.sql.Timestamp timestamp) {
                        cell.setCellValue(timestamp.toLocalDateTime().format(SQL_TIMESTAMP));
                    } else if (value instanceof java.sql.Date date) {
                        cell.setCellValue(date.toLocalDate().toString());
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, rowNumber - 1),
                    0, columnCount - 1));
            for (int column = 0; column < columnCount; column++) {
                sheet.autoSizeColumn(column);
            }
        }
    }

    private static String argumentOrEnvironment(String[] args, int index, String variable, String fallback) {
        if (args.length > index && !args[index].isBlank()) {
            return args[index];
        }
        String value = System.getenv(variable);
        return value == null || value.isBlank() ? fallback : value;
    }
}
