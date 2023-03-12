package io.github.mortuusars.mpfui.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LoremIpsum {
    private static final String[] allWords = """
        Lorem ipsum dolor sit amet consectetur adipiscing elit
        sed do eiusmod tempor incididunt ut labore et dolore magna aliqua ut enim ad minim
        veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat duis aute irure dolor
        in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur, excepteur sint occaecat cupidatat
        non proident, sunt in culpa qui officia deserunt mollit anim id est laborum""".split(" ");

    public static String words(int count){
        List<String> parts = new ArrayList<>();

        // Start with first words
        for (int i = 0; i < Math.min(count, 7); i++) {
            parts.add(allWords[i]);
        }

        if (parts.size() < count) {
            Random random = new Random();
            for (int i = 0; i < count - parts.size(); i++) {
                parts.add(allWords[random.nextInt(0, allWords.length)]);
            }
        }

        return String.join(" ", parts);
    }

    public static String chars(int count){
        List<String> parts = new ArrayList<>();

        int whole = allWords.length;

        Random random = new Random();

        while (count > 0){
            String word = whole >= 0 ? allWords[whole--] : allWords[random.nextInt(0, allWords.length)];
            String addedWord = word.length() > count ? word.substring(0, count) : word;
            parts.add(addedWord);
            count -= addedWord.length() + 1;
        }

        return String.join(" ", parts);
    }
}