package com.groundwork.collage.test;

import com.groundwork.collage.CollageFactory;
import org.junit.After;
import org.junit.Before;

public class AbstractSpringAssembledTest extends AbstractTestCaseWithTransactionSupport {

    public AbstractSpringAssembledTest(String x) { super(x); }

    protected CollageFactory collage;

    @Before
    protected void setUp()
    {
        collage = CollageFactory.getInstance();
        collage.loadSpringAssembly("META-INF/test-common-model-assembly.xml");
    }

    @After
    protected void tearDown() {
        //collage.shutdown();
    }

    public void testCollageObject()
    {
        assertNotNull(collage);

    }


}
