package com.winthier.rph;

import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class Ripper {
    private Ripper() { }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            helpExit();
            return;
        }
        switch (args[0]) {
        case "minecraft-heads.com": {
            if (args.length > 2) {
                helpExit();
                return;
            }
            int start = args.length >= 2 ? Integer.parseInt(args[1]) : 0;
            minecraftHeadsDotCom(System.out, start);
            break;
        }
        case "freshcoal.com": {
            if (args.length > 2) {
                helpExit();
                return;
            }
            freshCoalDotComMain(System.out);
            int start = args.length >= 2 ? Integer.parseInt(args[1]) : 0;
            freshCoalDotComUser(System.out, start);
            break;
        }
        default: {
            helpExit();
            return;
        }
        }
    }

    static void helpExit() {
        help();
        System.exit(1);
    }

    static void help() {
        System.err.println("java -jar PATH minecraft-heads.com [startIndex]");
        System.err.println("java -jar PATH freshcoal.com");
    }

    static String fetch(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
            .uri(URI.create(url))
            .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    static String split(final String str, final char c, final int index) {
        int at = str.indexOf(c);
        if (at < 0) return str;
        return index == 0
            ? str.substring(0, at)
            : str.substring(at + 1);
    }

    static String split(final String str, final String c, final int index) {
        int at = str.indexOf(c);
        if (at < 0) return str;
        return index == 0
            ? str.substring(0, at)
            : str.substring(at + c.length());
    }

    static void minecraftHeadsDotCom(PrintStream out, final int start) {
        int startIndex;
        int totalHeads = 0;
        int totalFailures = 0;
        for (startIndex = start;; startIndex += 80) {
            String url = "https://minecraft-heads.com/custom-heads?start=" + startIndex;
            System.err.println("url: " + url);
            String page;
            try {
                page = fetch(url);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            String[] lines = page.split("\n");
            boolean success = false;
            for (String line : lines) {
                if (!line.startsWith("\t\t\t")) continue;
                if (!line.contains("<a href=\"/custom-heads/")) continue;
                if (!line.endsWith("\">")) continue;
                line = split(line, '"', 1);
                line = split(line, '"', 0);
                System.err.println("head " + line);
                if (minecraftHeadsPage(out, line)) {
                    totalHeads += 1;
                } else {
                    totalFailures += 1;
                }
                success = true;
            }
            if (!success) break;
        }
        System.err.println("Total " + totalHeads + " heads, " + totalFailures + " failures");
    }

    static boolean minecraftHeadsPage(PrintStream out, String url) {
        // url = /custom-heads/CATEGORY/ID-NAME
        String category = url;
        category = split(category, '/', 1);
        category = split(category, '/', 1);
        category = split(category, '/', 0);
        url = "https://minecraft-heads.com" + url;
        System.err.println("url: " + url);
        String page;
        try {
            page = fetch(url);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        List<String> tags = null;
        String[] lines = page.split("\n");
        for (String line : lines) {
            if (line.startsWith("                                <a href=\"/custom-heads/tags/var/")) {
                if (tags == null) tags = new ArrayList<>();
                String tag = line;
                tag = split(tag, '>', 1);
                tag = split(tag, '<', 0);
                tags.add(tag);
            }
            if (!line.contains("/give @p minecraft:player_head{")) continue;
            if (!line.startsWith("        <textarea")) continue;
            if (!line.endsWith(" 1</textarea>")) continue;
            // line = split(line, "/give @p minecraft:player_head", 1);
            // line = split(line, " 1</textarea>", 0);
            RawSkull rawSkull;
            try {
                rawSkull = RawSkull.fromGiveString(line);
            } catch (Exception e) {
                System.err.println(line);
                e.printStackTrace();
                continue;
            }
            rawSkull.printAsYaml(out);
            out.println("  Category: " + category);
            if (tags != null) {
                out.println("  Tags: [" + tags.stream().collect(Collectors.joining(", ")) + "]");
            }
            return true;
        }
        return false;
    }

    static void freshCoalDotComMain(PrintStream out) {
        String url = "https://freshcoal.com/maincollection";
        String page;
        try {
            page = fetch(url);
        } catch (Exception e) {
            System.err.println("url=" + url);
            return;
        }
        int count = 0;
        while (page.contains("<div class='heads ")) {
            page = split(page, "<div class='heads ", 1);
            String category = split(page, "'", 0);
            page = split(page, "/give @p skull 1 3 ", 1);
            String giveCode = split(page, "</", 0);
            RawSkull raw;
            try {
                raw = RawSkull.fromGiveString(giveCode);
            } catch (Exception e) {
                System.err.println(giveCode);
                e.printStackTrace();
                continue;
            }
            raw.printAsYaml(out);
            out.println("  Category: " + category);
            count += 1;
        }
        System.err.println("Main count: " + count);
    }

    static void freshCoalDotComUser(PrintStream out, int start) {
        Set<String> categories = new HashSet<>();
        // Fetch categories
        do {
            String url = "https://freshcoal.com/usercollection?pn=1&rows=0&view=1&sort=asc&sort1=date&category=all";
            String page;
            try {
                page = fetch(url);
            } catch (Exception e) {
                System.err.println("url=" + url);
                return;
            }
            String[] lines = page.split("\n");
            for (String line : lines) {
                if (!line.startsWith("<a href=/usercollection.php")) continue;
                while (line.contains("&category=")) {
                    line = split(line, "&category=", 1);
                    String category = split(line, ">", 0);
                    line = split(line, ">", 1);
                    if (category.equals("all")) continue;
                    categories.add(category);
                    System.err.println("category='" + category + "'");
                }
                break;
            }
        } while (false);
        // All pages
        int count = 0;
        int failureCount = 0;
        for (String category : categories) {
            System.err.println("category=" + category);
            for (int pn = start;; pn += 1) {
                String url = "https://freshcoal.com/usercollection?pn=" + pn + "&rows=200&view=1&sort=asc&sort1=date&category=" + category;
                System.err.println("pn=" + pn + ": " + url);
                String page;
                try {
                    page = fetch(url);
                } catch (Exception e) {
                    System.err.println("url=" + url);
                    failureCount += 1;
                    continue;
                }
                boolean nextPageExists = page.contains("?pn=" + (pn + 1) + "&");
                while (page.contains("<div class='heads' ")) {
                    page = split(page, "<div class='heads' ", 1);
                    page = split(page, "/give @p skull 1 3 ", 1);
                    String giveCode = split(page, "</", 0);
                    RawSkull raw;
                    try {
                        raw = RawSkull.fromGiveString(giveCode);
                    } catch (Exception e) {
                        System.err.println(giveCode);
                        e.printStackTrace();
                        continue;
                    }
                    raw.printAsYaml(out);
                    out.println("  Category: " + category);
                    count += 1;
                }
                if (!nextPageExists) break;
            }
        }
        System.err.println("User count: " + count + " failures: " + failureCount);
    }
}
