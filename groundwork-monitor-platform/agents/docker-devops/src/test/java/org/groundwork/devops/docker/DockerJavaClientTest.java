package org.groundwork.devops.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.StringStartsWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.github.dockerjava.api.model.AccessMode.ro;
import static com.github.dockerjava.api.model.AccessMode.rw;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

@Test(groups = "integration")
public class DockerJavaClientTest {

    public static final Logger log = LoggerFactory.getLogger(DockerJavaClientTest.class);

    private static final String[] PROVISIONED_CONTAINERS = {
            "apache24:latest", "gwos/boxspy:canary", "tomcat:8.0", "httpd:2.4", "centos:centos5", "centos:centos6",
            "centos:centos7", "progrium/busybox:latest"
    };
    protected static final String BUSY_BOX_1 = "busyBox1";
    protected static final String BOXSPY = "BoxSpy";

    private static DockerClient docker;

    @BeforeClass
    public static void setup() {
        //docker = DockerClientBuilder.getInstance("http://172.28.113.158:2375").build();
        docker = DockerClientBuilder.getInstance("http://dock-01-integration.groundwork.groundworkopensource.com:2375").build();
        Info info = docker.infoCmd().exec();
        assert info != null;
    }

    @Test
    public void testListContainers() {
        List<Container> containers = docker.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            System.out.println("Container: " + container.getImage() + " - " + container.getStatus());
            System.out.println("Command: " + container.getCommand()) ;
            for (String name : container.getNames()) {
                System.out.println("\t" + name);
            }
        }
    }

    /**
     *
     --volume=/:/rootfs:ro \
     --volume=/var/run:/var/run:rw \
     --volume=/sys:/sys:ro \
     --volume=/var/lib/docker/:/var/lib/docker:ro \
     --publish=8081:8080 \
     --detach=true \
     --name=BoxSpy \
     */
    @Test
    public void startBoxSpy() {
        try {
            Ports portBindings = new Ports();
            portBindings.bind(ExposedPort.tcp(8081), Ports.Binding(8080));
            String containerId;
            Container container = findContainerByName(BOXSPY);
            if (container == null) {
                CreateContainerResponse response = docker.createContainerCmd("gwos/boxspy")
                        .withName(BOXSPY)
                        .exec();
                log.info("Created container: {}", container.toString());
                assertThat(response.getId(), not(isEmptyString()));
                containerId = response.getId();
            }
            else {
                containerId = container.getId();
            }
            InspectContainerResponse inspector = docker.inspectContainerCmd(containerId).exec();
            if (inspector.getState().isRunning() == false) {
                docker.startContainerCmd(container.getId())
                        .withBinds(
                                new Bind("/", new Volume("/rootfs"), ro),
                                new Bind("/var/run", new Volume("/var/run"), rw),
                                new Bind("/sys", new Volume("/sys"), ro),
                                new Bind("/var/lib/docker", new Volume("/var/lib/docker"), ro)
                        )
                        .withPortBindings(portBindings)
                        .exec();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Test
    public void stopBoxSpy() {
        Container container = findContainerByName(BOXSPY);
        if (container != null) {
            InspectContainerResponse inspector = docker.inspectContainerCmd(container.getId()).exec();
            if (inspector.getState().isRunning()) {
                docker.stopContainerCmd(container.getId()).exec();
            }
        }
    }

    @Test
    public void removeBoxSpy() {
        Container container = findContainerByName(BOXSPY);
        if (container != null) {
            InspectContainerResponse inspector = docker.inspectContainerCmd(container.getId()).exec();
            if (inspector.getState().isRunning()) {
                docker.stopContainerCmd(container.getId()).exec();
            }
            docker.removeContainerCmd(container.getId()).exec();
        }
    }

    @Test
    public void testCreateStartStopRemoveCycle() {

        try {
            String containerId;
            Container container = findContainerByName(BUSY_BOX_1);
            if (container == null) {
                CreateContainerResponse busyBox = docker.createContainerCmd("progrium/busybox").withCmd(new String[]{"top"}).withName(BUSY_BOX_1).exec();

                log.info("Created container {}", busyBox.toString());
                assertThat(busyBox.getId(), not(isEmptyString()));
                docker.startContainerCmd(busyBox.getId()).exec();
                containerId = busyBox.getId();
            } else {
                containerId = container.getId();
                InspectContainerResponse inspector = docker.inspectContainerCmd(containerId).exec();
                if (inspector.getState().isRunning() == false) {
                    docker.startContainerCmd(containerId).exec();
                }
            }


            InspectContainerResponse inspector = docker.inspectContainerCmd(containerId).exec();
            log.info("Container Inspect: {}", inspector.toString());

            assertThat(inspector.getConfig(), is(notNullValue()));
            assertThat(inspector.getId(), not(isEmptyString()));
            assertThat(inspector.getId(), StringStartsWith.startsWith(containerId));
            assertThat(inspector.getImageId(), not(isEmptyString()));
            assertThat(inspector.getState(), is(notNullValue()));

            assertThat(inspector.getState().isRunning(), is(true));

            docker.stopContainerCmd(containerId).exec();
            inspector = docker.inspectContainerCmd(containerId).exec();

            assertThat(inspector.getState().isRunning(), is(false));
            if (!inspector.getState().isRunning()) {
                assertThat(inspector.getState().getExitCode(),
                        CoreMatchers.is(CoreMatchers.equalTo(143)));
            }

            docker.removeContainerCmd(containerId).exec();

            boolean found = true;
            try {
                Thread.sleep(200);
                inspector = docker.inspectContainerCmd(containerId).exec();
            } catch (Exception e) {
                found = false;
            }
            assert found == false;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private Image findImage(String imageName) {
        List<Image> images = docker.listImagesCmd().withShowAll(true).exec();
        for (Image image : images) {
            for (String tag : image.getRepoTags()) {
                if (imageName.equals(tag))
                    return image;
                if (tag.startsWith(imageName))
                    return image;
            }
        }
        return null;
    }

    private Container findContainerByName(String containerName) {
        String path = "/" + containerName;
        List<Container> containers = docker.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            for (String name : container.getNames()) {
                if (path.equals(name))
                    return container;
            }
        }
        return null;
    }

}
