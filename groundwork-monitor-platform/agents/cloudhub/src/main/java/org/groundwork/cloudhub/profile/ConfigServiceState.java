package org.groundwork.cloudhub.profile;

import java.io.Serializable;

public class ConfigServiceState implements Serializable {

    public enum ConfigView {
        ViewStorage(0),
        ViewNetwork(1),
        ViewPool(2),
        ViewCustom(3);

        private int index;

        private ConfigView(int index) {
            this.index = index;
        }
    }
    public static final int VIEW_STORAGE = 0;
    public static final int VIEW_NETWORK = 1;
    public static final int VIEW_POOL = 2;
    public static final int VIEW_CUSTOM = 3;
    public static final int VIEWS_SIZE = 4;

    private Boolean views[] = new Boolean[VIEWS_SIZE];

    public void reset() {
        for (int ix = 0; ix < VIEWS_SIZE; ix++) {
            views[ix] = null;
        }
    }

    public void setView(ConfigView configView, Boolean b) {
        views[configView.index] = b;
    }

    public Boolean getView(ConfigView configView) {
        return views[configView.index];
    }

}
