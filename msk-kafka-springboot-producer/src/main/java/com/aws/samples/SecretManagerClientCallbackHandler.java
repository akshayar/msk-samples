package com.aws.samples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.AppConfigurationEntry;
import java.util.*;
import java.util.stream.Collectors;

public class SecretManagerClientCallbackHandler implements AuthenticateCallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(SecretManagerClientCallbackHandler.class);
    String secretName="";
    String region="";
    public static JsonNode getSecret(String secretName, String region) {
        ObjectMapper objectMapper = new ObjectMapper();

        // Create a Secrets Manager client
        JsonNode secretNode;
        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(region))
                .build()) {

            // In this sample we only handle the specific exceptions for the 'GetSecretValue' API.
            // See https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
            // We rethrow the exception by default.

            String secret, decodedBinarySecret;
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName).build();
            try {
                GetSecretValueResponse getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

                // Decrypts secret using the associated KMS key.
                // Depending on whether the secret is a string or binary, one of these fields will be populated.
                if (getSecretValueResponse.secretString() != null) {
                    secret = getSecretValueResponse.secretString();
                    secretNode = objectMapper.readTree(secret);

                } else {
                    decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResponse.secretBinary().asByteArray()));
                    secretNode = objectMapper.readTree(decodedBinarySecret);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return secretNode;
    }

    @Override
    public void configure(Map<String, ?> configs, String saslMechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        Map<?,?> configMap=jaasConfigEntries.stream().map(AppConfigurationEntry::getOptions).flatMap(m->m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
        secretName= Optional.ofNullable(configMap.get("secretId")).map(Objects::toString).orElse("");
        region=Optional.ofNullable(configMap.get("region")).map(Objects::toString).orElse("us-east-1");
        log.warn("Region:" +region+",secretName:"+secretName);
    }

    @Override
    public void close() {
        log.warn("closing provider");

    }

    @Override
    public void handle(Callback[] callbacks) {
        JsonNode node = getSecret(secretName, region);
        String username = node.get("username").asText();
        String password = node.get("password").asText();

        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                log.warn(">>>>"+callback);
                ((NameCallback) callback).setName(username);
            } else if (callback instanceof PasswordCallback) {
                log.warn(">>>>"+callback);
                ((PasswordCallback) callback).setPassword(password.toCharArray());
            }else{
                log.warn(">>>>"+callback);
            }
        }
    }
}
