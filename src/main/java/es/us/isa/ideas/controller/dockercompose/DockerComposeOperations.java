package es.us.isa.ideas.controller.dockercompose;


import es.us.isa.ideas.module.common.AppResponse;
import es.us.isa.ideas.module.common.AppResponse.Status;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DockerComposeOperations {


    public String inContainer(String username, String command) {
        return "docker exec " + username + " " + command;
    }

    public void up(String content, String fileName, String username, AppResponse appResponse) {
        try {
            //executeCommand(inContainer(username, "mkdir /dockercomposefiles"), "/");
            executeCommand(inContainer(username, "touch /dockercomposefiles/" + fileName), "/");

            Path path = Paths.get("/dockercomposefiles");
            Files.createDirectories(path);
            File tmpDockerComposeFile = new File("/dockercomposefiles/" + username);
            FileWriter fw = new FileWriter(tmpDockerComposeFile);
            fw.write(content);
            fw.close();

            executeCommand("docker cp /dockercomposefiles/" + username + " " + username + ":/dockercomposefiles/" + fileName, "/");
            tmpDockerComposeFile.delete();

            String message = executeCommand(inContainer(username, "docker-compose -f /dockercomposefiles/" + fileName + " --no-ansi up -d"),
                    "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void down(String content, String fileName, String username, AppResponse appResponse) {
        try {
            String message = executeCommand(inContainer(username, "docker-compose -f /dockercomposefiles/" + fileName + " --no-ansi down"),
                    "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }


    public String executeCommand(String command, String inputPath) throws IOException {
        long start = System.currentTimeMillis();

        System.out.println(System.currentTimeMillis() + " - Executing command: '" + command + "' at path: '"
                + inputPath + "'");

        String[] commands = command.split(" ");
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(new File(inputPath));

        Path output = Files.createTempFile("", "-outuput.log");
        Path errors = Files.createTempFile("", "-error.log");
        pb.redirectError(Redirect.appendTo(errors.toFile()));
        pb.redirectOutput(Redirect.appendTo(output.toFile()));

        Process p = pb.start();
        while (p.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger("Docker").log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(System.currentTimeMillis() + " - Command execution finished with code: " + p.exitValue());
        String outputString = org.assertj.core.util.Files.contentOf(output.toFile(), Charset.defaultCharset());
        String errorString = org.assertj.core.util.Files.contentOf(errors.toFile(), Charset.defaultCharset());
        return generateHTMLMessage(outputString, errorString, System.currentTimeMillis() - start);
    }

    public String generateHTMLMessage(String output, String errors, long duration) {
        StringBuilder builder = new StringBuilder();
        builder.append("<b>Execution duration:</b>" + duration + " ms<br>\n");

        builder.append("<h3>Operation output:</h3>\n");
        builder.append("<p><pre>" + output + "</pre></p>\n");

        if (!errors.equals("")) {
            builder.append("<h3>Errors found:</h3>\n");
            builder.append("<p><pre>" + errors + "</pre></p>\n");
        }
        return builder.toString();

    }

    public void generateAppResponseError(AppResponse appResponse, IOException e) {
        appResponse
                .setHtmlMessage("<h1>An error has ocurred. </h1><br><b><pre>" + e.toString() + "'</pre></b>");
        appResponse.setStatus(Status.OK_PROBLEMS); // Si se pone Status.ERRORS no muestra el mensaje HTML
    }
    
}
