package com.buaa01.illumineer_backend.tool;

import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.entity.Paper;
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
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

@Component
public class StormTool {
    @Autowired
    private CategoryService categoryService;

    public String check(String last_update) {
        WebDriverManager.chromedriver().driverVersion("129.0.6668.59").setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=old");
        options.addArguments("--remote-allow-origins=*");
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get("https://openalex.s3.amazonaws.com/browse.html#data/works/");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            while (true) {
                Thread.sleep(2000);
                // 尝试查找下一页按钮
                List<WebElement> nextPageButtonList = driver.findElements(By.xpath("//li[@class='paginate_button next']"));
                // 如果找不到下一页按钮，直接退出循环
                if (nextPageButtonList.isEmpty()) {
                    WebElement tbody = driver.findElement(By.id("tbody-s3objects"));
                    List<WebElement> rows = tbody.findElements(By.tagName("tr"));
                    WebElement secondLastRow = rows.get(rows.size() - 2);
                    return secondLastRow.findElements(By.tagName("td")).get(0).getText();  // 到达最后一页
                }
                WebElement nextPageButton = nextPageButtonList.get(0);
                WebElement a = nextPageButton.findElement(By.tagName("a"));
                // 使用 JavaScript 点击翻页按钮
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].click();", a);
                // 等待页面加载完成，或等待翻页按钮状态变化
                wait.until(ExpectedConditions.stalenessOf(nextPageButton));  // 确保页面刷新
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }

    public ArrayList<Paper> getPapers(String last_update) {
        Paper article;
        ArrayList<Paper> articles = new ArrayList<>();
        String downloadDir = "D:\\java\\demo1\\src\\main\\java\\Tool\\data";
        WebDriverManager.chromedriver().driverVersion("129.0.6668.59").setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=old");
        options.addArguments("--remote-allow-origins=*");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir);  // 设置下载路径
        prefs.put("download.prompt_for_download", false);      // 不询问下载路径
        prefs.put("download.directory_upgrade", true);         // 自动升级目录
        prefs.put("safebrowsing.enabled", true);               // 禁用安全浏览器保护
        options.setExperimentalOption("prefs", prefs);
        // Initialize ChromeDriver
        WebDriver driver = new ChromeDriver(options);
        try {
            // Navigate to the URL
            driver.get("https://openalex.s3.amazonaws.com/browse.html#data/works/" + last_update);
            Thread.sleep(1000);
            // Maximize browser window
            driver.manage().window().maximize();
            Thread.sleep(1000);
            // Wait for the tbody to be present
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement tbody2 = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody[@id='tbody-s3objects']")));
            // Find the last row in the tbody and its first <td> element
            List<WebElement> rows = tbody2.findElements(By.tagName("tr"));
            WebElement lastRow = rows.get(rows.size() - 1);
            WebElement td2 = lastRow.findElements(By.tagName("td")).get(0);
            // Click on the link inside the <td>
            WebElement a = td2.findElement(By.tagName("a"));
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
            // Click the download link
            a.click();
            String fileName = a.getText(); // Expected downloaded file name
            waitForDownloadToComplete(downloadDir, fileName);
            // Close the browser
            driver.quit();
            // Find the .gz file in the download directory
            File[] gzFiles = new File(downloadDir).listFiles((dir, name) -> name.endsWith(".gz"));
            if (gzFiles == null || gzFiles.length == 0) {
                System.out.println("No .gz file found!");
                return articles;
            }
            File gzFile = gzFiles[0];
            // Decompress the .gz file
            String outputPath = "D:\\java\\demo1\\src\\main\\java\\Tool\\data\\data.txt"; // Output file path
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
                while ((line = br.readLine()) != null) {
                    // Assuming the line contains JSON
                    article = handle(line);
                    articles.add(article);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return articles;
        }
        return articles;
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
            article.setEssabs(montage(abstractIndex.toString()));
        } else {
            article.setEssabs(""); // 或者设为某个默认值
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
            article.setCategoryId(Integer.parseInt(fieldId));
            // 定义正则表达式：提取 URL 中最后的数字
            Pattern pattern = Pattern.compile("(\\d+)$");
            Matcher matcher = pattern.matcher(subfieldId.trim());
            // 查找并提取匹配的数字
            String subfieldNumber = "1";
            if (matcher.find()) {
                subfieldNumber = matcher.group(1);
            }
            matcher = pattern.matcher(fieldId.trim());
            String  fieldNumber = "0";
            if (matcher.find()) {
                fieldNumber = matcher.group(1);
            }
            Category rs = categoryService.getCategoryByID(subfieldNumber, fieldNumber);
            if (rs == null) {
                rs = categoryService.insertCategory(subfieldNumber, fieldNumber, subfield, field);
            }
            article.setField(rs.toJsonString());
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
        article.setFavTime(0);
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

    // Method to wait until the download is complete
    public static void waitForDownloadToComplete(String downloadDir, String fileName) throws InterruptedException {
        File file = new File(downloadDir, fileName);
        File tempFile = new File(downloadDir, fileName + ".crdownload");
        while (!file.exists() || tempFile.exists()) {
            Thread.sleep(1000);
        }
    }
}
