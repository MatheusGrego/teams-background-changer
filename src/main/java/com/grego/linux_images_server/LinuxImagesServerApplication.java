package com.grego.linux_images_server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@SpringBootApplication
public class LinuxImagesServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinuxImagesServerApplication.class, args);
    }

    @Component
    public static class AppStartupRunner implements CommandLineRunner {
        @Override
        public void run(String... args) {
            new CommandLine(new ServerInfo()).execute(args);
        }
    }

    @Command(name = "server-info", mixinStandardHelpOptions = true, version = "1.0",
            description = "Displays server information.")
    public static class ServerInfo implements Callable<Integer> {

        @Value("${server.port}")
        private int port;

        @EventListener(ApplicationReadyEvent.class)
        public void onApplicationEvent() {
            displayInfo();
        }

        @Override
        public Integer call() {
            displayInfo();
            return 0;
        }

        private void displayInfo() {
            System.out.println("\n\033[1;32mImage Server is running!\033[0m");
            System.out.println("\033[1;34mAccess the server at: \033[1;33mhttp://localhost:" + port + "\033[0m");
            System.out.println("\033[1;34mPress Ctrl+C to exit.\033[0m");
        }
    }
}
