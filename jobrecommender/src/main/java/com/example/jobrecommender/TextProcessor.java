package com.example.jobrecommender;
import edu.stanford.nlp.pipeline.*;
import java.util.Properties;

public class TextProcessor {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        String cvText = "Mihai-Alexandru Pascu\n" +
                "Data na?terii: 20/10/2003\n" +
                "Skills: Java, Python, C++, SQL, HTML, CSS, Unity\n" +
                "Education: University Politehnica of Bucharest";

        CoreDocument document = new CoreDocument(cvText);

        pipeline.annotate(document);

        System.out.println("Entities:");
        for (CoreEntityMention entity : document.entityMentions()) {
            System.out.println(entity.text() + " - " + entity.entityType());
        }
    }
}