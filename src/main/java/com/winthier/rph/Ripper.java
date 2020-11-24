package com.winthier.rph;

import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class Ripper {
    private Ripper() { }

    public static void main(String[] args) throws Exception {
        int start = args.length >= 1 ? Integer.parseInt(args[0]) : 0;
        minecraftHeadsDotCom(System.out, start);
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
        url = "https://minecraft-heads.com" + url;
        System.err.println("url: " + url);
        String page;
        try {
            page = fetch(url);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        String[] lines = page.split("\n");
        for (String line : lines) {
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
            return true;
        }
        return false;
    }
}
