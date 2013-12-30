package info.gomeow.mctag;

public class TagInfo {

    private int tags = 0;
    private int tagged = 0;

    public void addTags(int num) {
        tags = tags + num;
    }

    public int getTags() {
        return tags;
    }

    public void addTagged(int num) {
        tagged = tagged + num;
    }

    public int getTagged() {
        return tagged;
    }


}
