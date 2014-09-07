package hu.rycus.tweetwear.preferences;

import java.util.HashSet;
import java.util.Set;

import hu.rycus.tweetwear.common.util.Mapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListSettings {

    private boolean timelineSelected = true;

    private final Set<Long> selectedListIds = new HashSet<Long>();

    public void setListSelected(final long id, final boolean selected) {
        if (selected) {
            selectedListIds.add(id);
        } else {
            selectedListIds.remove(id);
        }
    }

    public boolean isListSelected(final long id) {
        return selectedListIds.contains(id);
    }

    public static ListSettings create(final String json) {
        final ListSettings settings = Mapper.readObject(json, ListSettings.class);
        if (settings != null) {
            return settings;
        } else {
            return new ListSettings();
        }
    }

    public String serialize() {
        return Mapper.writeObjectAsString(this);
    }

}
