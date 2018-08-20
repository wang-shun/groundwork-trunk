package com.groundwork.portal.organization;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLInputFactory;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;

public class RoleImportHelper {
    private SMHierarchicCursor rootC;
    private SMInputCursor      roleC;

    /**
     * @param   is
     *
     * @throws  Exception
     */
    public void startImport(InputStream is) throws Exception {
        SMInputFactory inf = new SMInputFactory(XMLInputFactory.newInstance());

        rootC = inf.rootElementCursor(is);
        rootC.advance();
        roleC = rootC.childElementCursor("role");
    }

    /** @throws  Exception */
    public void endImport() throws Exception {
        if (rootC != null) {
            rootC.getStreamReader().closeCompletely();
        }
    }

    /**
     * Will return single MRole generated from the current piece of XML that StAX cursor is pointing on.
     *
     * @return
     *
     * @throws  Exception
     */
    public ImportRole getImportRole() throws Exception {
        // Check if there is still something to read
        roleC.advance();

        if (roleC.asEvent() == null) {
            return null;
        }

        // <role>
        String name        = roleC.getAttrValue("name");
        String displayName = roleC.getAttrValue("displayName");

        // <members>
        Set<String>   members  = new HashSet<String>();
        SMInputCursor membersC = roleC.childElementCursor("members").advance();
        SMInputCursor memberC  = membersC.childElementCursor("member").advance();

        // <member>
        while (memberC.asEvent() != null) {
            String memberName = memberC.getAttrValue("name");

            members.add(memberName);
            memberC.advance();
        }

        ImportRole mrole = new ImportRole(name, displayName, members);

        return mrole;
    }

    /**
     * Will parse whole XML once and return all MRole objects. For bigger XML files can trigger memory problems.
     *
     * @return
     *
     * @throws  Exception
     */
    public List<ImportRole> getImportRoles() throws Exception {
        List<ImportRole> roles = new LinkedList<ImportRole>();
        ImportRole       mrole = getImportRole();

        while (mrole != null) {
            roles.add(mrole);
            mrole = getImportRole();
        }

        return roles;
    }
}
