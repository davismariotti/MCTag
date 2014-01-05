package info.gomeow.mctag;

public class TagInfo {

    private int tags = 0;
    private int tagged = 0;

    public void addTags() {
        tags++;
    }

    public int getTags() {
        return tags;
    }

    public void addTagged() {
        tagged++;
    }

    public int getTagged() {
        return tagged;
    }


}
