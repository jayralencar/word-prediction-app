package br.com.clubedosgeeks.wordprediction;

public class Recognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private final String id;

    /**
     * Display name for the recognition.
     */
    private final String word;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private final Float confidence;

    public Recognition(
            final String id, final String word, final Float confidence) {
        this.id = id;
        this.word = word;
        this.confidence = confidence;
    }
    public String getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public Float getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        String resultString = "";
        if (id != null) {
            resultString += "[" + id + "] ";
        }

        if (word != null) {
            resultString += word + " ";
        }

        if (confidence != null) {
            resultString += String.format("(%.1f%%) ", confidence * 100.0f);
        }

        return resultString.trim();
    }
}
