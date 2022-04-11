package es.us.isa.ideas.controller.dockercompose;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.assertj.core.util.Files;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.Order;

import es.us.isa.ideas.module.common.AppResponse;
import es.us.isa.ideas.module.common.AppResponse.Status;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class DockerComposeOperationsTest {

    /*
    SETUP:
    1º: Carga un documento de pruebas
    2º: Levanta el contenedor de un usuario concreto
    */

    DockerComposeOperations operations = new DockerComposeOperations();

    String[] usernames = {"test1", "test2"};
    String[] documentNames = {"SimpleDockerCompose.yaml", "IdeasDockerCompose.yaml", "OneServiceCompose.yaml"};
    String[] documentContents = {"", "", ""};
    private Boolean one_user_mode = Boolean.parseBoolean(System.getenv("ONE_USER_MODE"));


    @BeforeAll
    public void setup() throws IOException {
        System.out.println("==================================");
        System.out.println("START SETUP");

        String path="src/main/resources/testfiles/";
        for (int i=0; i<documentNames.length;i++) {
            File file=new File(path + documentNames[i]);
            if(file.exists()) {
                documentContents[i]=Files.contentOf(file, Charset.defaultCharset());
                System.out.println("LOAD FILE: " + path+documentNames[i]);
            } 
        }
        
        if (!one_user_mode) {
            for (String u: usernames) {
                operations.executeCommand("docker run -d --privileged --name " + u + " docker4ideas/dind-compose dockerd", "/");
                operations.executeCommand(operations.inContainer(u, "mkdir /dockercomposefiles"), "/");
                operations.executeCommand("docker start " + u, "/");
                System.out.println("CONTAINER RUNNING FOR: " + u);
            }
        } else {
            operations.executeCommand("docker kill $(docker ps -aq)", "/");
            operations.executeCommand("docker rm $(docker ps -aq)", "/");
            operations.executeCommand(operations.inContainer("", "mkdir /dockercomposefiles"), "/");
        }
        
 
        System.out.println("SETUP OK");
    }

    // Para y elimina los contenedores de prueba
    @AfterAll
    public void stopTestContainer() throws IOException {
        System.out.println("==================================");
        System.out.println("STOPPING TEST CONTAINER");

        if (!one_user_mode) {
            for (String u:usernames) {
                operations.executeCommand("docker kill " + u, "/");
                operations.executeCommand("docker rm " + u, "/");
            }       
        } else {
            operations.executeCommand("docker kill $(docker ps -aq)", "/");
            operations.executeCommand("docker rm $(docker ps -aq)", "/");
        }
         
        System.out.println("==================================");
        System.out.println("TESTS FINISHED");
    }


    @Test
    @Order(1)
    public void testComposeUpAndDown() throws IOException {
        System.out.println("==================================");
        System.out.println("TEST COMPOSE UP");

        // Obtener de un testFile: nombre, contenido, flags.
        AppResponse appResponse = new AppResponse();
        String flags = "";

        // Ejecutar 
        operations.up(documentContents[0], "SimpleDockerCompose.yaml", usernames[0], flags, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);

        // Comprobar que hay 2 contenedores en ejecución
        String[] output = operations.executeCommandForTesting(operations.inContainer(usernames[0], "docker ps -aq"), "/");
        assertEquals(2, output[0].split("\n").length);

        System.out.println("==================================");
        System.out.println("TEST COMPOSE DOWN");

        // Obtener de un testFile: nombre, flags.
        appResponse = new AppResponse();
        flags = "";

        // Ejecutar 
        operations.down("SimpleDockerCompose.yaml", usernames[0], flags, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);

        // Comprobar que hay 0 contenedores en ejecución
        output = operations.executeCommandForTesting(operations.inContainer(usernames[0], "docker ps -aq"), "/");
        assertEquals("", output[0]);
        
    }


    @Test
    @Order(3)
    public void testLogs() throws IOException {
        System.out.println("==================================");
        System.out.println("TEST LOGS FROM CONTAINER");

        // Compose Up de solo un contenedor y obtenemos containerId
        AppResponse appResponse = new AppResponse();
        String flags = "";
        operations.up(documentContents[2], "OneServiceCompose.yaml", usernames[0], flags, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
        
        flags = "--quiet";
        operations.showContainers(usernames[0], flags, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
        String containerId = appResponse.getHtmlMessage().split("<pre>")[1].split("\n")[0]; 

        // Ejecutar
        operations.logs_from_container(usernames[0], containerId, appResponse);

        // Comprobar que la salida es la esperada
        assertEquals(appResponse.getStatus(), Status.OK);
        String logs = appResponse.getHtmlMessage();
        File logs_output = new File("src/main/resources/testfiles/logs.txt");
        assertEquals(logs.replaceAll("</b>.*? ms", "</b>1 ms").replaceAll("172\\.[0-9]{2}\\.0\\..*?\\.", "172.18.0.*.")
                .replaceAll("\n\\[.*?\\]", "\n[fecha]").replaceAll("tid .*?]", "tid \\*]").replaceAll("Apache/2\\.4\\.[0-9]{2}", "Apache/2.4.52"), 
                Files.contentOf(logs_output, Charset.defaultCharset()));

        // Borrar contenedor tras su uso
        operations.down("OneServiceCompose.yaml", usernames[0], flags, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
    }


    @Test
    @Order(4)
    @DisabledIfEnvironmentVariable(named = "ONE_USER_MODE", matches= "true")
    public void testMultipleUsers() throws IOException {
        System.out.println("==================================");
        System.out.println("TEST MULTIPLE USERS");

        AppResponse appResponse = new AppResponse();
        String flags = "";

        // Cada usuario debería ver solo 2 contenedores
        for (String u:usernames) {
            operations.up(documentContents[0], "SimpleDockerCompose.yaml", u, flags, appResponse);
            assertEquals(appResponse.getStatus(), Status.OK);

            String[] output = operations.executeCommandForTesting(operations.inContainer(u, "docker ps -aq"), "/");
            assertEquals(2, output[0].split("\n").length);
        }  

        // Hacer down con "test1" no afecta a "test2"
        operations.down("SimpleDockerCompose.yaml", usernames[0], flags, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
        String[] output = operations.executeCommandForTesting(operations.inContainer(usernames[1], "docker ps -aq"), "/");
        assertEquals(2, output[0].split("\n").length);
    }
}
