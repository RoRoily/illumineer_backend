package com.buaa01.illumineer_backend.tool;

import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.mapper.StormMapper;
import com.buaa01.illumineer_backend.service.CategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

@Component
public class StormTool {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private PaperMapper paperMapper;
    @Autowired
    private StormMapper stormMapper;
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    public String check() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
        String datePart = "";
        String url = "https://openalex.s3.amazonaws.com/?list-type=2&delimiter=%2F&prefix=data%2Fworks%2F";
        URI uri = new URI(url);
        URL xmlUrl = uri.toURL();
        InputStream inputStream = xmlUrl.openStream();
        // 创建一个 DocumentBuilder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        // 解析 XML 输入流并返回 Document 对象
        Document document = builder.parse(inputStream);
        // 获取根元素
        Element root = document.getDocumentElement();
        // 获取所有子元素 (例如，处理 <CommonPrefixes> 标签)
        NodeList commonPrefixes = root.getElementsByTagName("CommonPrefixes");
        Node commonPrefix = commonPrefixes.item(commonPrefixes.getLength() - 1);
        if (commonPrefix.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) commonPrefix;
            String path = element.getTextContent();
            String[] parts = path.split("/");
            Optional<String> updatedDatePart = Arrays.stream(parts)
                    .filter(part -> part.startsWith("updated_date="))
                    .findFirst();
            if (updatedDatePart.isPresent()) {
                datePart = updatedDatePart.get().substring("updated_date=".length());
                System.out.println(datePart);
            } else {
                System.out.println("updated_date part not found");
            }
        }
        // 关闭输入流
        inputStream.close();
        return datePart;
    }

    public int getPapers(String last_update) {
        AtomicReference<Paper> article = new AtomicReference<>();
        AtomicInteger articles = new AtomicInteger();
        String downloadDir = "downloads";
        try {
            String url2 = "https://openalex.s3.amazonaws.com/?list-type=2&delimiter=%2F&prefix=data%2Fworks%2Fupdated_date%3D" + last_update + "%2F";
            URI uri2 = new URI(url2);
            URL xmlUrl2 = uri2.toURL();
            InputStream inputStream2 = xmlUrl2.openStream();
            DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder2 = factory2.newDocumentBuilder();
            Document document2 = builder2.parse(inputStream2);
            Element root2 = document2.getDocumentElement();
            NodeList commonPrefixes2 = root2.getElementsByTagName("Contents");
            int len2 = commonPrefixes2.getLength();
            System.out.println(len2);
            for (int i = 0; i < len2; i++) {
                String uri0 = String.format("https://openalex.s3.amazonaws.com/data/works/updated_date%%3D%s/part_%03d.gz", last_update, i);
                System.out.println(uri0); // 用于调试，可以删除
                downloadFile(uri0, "downloads\\part_" + String.format("%03d", i) + ".gz");
            }
            // Find the .gz file in the download directory
            File[] gzFiles = new File(downloadDir).listFiles((dir, name) -> name.endsWith(".gz"));
            if (gzFiles == null || gzFiles.length == 0) {
                System.out.println("No .gz file found!");
                return articles.get();
            }
            for (int i = 0; i < gzFiles.length; i++) {
                File gzFile = gzFiles[i];
                // Decompress the .gz file
                String outputPath = "downloads\\data\\data" + i + ".txt"; // Output file path
                try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(gzFile));
                     FileOutputStream fos = new FileOutputStream(outputPath)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = gis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                // Read and print the decompressed file content
                try (BufferedReader br = new BufferedReader(new FileReader(outputPath))) {
                    String line;
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    while ((line = br.readLine()) != null) {
                        String finalLine = line;
                        // Assuming the line contains JSON
                        article.set(handle(finalLine));
                        if (article.get() != null && paperMapper.getPaperByPid(article.get().getPid()) == null) {
                            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                                try {
                                    stormMapper.insertPaper(article.get().getPid(), article.get().getTitle(),
                                            article.get().getEssAbs(), article.get().getKeywords(),
                                            article.get().getContentUrl(), article.get().getAuths(),
                                            article.get().getCategory(), article.get().getType(),
                                            article.get().getTheme(), article.get().getPublishDate(),
                                            article.get().getDerivation(), article.get().getRefs(),
                                            article.get().getRefTimes(), article.get().getFavTimes(),
                                            article.get().getStats());
                                    articles.getAndIncrement();
                                } catch (Exception e) {
                                    // Handle exceptions during processing
                                    e.printStackTrace();
                                }
                            }, taskExecutor);
                            futures.add(future);
                        }
                    }
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                }
            }
            // Delete and recreate the download directory
            Path downloadPath = Paths.get(downloadDir);
            if (Files.exists(downloadPath)) {
                try (Stream<Path> paths = Files.walk(downloadPath)) {
                    paths.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Files.createDirectories(downloadPath);
        } catch (Exception e) {
            e.printStackTrace();
            return articles.get();
        }
        return articles.get();
    }

    private Paper handle(String line) throws ParseException, SQLException, JsonProcessingException {
        JsonObject jsonObject = JsonParser.parseString(line).getAsJsonObject();
        Paper article = new Paper();
        String oid = jsonObject.get("id").getAsString();
        int start = oid.indexOf('W') + 1;
        int end = oid.length();
        long id = Long.parseLong(oid.substring(start, end));
        article.setPid(id);
        JsonElement title = jsonObject.get("title");
        if (title != null && !title.isJsonNull())
            article.setTitle(jsonObject.get("title").getAsString());
        else
            article.setTitle("");
        JsonElement abstractIndex = jsonObject.get("abstract_inverted_index");
        if (abstractIndex != null && abstractIndex.isJsonObject()) {
            article.setEssAbs(montage(abstractIndex.toString()));
        } else {
            article.setEssAbs(""); // 或者设为某个默认值
        }
        JsonElement keywordsElement = jsonObject.get("keywords");
        List<String> keywordList = new ArrayList<>();
        if (keywordsElement != null && keywordsElement.isJsonArray()) {
            JsonArray keys = keywordsElement.getAsJsonArray();
            if (!keys.isEmpty()) {
                for (JsonElement key : keys) {
                    if (key.isJsonObject()) {
                        JsonObject keyObject = key.getAsJsonObject();
                        JsonElement keywordElement = keyObject.get("display_name");
                        // 确保 keywordElement 不为 null
                        if (keywordElement != null && !keywordElement.isJsonNull()) {
                            String k = keywordElement.getAsString();
                            keywordList.add(k);
                        }
                    }
                }
            }
        }
        article.setKeywords(keywordList);
        JsonElement primaryLocationElement = jsonObject.get("primary_location");
        if (primaryLocationElement != null && primaryLocationElement.isJsonObject()) {
            JsonObject primaryLocation = primaryLocationElement.getAsJsonObject();
            JsonElement pdfUrlElement = primaryLocation.get("pdf_url");
            if (pdfUrlElement != null && !pdfUrlElement.isJsonNull()) {
                String pdfUrl = pdfUrlElement.getAsString();
                article.setContentUrl(pdfUrl);
            } else {
                article.setContentUrl("");
            }
        } else {
            article.setContentUrl("");
        }
        Map<String, Integer> displayNames = new HashMap<>();
        JsonArray authorships = jsonObject.getAsJsonArray("authorships");
        for (JsonElement authorship : authorships) {
            JsonObject author = authorship.getAsJsonObject().getAsJsonObject("author");
            String displayName = author.get("display_name").getAsString();
            displayNames.put(displayName, 0);
        }
        article.setAuths(displayNames);
        JsonElement topicElement = jsonObject.get("primary_topic");
        if (topicElement != null && !topicElement.isJsonNull()) {
            String theme = topicElement.getAsJsonObject().get("display_name").getAsString();
            article.setTheme(theme);
            String subfield = topicElement.getAsJsonObject().get("subfield").getAsJsonObject().get("display_name").getAsString();
            String field = topicElement.getAsJsonObject().get("field").getAsJsonObject().get("display_name").getAsString();
            String subfieldId = topicElement.getAsJsonObject().get("subfield").getAsJsonObject().get("id").getAsString();
            String fieldId = topicElement.getAsJsonObject().get("field").getAsJsonObject().get("id").getAsString();
//            article.setCategoryId(Integer.parseInt(fieldId));
            // 定义正则表达式：提取 URL 中最后的数字
            Pattern pattern = Pattern.compile("(\\d+)$");
            Matcher matcher = pattern.matcher(subfieldId.trim());
            // 查找并提取匹配的数字
            String subfieldNumber = "1";
            if (matcher.find()) {
                subfieldNumber = matcher.group(1);
            }
            matcher = pattern.matcher(fieldId.trim());
            String fieldNumber = "0";
            if (matcher.find()) {
                fieldNumber = matcher.group(1);
            }
            Category rs = categoryService.getCategoryByID(subfieldNumber);
            if (rs == null) {
                rs = categoryService.insertCategory(subfieldNumber, fieldNumber, subfield, field);
            }
            article.setCategory(rs.toJsonString());
        }
        String date = jsonObject.get("publication_date").getAsString();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date publishDate = formatter.parse(date);
        article.setPublishDate(publishDate);
        if (primaryLocationElement != null && primaryLocationElement.isJsonObject()) {
            JsonObject primaryLocation = primaryLocationElement.getAsJsonObject();
            JsonElement sourceElement = primaryLocation.get("source");
            if (sourceElement != null && sourceElement.isJsonObject()) {
                JsonObject source = sourceElement.getAsJsonObject();
                JsonElement pubElement = source.get("publisher");
                if (pubElement != null && !pubElement.isJsonNull()) {
                    String publisher = pubElement.getAsString();
                    article.setDerivation(publisher);
                } else {
                    article.setDerivation("");
                }
            } else {
                article.setDerivation("");
            }
        } else {
            article.setDerivation("");
        }
        article.setRefTimes(jsonObject.get("cited_by_count").getAsInt());
        List<Long> referenceList = new ArrayList<>();
        JsonArray references = jsonObject.getAsJsonArray("related_works");
        for (JsonElement reference : references) {
            String r = reference.getAsString();
            start = r.indexOf('W') + 1;
            end = r.length();
            id = Long.parseLong(r.substring(start, end));
            referenceList.add(id);
        }
        article.setRefs(referenceList);
        article.setFavTimes(0);
        article.setStats(0);
        JsonElement typeElement = jsonObject.get("type");
        if (typeElement != null && !typeElement.isJsonNull()) {
            String type = typeElement.getAsString();
            article.setType(type);
        } else
            article.setType("");
        return article;
    }

    public String montage(String originalString) {
        JSONObject jsonObject = new JSONObject(originalString);
        // 创建一个空的列表来存储单词及其位置
        List<Map.Entry<Integer, String>> sentenceList = new ArrayList<>();
        // 遍历倒排索引，将每个单词和位置对存入句子列表
        for (String word : jsonObject.keySet()) {
            JSONArray positions = jsonObject.getJSONArray(word);
            for (int i = 0; i < positions.length(); i++) {
                sentenceList.add(new AbstractMap.SimpleEntry<>(positions.getInt(i), word));
            }
        }
        // 按照位置对列表进行排序
        sentenceList.sort(Comparator.comparingInt(Map.Entry::getKey));
        // 拼接成完整的句子
        StringBuilder reconstructedSentence = new StringBuilder();
        for (Map.Entry<Integer, String> entry : sentenceList) {
            reconstructedSentence.append(entry.getValue()).append(" ");
        }
        return reconstructedSentence.toString().trim();
    }

    public static void downloadFile(String fileURL, String saveDir) throws URISyntaxException {
        try {
            URI url = new URI(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.toURL().openConnection();
            int responseCode = httpConn.getResponseCode();

            // 检查HTTP响应码是否为200 (HTTP_OK)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 打开输入流
                InputStream inputStream = httpConn.getInputStream();
                // 打开输出流
                FileOutputStream outputStream = new FileOutputStream(saveDir);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // 关闭流
                outputStream.close();
                inputStream.close();

                System.out.println("File downloaded and saved to " + saveDir);
            } else {
                System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            }
            httpConn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
