package org.groundwork.cloudhub.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.groundwork.cloudhub.api.dto.DtoProfileView;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;

/**
 * Created by dtaylor on 5/23/17.
 */
public class ViewProfileTest {

    @Test
    public void testReadProfile() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            DtoProfileView viewProfileWrapper = mapper.readValue(new File("./src/test/testdata/cloudera/metrics.json"), DtoProfileView.class);
            assertNotNull(viewProfileWrapper);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
