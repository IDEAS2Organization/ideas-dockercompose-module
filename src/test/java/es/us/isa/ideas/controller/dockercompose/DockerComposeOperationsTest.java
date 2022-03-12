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
import org.junit.jupiter.api.Order;

import es.us.isa.ideas.module.common.AppResponse;
import es.us.isa.ideas.module.common.AppResponse.Status;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class DockerComposeOperationsTest {

    
    /*
    - Pruebas unitarias de up y down.
    - Pruebas del controlador (¿automáticas o plan de pruebas?)
    - Pruebas de múltiples usuarios (un usuario no ve los contenedores del resto)
    - ¿Pruebas de flags?
    - ¿Pruebas de coloreado de sintaxis?
    */

    /*
    SETUP:
    1º: Carga un documento de pruebas
    2º: Levanta el contenedor de un usuario concreto
    */

    DockerComposeOperations operations = new DockerComposeOperations();

    String[] usernames = {"test1", "test2"};
    String[] documentNames = {"SimpleDockerCompose.yaml", "IdeasDockerCompose.yaml"};
    String[] documentContents = {"", ""};


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
        
        for (String u: usernames) {
            operations.executeCommand("docker run -d --privileged --name " + u + " aymdev/dind-compose dockerd", "/");
            operations.executeCommand(operations.inContainer(u, "mkdir /dockercomposefiles"), "/");
            operations.executeCommand("docker start " + u, "/");
            System.out.println("CONTAINER RUNNING FOR: " + u);
        }
 
        System.out.println("SETUP OK");
    }

    // Para y elimina los contenedores de prueba
    @AfterAll
    public void stopTestContainer() throws IOException {
        System.out.println("==================================");
        System.out.println("STOPPING TEST CONTAINER");

        for (String u:usernames) {
            operations.executeCommand("docker kill " + u, "/");
            operations.executeCommand("docker rm " + u, "/");
        }        
        System.out.println("==================================");
        System.out.println("TESTS FINISHED");
    }


    @Test
    @Order(1)
    public void testComposeUp() throws IOException {
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
    }

    @Test
    @Order(2)
    public void testComposeDown() throws IOException {
        System.out.println("==================================");
        System.out.println("TEST COMPOSE DOWN");

        // Obtener de un testFile: nombre, flags.
        AppResponse appResponse = new AppResponse();
        String flags = "";

        // Ejecutar 
        operations.down("SimpleDockerCompose.yaml", usernames[0], flags, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);

        // Comprobar que hay 0 contenedores en ejecución
        String[] output = operations.executeCommandForTesting(operations.inContainer(usernames[0], "docker ps -aq"), "/");
        assertEquals("", output[0]);
    }


    @Test
    @Order(3)
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
