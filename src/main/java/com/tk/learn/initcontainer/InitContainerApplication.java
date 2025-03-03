package com.tk.learn.initcontainer;

import com.azure.security.keyvault.secrets.SecretClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@SpringBootApplication(scanBasePackages = "com.tk.learn.initcontainer")
public class InitContainerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(InitContainerApplication.class)
                .web(WebApplicationType.NONE)
                .lazyInitialization(true)
                .run(args);
    }


    @Bean
    public CommandLineRunner init(SecretClient secretClient) {
        return args -> {
            String secret1 = secretClient.getSecret("mysecretname").getValue();
            String secret2 = secretClient.getSecret("mysecretname2").getValue();

            // Prepare the secrets to write to YAML
            Map<String, String> secretMap = Map.of(
                    "secret1", secret1,
                    "secret2", secret2
            );

            // Write secrets to a YAML file
            writeSecretsToYamlFile(secretMap);
            System.exit(0);
        };
    }

    private void writeSecretsToYamlFile(Map<String, String> secrets) {
        DumperOptions options = new DumperOptions();
        options.setIndent(2); // Indentation for YAML file
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Use block style for better readability

        Yaml yaml = new Yaml(options);

        String yamlFilePath = System.getenv("SECRET_YAML_PATH");
        if (yamlFilePath == null) {
            // Fallback to default if the environment variable is not set yamlFilePath = "/tmp/secrets.yaml";
            yamlFilePath = "/Users/thejkaruneegar/az-init-container/init-container/mnt/secrets.yaml";
            // Modify as per your need (e.g., location in Docker container)
        }
        // Specify the file path where you want to store the YAML file

        try (FileWriter writer = new FileWriter(yamlFilePath)) {
            yaml.dump(secrets, writer);
            System.out.println("Secrets have been written to " + yamlFilePath);
        } catch (IOException e) {
            System.err.println("Error writing secrets to YAML file: " + e.getMessage());
        }
    }

}
