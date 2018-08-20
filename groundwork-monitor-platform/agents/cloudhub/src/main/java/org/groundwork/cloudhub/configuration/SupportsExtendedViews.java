package org.groundwork.cloudhub.configuration;

import java.util.List;

/**
 * Created by dtaylor on 5/22/17.
 */
public interface SupportsExtendedViews {

    List<ConfigurationView> getViews();

    ConfigurationView getView(String viewName);

    void setViews(List<ConfigurationView> views);

}
