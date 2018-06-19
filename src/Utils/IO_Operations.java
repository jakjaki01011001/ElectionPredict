/**
 * @author : Arman Kabiri
 * @Email : "arman73k@gmail.com"
 * @Linked in : https://www.linkedin.com/in/armankabiri73
 * @Created at Dec 9, 2016 , 1:35:50 PM
 */
package Utils;

import Modules.LexiconRecord;
import Modules.CommentRecord;
import Modules.Record;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class IO_Operations {

    // Load Datasets Json
    public ArrayList<? extends Record> loadDataSetJson(String inputURL, boolean shuffle, boolean limit) throws JSONException, IOException {

        String[] fileTypes = {"json"};
        Collection<File> jsonFiles = FileUtils.listFiles(new File(inputURL), fileTypes, true);      // Load all Files Recursively in Folder
        ArrayList<CommentRecord> allRecords = new ArrayList<>();
        MutableInt idCounter = new MutableInt(0);

        for (File jsonFile : jsonFiles) {
            String fileName = jsonFile.getName();
            fileName = fileName.toLowerCase();
            // Just Load Files From Levels 1,2,3
            if (fileName.contains("telemine_key1") || fileName.contains("telemine_key2") || fileName.contains("telemine_key3")) {
                String rawStr = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
                JSONArray jsonArray = new JSONArray(rawStr);

                for (int i = 0; i < jsonArray.length(); i++) {
                    idCounter.increment();
                    CommentRecord record = new CommentRecord();
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    record.id = idCounter.getValue();
                    record.viewCount = jsonObj.optInt("viewCount", 0);
                    record.text = jsonObj.optString("text", "");
                    record.publishedDate = jsonObj.optLong("date", 0);
                    specifyDateLevel(record);
                    specifyViewCountLevel(record);
                    allRecords.add(record);
                }

                System.out.println("File : " + jsonFile.getName() + " was loaded.");
            }
        }
        System.out.println(allRecords.size() + " records was loaded");
        return allRecords;
    }

    public ArrayList<LexiconRecord> loadLexiconExcel(String inputURL) {
        ArrayList<LexiconRecord> lexicons = new ArrayList<>();
        try {
            int idCounter = 1;
            FileInputStream excelFile;
            excelFile = new FileInputStream(new File(inputURL));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            if (rows.hasNext()) {
                rows.next(); //skip header
            }
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                Cell wordCell = currentRow.getCell(0);
                Cell scoreCell = currentRow.getCell(1);
                LexiconRecord record = new LexiconRecord();
                record.id = idCounter++;
                record.score = (int) scoreCell.getNumericCellValue();
                record.word = wordCell.getStringCellValue();
                if (record.score != 3) {
                    lexicons.add(record);
                }
            }
            excelFile.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IO_Operations.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IO_Operations.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return lexicons;
    }

    // Load Lexicon XML Format. : Like LexiPers
    public ArrayList<LexiconRecord> loadLexiconXML(String inputURL) {

        ArrayList<LexiconRecord> lexicons = new ArrayList<>();
        try {
            File xmlFile = new File(inputURL);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList synsetList = doc.getElementsByTagName("Synset");
            int synsetListLength = synsetList.getLength();
            for (int i = 0; i < synsetListLength; i++) {
                Node synsetNode = synsetList.item(i);
                if (synsetNode.getNodeType() == Node.ELEMENT_NODE) {
                    LexiconRecord lexRecord = new LexiconRecord();
                    Element synsetElement = (Element) synsetNode;
                    lexRecord.id = Integer.valueOf(synsetElement.getAttribute("ID"));
                    String expressions = synsetElement.getAttribute("Sense");
                    List<String> expressionsList = Arrays.asList(expressions.split(","));
                    StringBuilder lexWordBuilder = new StringBuilder();
                    lexWordBuilder.append("(");
                    expressionsList.stream().forEach(ex -> {
                        lexWordBuilder.append("(").append(ex).append(")|");
                    });
                    lexWordBuilder.deleteCharAt(lexWordBuilder.length() - 1);
                    lexWordBuilder.append(")");
                    lexRecord.word = lexWordBuilder.toString();
                    byte label = Byte.valueOf(synsetElement.getAttribute("Label"));
                    switch (label) {
                        case -1:
                            lexRecord.score = 1;
                            break;
                        case 0:
                            lexRecord.score = 3;
                            break;
                        case +1:
                            lexRecord.score = 5;
                            break;
                    }
                    if (lexRecord.score != 3) {
                        lexicons.add(lexRecord);
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return lexicons;
    }

    public ArrayList<String> loadStringList(String inputURL) {
        List<String> lines = new ArrayList<>();
        try {
            lines = FileUtils.readLines(new File(inputURL), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(IO_Operations.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return (ArrayList<String>) lines;
    }

    //  Specifying Different Weights for different messages seen by various Users.
    // The more viewCount is, The more weight this vote has.
    private void specifyViewCountLevel(CommentRecord record) {
        if (record.viewCount < 500) {
            record.viewCountLevel = 2;
        } else if (record.viewCount < 10000) {
            record.viewCountLevel = 4;
        } else if (record.viewCount < 100000) {
            record.viewCountLevel = 6;
        } else if (record.viewCount < 1000000) {
            record.viewCountLevel = 8;
        } else if (record.viewCount >= 1000000) {
            record.viewCountLevel = 10;
        }
    }

    // Specifying Different Weights for different messages published in various times
    // The more Recent Messages are more important.
    private void specifyDateLevel(CommentRecord record) {
        long maxDate = 1495135799;  //    5/18/2017, 11:59:59 PM    Last day of the Election period

        if ((maxDate - record.publishedDate) < (2 * 24 * 3600)) {       //l ess than 2 days ago
            record.publishedDateLevel = 12;
        } else if ((maxDate - record.publishedDate) < (5 * 24 * 3600)) {       //l ess than 5 days ago
            record.publishedDateLevel = 10;
        } else if ((maxDate - record.publishedDate) < (10 * 24 * 3600)) {     //less than 10 days ago
            record.publishedDateLevel = 6;
        } else if ((maxDate - record.publishedDate) < (20 * 24 * 3600)) {      //less than 20 days ago
            record.publishedDateLevel = 4;
        } else if ((maxDate - record.publishedDate) < (35 * 24 * 3600)) {      //less than 35 days ago
            record.publishedDateLevel = 2;
        } else if ((maxDate - record.publishedDate) >= (35 * 24 * 3600)) {      //more than 35 days ago
            record.publishedDateLevel = 1;
        }
    }
}
