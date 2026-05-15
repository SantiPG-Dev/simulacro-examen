package com.examenes.service;

import com.examenes.model.Question;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ExcelReader {

    private static final String[] HEADERS = {"Pregunta", "OpcionA", "OpcionB", "OpcionC", "OpcionD", "Correcta"};

    public static List<Question> readQuestions(String filePath) throws IOException {
        List<Question> questions = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return questions;

        try (InputStream is = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(is)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                questions.addAll(readSheet(sheet, sheet.getSheetName()));
            }
        }
        return questions;
    }

    public static List<Question> readQuestions(String filePath, String subject) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) return new ArrayList<>();

        try (InputStream is = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet(subject);
            if (sheet == null) return new ArrayList<>();
            return readSheet(sheet, subject);
        }
    }

    private static List<Question> readSheet(Sheet sheet, String subject) {
        List<Question> questions = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String question = getCellValue(row.getCell(0));
            String optA = getCellValue(row.getCell(1));
            String optB = getCellValue(row.getCell(2));
            String optC = getCellValue(row.getCell(3));
            String optD = getCellValue(row.getCell(4));
            String correctStr = getCellValue(row.getCell(5));

            if (question.isEmpty() || optA.isEmpty()) continue;

            List<String> options = new ArrayList<>();
            options.add(optA);
            options.add(optB);
            options.add(optC);
            options.add(optD);

            int correctIndex = switch (correctStr.toUpperCase()) {
                case "A" -> 0;
                case "B" -> 1;
                case "C" -> 2;
                case "D" -> 3;
                default -> -1;
            };

            if (correctIndex < 0) continue;

            questions.add(new Question(subject, question, options, correctIndex));
        }
        return questions;
    }

    public static List<String> getSubjects(String filePath) throws IOException {
        List<String> subjects = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return subjects;

        try (InputStream is = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(is)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                subjects.add(workbook.getSheetAt(i).getSheetName());
            }
        }
        return subjects;
    }

    public static Map<String, Integer> getSubjectQuestionCounts(String filePath) throws IOException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        File file = new File(filePath);
        if (!file.exists()) return counts;

        try (InputStream is = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(is)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                int count = 0;
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row != null) {
                        String q = getCellValue(row.getCell(0));
                        if (!q.isEmpty()) count++;
                    }
                }
                counts.put(sheet.getSheetName(), count);
            }
        }
        return counts;
    }

    public static void appendQuestions(String filePath, String subject, List<Question> newQuestions) throws IOException {
        File file = new File(filePath);
        Workbook workbook;
        if (file.exists()) {
            try (InputStream is = new FileInputStream(filePath)) {
                workbook = new XSSFWorkbook(is);
            }
        } else {
            workbook = new XSSFWorkbook();
            file.getParentFile().mkdirs();
        }

        Sheet sheet = findSheetIgnoreCase(workbook, subject);
        if (sheet == null) {
            sheet = workbook.createSheet(subject);
            createHeader(sheet);
        }

        deduplicateSheet(sheet);

        Set<String> existingQuestions = new HashSet<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                String qtext = getCellValue(row.getCell(0));
                if (!qtext.isEmpty()) {
                    existingQuestions.add(normalizeDedupKey(qtext));
                }
            }
        }

        int rowNum = sheet.getLastRowNum() + 1;
        int added = 0;
        Set<String> seenInBatch = new HashSet<>();
        for (Question q : newQuestions) {
            String normalized = normalizeDedupKey(q.getQuestion());
            if (existingQuestions.contains(normalized)) continue;
            if (seenInBatch.contains(normalized)) continue;
            seenInBatch.add(normalized);
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(q.getQuestion());
            List<String> opts = q.getOptions();
            for (int j = 0; j < 4 && j < opts.size(); j++) {
                row.createCell(1 + j).setCellValue(opts.get(j) != null ? opts.get(j) : "");
            }
            row.createCell(5).setCellValue(String.valueOf((char) ('A' + q.getCorrectIndex())));
            existingQuestions.add(normalized);
            added++;
        }

        if (added > 0) {
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }
        workbook.close();
    }

    private static void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            header.createCell(i).setCellValue(HEADERS[i]);
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        yield String.valueOf((int) cell.getNumericCellValue());
                    } catch (Exception e2) {
                        yield "";
                    }
                }
            }
            default -> "";
        };
    }

    /**
     * Normaliza el texto de una pregunta para usarlo como clave de deduplicación:
     * elimina número inicial, colapsa espacios, quita puntuación final, pasa a minúsculas y recorta.
     */
    public static String normalizeDedupKey(String text) {
        if (text == null) return "";
        String key = HtmlParser.stripLeadingNumber(text);
        key = key.replaceAll("\\s+", " ");
        key = key.replaceAll("[\\p{Punct}]+$", "");
        return key.toLowerCase().trim();
    }

    private static void deduplicateSheet(Sheet sheet) {
        List<String[]> rows = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String qtext = getCellValue(row.getCell(0));
            String normalized = normalizeDedupKey(qtext);
            if (normalized.isEmpty() || seen.contains(normalized)) continue;
            seen.add(normalized);
            String[] rowData = new String[6];
            rowData[0] = qtext;
            for (int j = 1; j < 6; j++) {
                rowData[j] = getCellValue(row.getCell(j));
            }
            rows.add(rowData);
        }

        for (int i = sheet.getLastRowNum(); i >= 1; i--) {
            Row r = sheet.getRow(i);
            if (r != null) sheet.removeRow(r);
        }

        int rowNum = 1;
        for (String[] rowData : rows) {
            Row r = sheet.createRow(rowNum++);
            for (int j = 0; j < rowData.length; j++) {
                r.createCell(j).setCellValue(rowData[j] != null ? rowData[j] : "");
            }
        }
    }

    /**
     * Recorre todas las hojas del Excel y elimina preguntas duplicadas dentro de cada una.
     * Devuelve un mapa: nombreDeHoja -&gt; número de duplicados eliminados.
     * Si el mapa está vacío, no se encontraron duplicados.
     */
    public static Map<String, Integer> deduplicateAllSheets(String filePath) throws IOException {
        Map<String, Integer> results = new LinkedHashMap<>();
        File file = new File(filePath);
        if (!file.exists()) return results;

        Workbook workbook;
        try (InputStream is = new FileInputStream(filePath)) {
            workbook = new XSSFWorkbook(is);
        }

        int totalRemoved = 0;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            int before = countQuestions(sheet);
            deduplicateSheet(sheet);
            int after = countQuestions(sheet);
            int removed = before - after;
            if (removed > 0) {
                results.put(sheet.getSheetName(), removed);
                totalRemoved += removed;
            }
        }

        if (totalRemoved > 0) {
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
        workbook.close();
        return results;
    }

    private static int countQuestions(Sheet sheet) {
        int count = 0;
        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row != null && !getCellValue(row.getCell(0)).isEmpty()) count++;
        }
        return count;
    }

    public static void createEmptyExcel(String filePath) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try (Workbook workbook = new XSSFWorkbook()) {
            List<String> subjects = Arrays.asList(
                "Acceso a datos",
                "Programacion de servicios y procesos",
                "Sistemas de gestion empresarial",
                "Desarrollo de interfaces",
                "Programacion multimedia y dispositivos moviles",
                "Itinerario para la empleabilidad 2",
                "Ingles",
                "Big data"
            );
            for (String subject : subjects) {
                Sheet sheet = workbook.createSheet(subject);
                createHeader(sheet);
                sheet.createRow(1);
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

    private static Sheet findSheetIgnoreCase(Workbook workbook, String name) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (workbook.getSheetAt(i).getSheetName().equalsIgnoreCase(name)) {
                return workbook.getSheetAt(i);
            }
        }
        return null;
    }

    public static void createSampleExcel(String filePath) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try (Workbook workbook = new XSSFWorkbook()) {
            Map<String, List<String[]>> data = new LinkedHashMap<>();
            data.put("Programacion", Arrays.asList(
                new String[]{"Que es un objeto en POO?", "Una funcion", "Una instancia de una clase", "Un tipo de dato primitivo", "Un bucle", "B"},
                new String[]{"Que palabra clave se usa para heredar en Java?", "implements", "extends", "inherits", "super", "B"},
                new String[]{"Cual es el tipo de dato para texto en Java?", "int", "char", "String", "boolean", "C"},
                new String[]{"Que es un array?", "Estructura que almacena multiples valores del mismo tipo", "Un tipo de objeto", "Una funcion", "Una clase", "A"},
                new String[]{"Que hace el operador ++?", "Suma 1", "Resta 1", "Multiplica por 2", "Divide entre 2", "A"},
                new String[]{"Cual es el metodo principal en Java?", "main()", "start()", "run()", "init()", "A"},
                new String[]{"Que coleccion no permite duplicados?", "List", "Set", "Map", "Queue", "B"},
                new String[]{"Que significa LTS en Java?", "Long Time Support", "Long Term Support", "Latest Technology Standard", "Lightweight Thread System", "B"},
                new String[]{"Cual de estos no es un modificador de acceso?", "public", "private", "static", "protected", "C"},
                new String[]{"Que excepcion se lanza cuando un recurso no se encuentra?", "IOException", "FileNotFoundException", "NullPointerException", "ClassNotFoundException", "B"}
            ));
            data.put("BBDD", Arrays.asList(
                new String[]{"Que significa SQL?", "Structured Query Language", "Simple Query Language", "Standard Query Language", "Sequential Query Language", "A"},
                new String[]{"Cual es la clausula para filtrar registros?", "WHERE", "HAVING", "FILTER", "MATCH", "A"},
                new String[]{"Que tipo de JOIN devuelve solo las filas coincidentes?", "LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "FULL JOIN", "C"},
                new String[]{"Que comando se usa para insertar datos?", "INSERT INTO", "ADD TO", "PUT INTO", "CREATE", "A"},
                new String[]{"Que es una clave primaria?", "Un identificador unico para cada fila", "Una clave para abrir la BD", "El primer campo de la tabla", "Un tipo de indice", "A"},
                new String[]{"Que hace COUNT(*)?", "Cuenta filas", "Suma valores", "Calcula el promedio", "Cuenta columnas", "A"},
                new String[]{"Cual es el tipo de dato para numeros decimales en SQL?", "INT", "VARCHAR", "DECIMAL", "DATE", "C"},
                new String[]{"Que normalizacion elimina dependencias transitivas?", "1NF", "2NF", "3NF", "BCNF", "C"},
                new String[]{"Que es una vista en BD?", "Una tabla virtual basada en una consulta", "Una tabla fisica", "Un tipo de indice", "Una funcion", "A"},
                new String[]{"Que comando elimina una tabla?", "DROP TABLE", "DELETE TABLE", "REMOVE TABLE", "ERASE TABLE", "A"}
            ));
            data.put("Sistemas", Arrays.asList(
                new String[]{"Que sistema operativo es de codigo abierto?", "Windows", "macOS", "Linux", "iOS", "C"},
                new String[]{"Que es una IP?", "Direccion de red", "Un protocolo de archivos", "Tipo de memoria", "Un periferico", "A"},
                new String[]{"Que hace el comando ping?", "Prueba conectividad de red", "Mide velocidad de disco", "Lista procesos", "Muestra uso de RAM", "A"},
                new String[]{"Que es una maquina virtual?", "Simulacion software de un ordenador", "Un PC fisico", "Un tipo de disco", "Un monitor", "A"},
                new String[]{"Que particion se usa para swap en Linux?", "ext4", "ntfs", "swap", "fat32", "C"},
                new String[]{"Que es el kernel?", "Nucleo del SO", "La interfaz grafica", "Un programa de usuario", "Un driver", "A"},
                new String[]{"Que hace chmod 755?", "Establece permisos rwxr-xr-x", "Cambia el propietario", "Borra un archivo", "Copia un directorio", "A"},
                new String[]{"Que es RAID 1?", "Mirroring (espejo)", "Striping", "Paridad", "Ninguno", "A"},
                new String[]{"Que comando lista procesos en Linux?", "ps", "ls", "dir", "cd", "A"},
                new String[]{"Que protocolo se usa para web?", "HTTP", "FTP", "SSH", "SMTP", "A"}
            ));
            data.put("Marcas", Arrays.asList(
                new String[]{"Que significa HTML?", "HyperText Markup Language", "HighText Machine Language", "HyperTool Markup Language", "None", "A"},
                new String[]{"Cual es la etiqueta para enlace en HTML?", "<a>", "<link>", "<href>", "<url>", "A"},
                new String[]{"Que atributo HTML define una imagen?", "src", "href", "alt", "img", "A"},
                new String[]{"Que CSS propiedad cambia el color de fondo?", "background-color", "color", "font-color", "bgcolor", "A"},
                new String[]{"Que etiqueta crea una lista ordenada?", "<ol>", "<ul>", "<li>", "<list>", "A"},
                new String[]{"Que es XML?", "Lenguaje de marcado extensible", "Base de datos", "Lenguaje de programacion", "Sistema operativo", "A"},
                new String[]{"Que significa XSD?", "XML Schema Definition", "XML Structured Data", "Extended Schema Document", "XPath Standard Definition", "A"},
                new String[]{"Que etiqueta HTML5 se usa para navegacion?", "<nav>", "<menu>", "<header>", "<section>", "A"},
                new String[]{"Que propiedad CSS hace un elemento flexible?", "display: flex", "position: relative", "float: left", "display: block", "A"},
                new String[]{"Que comando Git sube cambios al remoto?", "git push", "git commit", "git pull", "git clone", "A"}
            ));
            data.put("Entornos", Arrays.asList(
                new String[]{"Que es Git?", "Sistema de control de versiones", "Un IDE", "Un compilador", "Un SO", "A"},
                new String[]{"Que comando Git crea un commit?", "git commit -m mensaje", "git push", "git add", "git save", "A"},
                new String[]{"Que es un repositorio?", "Almacen de codigo y su historial", "Una carpeta cualquiera", "Un archivo de texto", "Un programa", "A"},
                new String[]{"Para que sirve UML?", "Modelar y disenar sistemas", "Compilar codigo", "Ejecutar pruebas", "Desplegar aplicaciones", "A"},
                new String[]{"Que es un diagrama de clases?", "Representacion de clases y sus relaciones", "Diagrama de red", "Diagrama de flujo", "Diagrama de base de datos", "A"},
                new String[]{"Que herramienta se usa para construir proyectos Java?", "Maven", "Git", "VS Code", "UML", "A"},
                new String[]{"Que tipo de prueba verifica unidades individuales?", "Test unitario", "Test de integracion", "Test funcional", "Test de estres", "A"},
                new String[]{"Que es JUnit?", "Framework para pruebas en Java", "Un IDE", "Un compilador", "Un gestor de dependencias", "A"},
                new String[]{"Que es un issue en GitHub?", "Tarea o problema reportado en un repositorio", "Un error de compilacion", "Una rama de Git", "Un tipo de commit", "A"},
                new String[]{"Que significa CI/CD?", "Integracion Continua / Despliegue Continuo", "Compilacion Interna / Codigo Directo", "Control de Interfaces / Desarrollo Continuo", "Centralizacion de Informacion y Codigo", "A"}
            ));

            for (var entry : data.entrySet()) {
                Sheet sheet = workbook.createSheet(entry.getKey());
                createHeader(sheet);
                int rowNum = 1;
                for (String[] q : entry.getValue()) {
                    Row row = sheet.createRow(rowNum++);
                    for (int i = 0; i < q.length; i++) {
                        row.createCell(i).setCellValue(q[i]);
                    }
                }
                for (int i = 0; i < HEADERS.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

}
