package com.mrepol742.rssgenerator;

import java.text.SimpleDateFormat;
import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.nio.file.Paths;
import java.nio.file.Files;


import org.json.*;

import org.jsoup.*;
import org.jsoup.select.*;
import org.jsoup.helper.*;
import org.jsoup.internal.*;
import org.jsoup.nodes.*;
import org.jsoup.parser.*;

public class App {

    static List<Item> items = new ArrayList<>();
    static JSONObject obj = new JSONObject();
    static SimpleDateFormat format =  new SimpleDateFormat("E, dd MMMM yyyy k:m:s z");
    static String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rss xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\" xmlns:atom=\"http://www.w3.org/2005/Atom\" version=\"2.0\" xmlns:media=\"http://search.yahoo.com/mrss/\">\n<channel>\n";
    static String footer = "</channel>\n</rss>";

    static StringBuilder rss = new StringBuilder();
    static Arg arg;
    static boolean isHome = false;

    public static void main(String[] args) throws IOException  {
        if (args.length == 0) {
            System.out.println("arguments:\n\t--domain [address]\n\t--publisher [name]\noptional:\n\t--projectFolder [location]");
            return;
        }
        arg = resolveArguments(args);
        rss.append(header);
                find(new File(arg.getProjectFolder()), arg.getDomain());
        for (Item item: items) {
            rss.append(String.format("<item>\n" +
    "  <title>%1$s</title>\n" +
    "  <description>%2$s</description>\n" +
    "  <link>%3$s</link>\n" + 
    "  <creator>%4$s</creator>\n" +
    "  <content medium=\"image\" url=\"%5$s\"/>\n", item.title, item.description, item.link, "<![CDATA[ " + arg.getPublisher() + " ]]>", item.medium_url));
            rss.append("</item>\n");
        }
        rss.append(footer);
        Files.createDirectories(Paths.get(arg.getProjectFolder() + "/rss"));
        if (write(new File(arg.getProjectFolder() + arg.getOutputFile()), rss.toString(), false)) {
            System.out.println("\nRSS generated for " + arg.getDomain());
        } else {
            System.out.println("\nFailed to generate rss.");
        }
    }

    public static String getTitle(File file) {
        StringBuilder str = new StringBuilder();
        Document doc = Jsoup.parse(read(file, "\n"));
        Elements title = doc.getElementsByTag("title");
        return title.get(0).toString().replace("<title>", "").replace("</title>", "");
    }

    public static String[] getMeta(File file) {
        String[] data = {"",""};
        Document doc = Jsoup.parse(read(file, "\n"));
        Elements metas = doc.getElementsByTag("meta");
        for (Element meta : metas) {
            String content = meta.attr("content");
            String name = meta.attr("name");
            String prop = meta.attr("property");
            if (name.equals("description")) {
                data[0] = content;
            }
            if (prop.equals("og:image")) {
                data[1] = content;
            }
        }
        return data;
    }

    public static void find(File file, String domain) {
        if (file.list() == null) {
            System.out.println("no index " + file.toString());
            return;
        }
        if (file.isDirectory() && !isHome) {
            String[] metas = getMeta(new File(arg.getProjectFolder() + "/index.html"));
            rss.append("  <title>");
            rss.append("<![CDATA[");
            rss.append(getTitle(new File(arg.getProjectFolder() + "/index.html")));
            rss.append("]]>");
            rss.append("</title>\n");
            rss.append("  <description>");
            rss.append("<![CDATA[");
            rss.append( metas[0]);
            rss.append("]]>");
            rss.append("</description>\n");
            rss.append("  <link>");
            rss.append(arg.getDomain());
            rss.append("  </link>\n");
            rss.append("  <image>\n");
            rss.append("    <url>");
            rss.append(metas[1]);
            rss.append("</url>\n");
            rss.append("    <title>");
            rss.append(getTitle(new File(arg.getProjectFolder() + "/index.html")));
            rss.append("</title>\n");
            rss.append("    <link>");
            rss.append(arg.getDomain());
            rss.append("</link>\n");
            rss.append("  </image>\n  <generator> RSS for ");
            rss.append(arg.getPublisher());
            rss.append(" </generator>\n");
            rss.append("  <lastBuildDate>");
            rss.append(format.format(new Date()));
            rss.append("</lastBuildDate>\n");
            rss.append("  <link href=\"" + arg.getDomain() + arg.getOutputFile() + "\"" + " rel=\"self\" type=\"application/rss+xml\"/>\n");
            rss.append("  <language><![CDATA[ en ]]></language>\n");
            isHome = true;
        }

        String[] listFiles = file.list();
        for (String str: listFiles) {
            File folder = new File(file.getAbsolutePath() + "/" + str);
            if (folder.isDirectory()) {
                File hasIndex = new File(folder.getAbsolutePath() + "/index.html");
                if (hasIndex.isFile()) {
                    System.out.println(format.format(hasIndex.lastModified()) + " | " + arg.getDomain() + hasIndex.getParentFile().getAbsolutePath().replace(arg.getProjectFolder(), ""));
                    String[] metas = getMeta(new File(hasIndex.getParentFile().getAbsolutePath() + "/index.html"));
                     items.add(new Item("<![CDATA[" + getTitle(new File(hasIndex.getParentFile().getAbsolutePath() + "/index.html")) + "]]>", "<![CDATA[" + metas[0] + "]]>", arg.getDomain() + hasIndex.getParentFile().getAbsolutePath().replace(arg.getProjectFolder(), ""), metas[1]));
                     find(new File (file.getAbsolutePath() + "/" + str), arg.getDomain());
                }
            }
        
        }
    }


    public static boolean write(File location, String data, boolean readOnly) {
        try {
            FileWriter fw = new FileWriter(location, false);
            fw.write(data);
            fw.close();
            if (readOnly) {
                boolean bn = location.setReadOnly();
            }
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

       public static String read(java.io.File fe, String line) {
        try {
            if (!fe.exists()) {
                return null;
            }
            FileReader fr = new FileReader(fe);
            BufferedReader br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String ln;
            while ((ln = br.readLine()) != null) {
                sb.append(ln);
                sb.append(line);
            }
            fr.close();
            br.close();
            return sb.toString();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static Arg resolveArguments(String[] args) {
        Arg arg = new Arg();
        for (int i = 0; i < args.length; i++) {
            switch(args[i]) {
                case "--domain":
                    arg.setDomain(args[i + 1]);
                break;
                case "--publisher":
                    arg.setPublisher(args[i + 1]);
                break;
                case "--projectFolder":
                    arg.setProjectFolder(args[i + 1]);
                break;
            }
        }

        if (arg.getDomain() == null) {
            throw new RuntimeException("Undefined --domain value");
        }
        if (arg.getPublisher() == null) {
            throw new RuntimeException("Undefined --publisher value");
        }
        return arg;
    }
}