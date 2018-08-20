package com.groundwork.portal.organization;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;

public class UserImportHelper {
    private SMHierarchicCursor rootC;
    private SMInputCursor      userC;

    /**
     * Should be called to start import from file.
     *
     * @param   is
     *
     * @throws  Exception
     */
    public void startImport(InputStream is) throws Exception {
        SMInputFactory inf = new SMInputFactory(XMLInputFactory.newInstance());

        rootC = inf.rootElementCursor(is);
        rootC.advance();
        userC = rootC.childElementCursor("user");
    }

    /** @throws  Exception */
    public void endImport() throws Exception {
        if (rootC != null) {
            rootC.getStreamReader().closeCompletely();
        }
    }

    /**
     * Will return MUser from piece of XML that StAX cursor is currently pointing at.
     *
     * @return
     *
     * @throws  Exception
     */
    public ImportUser getUser() throws Exception {
        // Check if there is still something to read
        userC.advance();

        if (userC.asEvent() == null) {
            return null;
        }

        // <user>
        String name = userC.getAttrValue("name");

        // <properties>
        Map<String, String> props       = new HashMap<String, String>();
        SMInputCursor       propertiesC = userC.childElementCursor("properties").advance();

        // <property>
        SMInputCursor propC = propertiesC = propertiesC.childElementCursor("property").advance();

        while (propC.asEvent() != null) {
            String propertyName = propC.getAttrValue("name");

            // This could be used if needed- java type ie java.lang.String -
            // dropping for now
            String propertyType  = propC.getAttrValue("type");
            String propertyValue = propC.getElemStringValue();

            props.put(propertyName, propertyValue);
            propC.advance();
        }

        ImportUser muser = new ImportUser(name, props);

        return muser;
    }

    /**
     * Will parse whole XML file at once.
     *
     * @return
     *
     * @throws  Exception
     */
    public List<ImportUser> getUsers() throws Exception {
        List<ImportUser> users = new LinkedList<ImportUser>();
        ImportUser       user  = getUser();

        while (user != null) {
            users.add(user);
            user = getUser();
        }

        return users;
    }
}
